package flinkdemo;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Comparator;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple1;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.RichWindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.geotools.referencing.GeodeticCalculator;

import flinkdemo.connector.influxdb.InfluxDBConfig;
import flinkdemo.connector.influxdb.InfluxDBPoint;
import flinkdemo.connector.influxdb.InfluxDBSink;
import flinkdemo.connector.mqtt.MqttByteArrayMessage;
import flinkdemo.connector.mqtt.MqttSink;
import flinkdemo.connector.mqtt.MqttSource;

public class GpsApp {
	
	public static void main(String[] args) throws Exception {
		final ParameterTool params = ParameterTool.fromArgs(args);
		
		String mqttHost = "localhost";
		Integer mqttPort = 1883;
		String influxdbUrl = "http://localhost:8086";
		String influxUser = "";
		String influxPwd = "";
		String sourceTopic = "location/#";
		
		if ( params.has("mqtthost") )
			mqttHost = params.get("mqtthost");
		if ( params.has("mqttport"))
			mqttPort = params.getInt("mqttport");
		
		
		// set up the execution environment
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		env.getConfig().setGlobalJobParameters(params);
		
		DataStream<MqttByteArrayMessage> sourceStream = env.addSource(
				new MqttSource(mqttHost, mqttPort, sourceTopic));
		
		
		DataStream<Tuple4<String, Long, Double, Double>> locationStream = sourceStream.map(
				new RichMapFunction<MqttByteArrayMessage, Tuple4<String, Long, Double, Double>>() {
					private static final long serialVersionUID = 1L;
			@Override
			public Tuple4<String, Long, Double, Double> map(MqttByteArrayMessage value) throws Exception {
				if ( null == value.getPayload())
					return null;
				ByteBuffer payload = ByteBuffer.wrap(value.getPayload());
				if ( payload.remaining() != Long.BYTES + Float.BYTES + Float.BYTES )
					return null;
				Long eventTime = payload.getLong();
				Double latitude = payload.getDouble();
				Double longitude = payload.getDouble();
				if ( eventTime <= 0) 
					return null;				
				Tuple4<String, Long, Double, Double> parsed = new Tuple4<String, Long, Double, Double>(value.getTopic(),
						eventTime, latitude, longitude);
				return parsed;
			}			
		});
		
		 DataStream<InfluxDBPoint> dataStream = locationStream.map(
				 new RichMapFunction<Tuple4<String, Long, Double, Double>, InfluxDBPoint>() {
					private static final long serialVersionUID = 1L;
					@Override
					public InfluxDBPoint map(Tuple4<String, Long, Double, Double> value) throws Exception {
						long timestamp = System.currentTimeMillis();
						InfluxDBPoint point = new InfluxDBPoint("location", timestamp);
						point.getTags().put("node", value.f0);
						point.getFields().put("eventtime", value.f1);
						point.getFields().put("latitude", value.f2);
						point.getFields().put("longitude", value.f3);
						
						return point;
					}				 
				 });

		 
		 DataStream<Tuple4<String, Long, Double, Double>> speedStream = locationStream.keyBy(0).timeWindow(Time.seconds(5), Time.seconds(1))
		 .apply(
				 new RichWindowFunction<Tuple4<String, Long, Double, Double>, 
				 Tuple4<String, Long, Double, Double>, Tuple, TimeWindow>() {

					private static final long serialVersionUID = 1L;
		
					@SuppressWarnings("unchecked")
					@Override
					public void apply(Tuple key, TimeWindow window, Iterable<Tuple4<String, Long, Double, Double>> input,
							Collector<Tuple4<String, Long, Double, Double>> out) throws Exception {
						Long minTime = Long.MAX_VALUE;
						Long maxTime = Long.MIN_VALUE;
						Double lat1 = null, lon1 = null, lat2 = null, lon2 = null;
						
						for ( Tuple4<String, Long, Double, Double> in : input ) {
							if ( in.f1 < minTime ) {
								minTime = in.f1;
								lat1 = in.f2; lon1 = in.f3;
							}
							if ( in.f1 > maxTime ) {
								maxTime = in.f1;
								lat2 = in.f2; lon2 = in.f3;
							}
						}
						Double distance, kmph;
						Long milliseconds = maxTime - minTime;
						if ( lon1 != null && lat1 != null && lon2 != null && lat2 != null) {
							GeodeticCalculator calc = new GeodeticCalculator();
							calc.setStartingGeographicPoint(lon1, lat1);
							calc.setDestinationGeographicPoint(lon2, lat2);
							distance = calc.getOrthodromicDistance();
							
							kmph = SpedCalculator.speedkmph(milliseconds, distance);
							
							out.collect(new Tuple4<String, Long, Double, Double>(
									((Tuple1<String>)key).f0, 
									milliseconds,
									distance, kmph
									));
							}
					}			 
				 } );
		 
		 InfluxDBConfig influxDBConfig = InfluxDBConfig.builder().url(influxdbUrl)
			.username(influxUser).password(influxPwd)
		 	.database("db_flink_test").database("db_flink_test").build();

	     dataStream.addSink(new InfluxDBSink(influxDBConfig));		
	     
	     speedStream.map(new RichMapFunction<Tuple4<String,Long, Double, Double>, MqttByteArrayMessage>() {

			private static final long serialVersionUID = 1L;

			@Override
			public MqttByteArrayMessage map(Tuple4<String, Long, Double, Double> value) throws Exception {
				
				return new MqttByteArrayMessage("speed", value.toString().getBytes());
			}
	    	 
	     }).addSink(new MqttSink("localhost"));
	     
	     
    	 env.execute("flinkDemo");
	}

}
