package flinkdemo;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer.Optimum;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.io.jdbc.JDBCInputFormat;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple1;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.streaming.api.functions.co.CoFlatMapFunction;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.api.functions.windowing.RichWindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.table.api.*;
import org.apache.flink.types.Row;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aiblab.data.minewg1.MinewDataDao;
import com.lemmingapex.trilateration.NonLinearLeastSquaresSolver;
import com.lemmingapex.trilateration.TrilaterationFunction;

import flinkdemo.connector.influxdb.InfluxDBConfig;
import flinkdemo.connector.influxdb.InfluxDBPoint;
import flinkdemo.connector.influxdb.InfluxDBSink;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RssiApp {

	static Logger LOG = LoggerFactory.getLogger(RssiApp.class);
			
	@SuppressWarnings("serial")
	public static void main(String[] args) throws Exception {
		final ParameterTool params = ParameterTool.fromArgs(args);

		// set up the execution environment
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		final StreamTableEnvironment tEnv = StreamTableEnvironment.getTableEnvironment(env);

		String influxdbUrl = "http://localhost:8086";
		String influxUser = "";
		String influxPwd = "";
		
		env.getConfig().setGlobalJobParameters(params);
		//env.setParallelism(4);
		env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
		
		final TypeInformation<?>[] fieldTypes =
		        new TypeInformation<?>[] { BasicTypeInfo.STRING_TYPE_INFO 
								,BasicTypeInfo.DOUBLE_TYPE_INFO, BasicTypeInfo.DOUBLE_TYPE_INFO 
								,BasicTypeInfo.DOUBLE_TYPE_INFO, BasicTypeInfo.DOUBLE_TYPE_INFO, BasicTypeInfo.DOUBLE_TYPE_INFO  };

		    final RowTypeInfo rowTypeInfo = new RowTypeInfo(fieldTypes);
		    
		JDBCInputFormat.JDBCInputFormatBuilder inputBuilder =
		        JDBCInputFormat.buildJDBCInputFormat().setDrivername("org.mariadb.jdbc.Driver")
		        	.setDBUrl("jdbc:mariadb://localhost:3306/aibworks?serverTimezone=UTC")
		            .setQuery("select * form gateway").setRowTypeInfo(rowTypeInfo).setUsername("root");

		DataStream<Row> gatewayTable = env.createInput(inputBuilder.finish());
	
		MapStateDescriptor<String, Row> gatewayStateDescriptor = new MapStateDescriptor<>(
				"RulesBroadcastState",
				BasicTypeInfo.STRING_TYPE_INFO,
				TypeInformation.of(new TypeHint<Row>() {}));
		
		BroadcastStream<Row> gatewayBroadcastStream = gatewayTable.broadcast(gatewayStateDescriptor);
		
		
		Properties properties = new Properties();
		properties.setProperty("bootstrap.servers", "localhost:9092");
		properties.setProperty("group.id", "minew-collector-group");
		
		DataStream<List<MinewDataDao>> stream = env.addSource(
				new FlinkKafkaConsumer<List<MinewDataDao>>("minew", new MinewDeserializationSchema(), properties) );

//		DataStream<GatewaySource> gwSource = stream.flatMap( new RichFlatMapFunction<List<MinewDataDao>, GatewaySource>() {
//
//			@Override
//			public void flatMap(List<MinewDataDao> value, Collector<GatewaySource> out) throws Exception {
//				for ( MinewDataDao dao : value) {
//					if ( "Gateway".equals(dao.getType()) ) {
//						GatewaySource source = new GatewaySource();
//						source.setGatewayMac(dao.getMac());
//						source.setTimestamp(dao.getTimestamp());
//						source.setGatewayFree(dao.getGatewayFree());
//						source.setGatewayLoad(dao.getGatewayLoad());
//						out.collect(source);
//						break;
//					}
//				}
//			}		
//		} );
		
		DataStream<IBeaconSource> bleSource = stream.flatMap( new RichFlatMapFunction<List<MinewDataDao>, IBeaconSource>() {

			@Override
			public void flatMap(List<MinewDataDao> value, Collector<IBeaconSource> out) throws Exception {
				
				MinewDataDao gatewayData = null;
				for ( MinewDataDao dao : value) {
					if ( "Gateway".equals(dao.getType()) ) {
						gatewayData = dao;			
						break;
					}
				}
				if ( null == gatewayData )
					return;
				
				for ( MinewDataDao dao : value) {
					if ( "iBeacon".equals(dao.getType())) {
						IBeaconSource source = new IBeaconSource();
						source.setGatewayMac(gatewayData.getMac());
						source.setMac(dao.getMac());
						source.setBleName(dao.getBleName());
						source.setIBeaconMajor(dao.getIbeaconMajor());
						source.setIBeaconMinor(dao.getIbeaconMinor());
						source.setIBeaconTxPower(dao.getIbeaconTxPower());
						source.setRssi(dao.getRssi());
						source.setBattery(dao.getBattery());
						
						try {
							source.setTimestamp(ZonedDateTime.parse(dao.getTimestamp()).toInstant().toEpochMilli() );
							
							double ratio = dao.getRssi() * 1.0 / dao.getIbeaconTxPower();
							double distance = -1;
							if ( ratio < 1.0 ) 
								distance = Math.pow(ratio, 10);
							else {
								// get coefficents
								double coefficentA = 0.89976;
								double coefficentB = 7.7095;
								double coefficentC = 0.111;
								distance = coefficentA * Math.pow(ratio, coefficentB) + coefficentC;
							}
							source.setDistance(distance);
							
							out.collect(source);
						} catch (DateTimeParseException e) {
							LOG.error("{}", e);
						}
					}
				}				
			}
			
		} ).assignTimestampsAndWatermarks(new AscendingTimestampExtractor<IBeaconSource>() {

			@Override
			public long extractAscendingTimestamp(IBeaconSource element) {
				return element.getTimestamp();
			}			
		});
		
//		DataStream<IBeaconSource> a = bleSource.connect(gatewayBroadcastStream).process(new BroadcastProcessFunction<IBeaconSource, Row, IBeaconSource>() {
//
//			private final MapStateDescriptor<String, Row> gatewayStateDescriptor = new MapStateDescriptor<>(
//					"RulesBroadcastState",
//					BasicTypeInfo.STRING_TYPE_INFO,
//					TypeInformation.of(new TypeHint<Row>() {}));
//			
//			@Override
//			public void processBroadcastElement(Row value,
//					BroadcastProcessFunction<IBeaconSource, Row, IBeaconSource>.Context ctx,
//					Collector<IBeaconSource> out) throws Exception {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public void processElement(IBeaconSource value,
//					BroadcastProcessFunction<IBeaconSource, Row, IBeaconSource>.ReadOnlyContext ctx,
//					Collector<IBeaconSource> out) throws Exception {
//				// TODO Auto-generated method stub
//				
//			}
//			
//		});
		

		 DataStream<InfluxDBPoint> dataStream = bleSource.map(
				 new RichMapFunction<IBeaconSource, InfluxDBPoint>() {
					private static final long serialVersionUID = 1L;
					@Override
					public InfluxDBPoint map(IBeaconSource value) throws Exception {
						
						InfluxDBPoint point = new InfluxDBPoint("ibeacon", value.getTimestamp());
						point.getTags().put("mac", value.getMac());
						point.getTags().put("gateway", value.getGatewayMac());
						point.getTags().put("blename", value.getBleName());
						point.getFields().put("rssi", value.getRssi());
						point.getFields().put("txpower", value.getIBeaconTxPower());
						point.getFields().put("ibeaconmajor", value.getIBeaconMajor());
						point.getFields().put("ibeaconminor", value.getIBeaconMinor());
						point.getFields().put("distance", value.getDistance());
						
						return point;
					}				 
				 });


		 InfluxDBConfig influxDBConfig = InfluxDBConfig.builder().url(influxdbUrl)
			.username(influxUser).password(influxPwd)
		 	.database("db_flink_test").database("db_flink_test").build();

	     dataStream.addSink(new InfluxDBSink(influxDBConfig));		

//		FlinkKafkaProducer<IBeaconSource> producer = new FlinkKafkaProducer<IBeaconSource>("localhost:9092", "ibeacon", 
//				new IBeaconSerializationSchema());
//		producer.setWriteTimestampToKafka(true);
//		
//		bleSource.addSink(producer);

		
		
//		Properties properties2 = new Properties();
//		properties2.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
//		properties2.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "ibeacon-collector-group");
//		FlinkKafkaConsumer<IBeaconSource> bleConsumer = 
//				new FlinkKafkaConsumer<IBeaconSource>("ibeacon", new IBeaconDeserializationSchema(), properties2);
//		
//		long offsetTimestamp = Instant.now().minus(10, ChronoUnit.MINUTES).toEpochMilli();
//		bleConsumer.setStartFromTimestamp(offsetTimestamp);
//		
//		DataStream<IBeaconSource> bleStream = env.addSource(
//				bleConsumer );

		
		DataStream<IBeaconDistanceSet> bleDistance = bleSource.keyBy("mac")
				.timeWindow(Time.seconds(5), Time.seconds(1)).apply( 
				new RichWindowFunction<IBeaconSource, IBeaconDistanceSet, Tuple, TimeWindow>() {

					@SuppressWarnings("unchecked")
					@Override
					public void apply(Tuple key, TimeWindow window, Iterable<IBeaconSource> input,
							Collector<IBeaconDistanceSet> out) throws Exception {
						
						IBeaconDistanceSet distanceSet = new IBeaconDistanceSet();
						distanceSet.setMac(((Tuple1<String>)key).f0 );
						
						for ( IBeaconSource value : input) {
							distanceSet.setBleName(value.getBleName());
							distanceSet.setIBeaconMajor(value.getIBeaconMajor());
							distanceSet.setIBeaconMinor(value.getIBeaconMinor());
							
							IBeaconDistanceSet.IBeaconDistance inner = new IBeaconDistanceSet.IBeaconDistance();
							
							inner.setTimestamp(value.getTimestamp());
							inner.setDistance(value.getDistance());
							
							distanceSet.getValues().put(value.getGatewayMac(), inner);
						}
						if ( distanceSet.getValues().size() > 0 )
							out.collect(distanceSet);
					}					
				}
		);

		DataStream<IBeaconLocation> bleLocStream = bleDistance.map(new RichMapFunction<IBeaconDistanceSet, IBeaconLocation>() {

			@Override
			public IBeaconLocation map(IBeaconDistanceSet value) throws Exception {
				IBeaconLocation loc = new IBeaconLocation();
				loc.setBleName(value.getBleName());
				loc.setIBeaconMajor(value.getIBeaconMajor());
				loc.setIBeaconMinor(value.getIBeaconMinor());
				loc.setMac(value.getMac());
				long maxTimestamp = -1;
				String location = "";
				if ( 1 == value.getValues().size() ) {
					String gwmac = value.getValues().keySet().stream().findFirst().get();
					
					IBeaconDistanceSet.IBeaconDistance obj = value.getValues().values().stream().findFirst().get();
					maxTimestamp = obj.getTimestamp();
					
					location = String.format("%s %.02f", gwmac, obj.getDistance());
				} else {
					int size = value.getValues().size();
					double[][] positions = new double[size][2];
					double[] distances = new double[size];
					int index = 0;
					Iterator<String> it = value.getValues().keySet().iterator();					

					while ( it.hasNext() ) {
						String gwmac = it.next();
						String positionJson = null;
						if ( "AC233FC01253".equals(gwmac)) {
							positionJson = "7.0,3.1";
						}
						if ( "AC233FC012BC".equals(gwmac)) {
							positionJson = "7.0,6.0";
						}
						if ( "AC233FC011E1".equals(gwmac)) {
							positionJson = "2.0,7.0";
						}
						
						if ( null == positionJson ) 
							return null;

						IBeaconDistanceSet.IBeaconDistance obj = value.getValues().get(gwmac);
							if ( maxTimestamp < obj.getTimestamp() )
								maxTimestamp = obj.getTimestamp();
							String[] pos = positionJson.split(",");
							try {
							positions[index][0] = Double.parseDouble(pos[0]);
							positions[index][1] = Double.parseDouble(pos[1]);
							} catch ( Exception e) {
								return null;
							}
							distances[index] = obj.getDistance();
							index++;
						
					}
					try {
						NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(new TrilaterationFunction(positions, distances), 
								new LevenbergMarquardtOptimizer());
						Optimum optimum = solver.solve();
						double[] pt = optimum.getPoint().toArray();
						if ( null != pt )
							location = String.format("%.02f, %.02f", pt[0], pt[1]);
						else 
							location = "pt is null";
					} catch ( Exception e) {
						location = String.format("%s", e.toString());
					}

				}
				loc.setTimestamp(Instant.ofEpochMilli(maxTimestamp).toString());
				loc.setLocation(location);
				return loc;
			}
			
		});
		
		
		 DataStream<InfluxDBPoint> dataLocStream = bleLocStream.map(
				 new RichMapFunction<IBeaconLocation, InfluxDBPoint>() {
					private static final long serialVersionUID = 1L;
					@Override
					public InfluxDBPoint map(IBeaconLocation value) throws Exception {
						long timestamp = System.currentTimeMillis();
						InfluxDBPoint point = new InfluxDBPoint("ibeacondistance", timestamp);
						point.getTags().put("mac", value.getMac());
						point.getTags().put("blename", value.getBleName());
						point.getTags().put("blename", value.getBleName());
						point.getFields().put("ibeaconmajor", value.getIBeaconMajor());
						point.getFields().put("ibeaconminor", value.getIBeaconMinor());
						point.getFields().put("location", value.getLocation());
						
						return point;
					}				 
				 });


		 dataLocStream.addSink(new InfluxDBSink(influxDBConfig));		
		
//		FlinkKafkaProducer<IBeaconLocation> producer1 = new FlinkKafkaProducer<IBeaconLocation>("localhost:9092", "distance", 
//				new IBeaconLocationSerializationSchema());
//		producer1.setWriteTimestampToKafka(true);
//		
//		bleLocStream.addSink(producer1);
		
   	 	env.execute("flinkRssiDemo");
	
	}

}
