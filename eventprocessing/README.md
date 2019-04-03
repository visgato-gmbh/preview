# eventprocessing

* apache flink 1.7.2 https://flink.apache.org/  
* influxdb 1.7.4 https://www.influxdata.com/  
* mosquitto 1.5.8 https://mosquitto.org/  
* kafka 2.1.1 
* http://www.geotools.org/
* https://github.com/lemmingapex/Trilateration
* flink mqtt sample : https://github.com/ajiniesta/flink-connector-mqtt  
* rssi sample source(kalman filter) : https://github.com/fgroch/beacon-rssi-resolver  
* GPS data online generator : https://nmeagen.org/  
* https://github.com/jpias/beacon-pfilter-simulation/wiki  
* https://gis.stackexchange.com/questions/66/trilateration-using-3-latitude-longitude-points-and-3-distances
* http://www.tothenew.com/blog/indoor-positioning-systemtrilateration/  


### 개발환경   
os : macOS Mojave  
homebrew : 2.0.4

### 필수 서버 설치 방법  
brew install apache-flink  
brew install influxdb  
brew install mosquitto  
brew install zookeeper
brew install kafka

### apache-flink config  
~~~bash
vi /usr/local/Celler/apache-flink/1.7.2/libexec/conf/flink-conf.yaml  
~~~
~~~ini
taskmanager.numberOfTaskSlots: 4
~~~

### apache-flink run
~~~bash
/usr/local/Celler/apache-flink/1.7.2/libexec/bin/start-cluster.sh
~~~

### influxdb run
~~~bash
brew services start influxdb
~~~

### mosquitto run
~~~bash
brew services start mosquitto
~~~

### kafka run
~~~bash
zkServer start
brew services start kafka
or
kafka-server-start /usr/local/etc/kafka/server.properties
~~~

### sketch  
  
<pre>
|⎻⎻⎻⎻⎻⎻⎻⎻|         |⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻|         |⎻⎻⎻⎻⎻⎻⎻⎻|
| source |  ---->  | transformation |  ---->  | sink   |
|⎽⎽⎽⎽⎽⎽⎽⎽|         |⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽|         |⎽⎽⎽⎽⎽⎽⎽⎽|


GPS 위치 추적
                   |⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻|         |⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻|
|⎻⎻⎻⎻⎻⎻⎻⎻|  ---->  | db object |  ---->  | influxdb sink |
| mqtt   |         |⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽|         |⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽|
| source |
|⎽⎽⎽⎽⎽⎽⎽⎽|  
            ---->  |⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻|
                   | calibration|
                   |⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽|
                         |
                         ˅
                   |⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻| 
                   | slide time  |
                   | window 5 sec|
                   | event 1 sec |
                   |⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽|
                         |
                         ˅
                    |⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻|         |⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻|
                    | calculate |  ---->  | mqtt sink |
                    |⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽|         |⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽|


RSSI 위치 추적

|⎻⎻⎻⎻⎻⎻⎻⎻⎻|
| http    |
| to kafka|
|⎽⎽⎽⎽⎽⎽⎽⎽⎽|
     |
     ˅ 
                   |⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻|       
|⎻⎻⎻⎻⎻⎻⎻⎻|  ---->  | transformation stream |  
| kafka  |         |⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽|       
| source |               |
|⎽⎽⎽⎽⎽⎽⎽⎽|               ˅
                   |⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻|
                   | distance   |
                   |⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽|
                         |
                         ˅
                   |⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻| 
                   | slide time  |
                   | window 5 sec|
                   | event 1 sec |
                   |⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽|
                         |
                         ˅
                    |⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻|         |⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻|
                    | trilateration |  ---->  | infuxdb sink|
                    |⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽|         |⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽|



 
</pre>

### GPS 관련 정보
mqtt topic rule - location/{{nodeId}}  

mqtt data format  
~~~java
long eventTime; // UTC00:00
float latitude;
float longitude;
float altitude;
~~~

위경도 기준 거리 계산  
geotools 이용  http://docs.geotools.org/stable/userguide/library/referencing/calculator.html
~~~xml
<!-- https://mvnrepository.com/artifact/org.geotools/gt-referencing -->
<dependency>
    <groupId>org.geotools</groupId>
    <artifactId>gt-referencing</artifactId>
    <version>21.0</version>
</dependency>
...
<repository>
      <id>maven2-repository.dev.java.net</id>
      <name>Java.net repository</name>
      <url>http://download.java.net/maven/2</url>
</repository>
<repository>
      <id>osgeo</id>
      <name>Open Source Geospatial Foundation Repository</name>
      <url>http://download.osgeo.org/webdav/geotools/</url>
</repository>
<repository>
          <snapshots>
            <enabled>true</enabled>
          </snapshots>
          <id>boundless</id>
          <name>Boundless Maven Repository</name>
          <url>http://repo.boundlessgeo.com/main</url>
</repository>
~~~  

~~~java
double distance = 0.0;
double longitude1 = 127.001768052578;
double latitude1 = 37.58082819908043;
double longitude2 = 127.0014366465068;
double latitude2 = 37.57950507552937;

GeodeticCalculator calc = new GeodeticCalculator(crs);
calc.setStartingGeographicPoint(longitude1, latitude1);
calc.setDestinationGeographicPoint(longitude2, latitude2);

distance = calc.getOrthodromicDistance(); // totalmeters 
double bearing = calc.getAzimuth();

Measure<Double, Length> dist = Measure.valueOf(distance, SI.METER);
System.out.println(dist.doubleValue(SI.KILOMETER) + " Km");
System.out.println(dist.doubleValue(NonSI.MILE) + " miles");
System.out.println("Bearing " + bearing + " degrees");
~~~

속도 계산식 
~~~java
public double kmph_to_mps(double kmph) {
      return 0.277778 * kmph;
}
public double mps_to_kmph(double mps) {
      return 3.6 * mps;
}  

public double speedmps(double milliseconds, double totalmeters) {
      return totalmeters / (milliseconds * 1000);
}

public double speedkmph(double milliseconds, double totalmeters) {
      return mps_to_kmph(speedmps(milliseconds, totalmeters));
}
~~~

RSSI 거리 계산  
~~~java
// coefficentA, coefficentB, coefficentC 은 보정계수로 gateway장치의 특성에 따라 고정
// 아래 coefficent는 nexus폰 기준(?)
// rpi embeded anenna
// coefficientA = 0.42093; coefficientB = 6.9476; coefficientC = 0.54992;
int rssi, txpower;
double ratio = rssi * 1.0 / txpower;
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
~~~

RSSI kalman filter 적용  


삼각측정을 통한 위치 추적  
~~~xml
<dependency>
    <groupId>com.lemmingapex.trilateration</groupId>
    <artifactId>trilateration</artifactId>
    <version>1.0.2</version>
</dependency>
~~~

~~~java
double[][] positions = new double[][] { { 5.0, -6.0 }, { 13.0, -15.0 }, { 21.0, -3.0 }, { 12.4, -21.2 } };
double[] distances = new double[] { 8.06, 13.97, 23.32, 15.31 };

NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(new TrilaterationFunction(positions, distances), new LevenbergMarquardtOptimizer());
Optimum optimum = solver.solve();

// the answer
double[] centroid = optimum.getPoint().toArray();

// error and geometry information; may throw SingularMatrixException depending the threshold argument provided
RealVector standardDeviation = optimum.getSigma(0);
RealMatrix covarianceMatrix = optimum.getCovariances(0);
~~~

### flink code example
~~~java
final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
// TimeCharacteristic.ProcessingTime
DataStream<List<BleJsonSourceClass> sourceStream = env.addSource(
 new FlinkKafkaConsumer<BleJsonSourceClass>("minew", new CustomDeserializationSchema(), props) );

sourceStream.flatMap(new RichFlatMapFunction<List<BleJsonSourceClass>>() {
      @Override
	public void flatMap(List<BleJsonSourceClass> value, Collector<IBeaconSource> out) throws Exception {
            // find gateway json
            String gatewayMac;
            ...
            for ( BleJsonSourceClass json : value ) {
                  IBeaconSource target = new IBeaconSource();
                  target.setTimestamp(ZonedDateTime.parse(value.getTimestamp()).toInstant().toEpochMilli());
                  target.setGatewayMac(gatewayMac);
                  double ratio = value.getRssi() * 1.0 / value.getIbeaconTxPower();
			double distance = -1;
			if ( ratio < 1.0 ) 
			      distance = Math.pow(ratio, 10);
			else {
                        double coefficentA = 0.89976;
                        double coefficentB = 7.7095;
                        double coefficentC = 0.111;
                        distance = coefficentA * Math.pow(ratio, coefficentB) + coefficentC;
                  }
                  target.setDistance(distance);
                  ...
                  out.collect( convert(json) );
            }             
      }
}).assignTimestampsAndWatermarks(new AscendingTimestampExtractor<BleJsonSourceClass>() {
      @Override
      public long extractAscendingTimestamp(PojoClass element) {
            return element.getTimestamp();
      }			
}).keyBy("mac")
.timeWindow(Time.seconds(5), Time.seconds(1)).apply(new RichWindowFunction<IBeaconSource, BeaconLocations, Tuple, TimeWindow) {
      @Override
      public void apply(Tuple key, TimeWindow window, Iterable<IBeaconSource> input,
            Collector<BeaconLocations> out) throws Exception {
                  // beacon 기준으로 5초간 수집된 gateway와의 거리 정보 수집
      }
}).map(new RichMapFunction<BeaconLocations, IBeaconLocation>() {
      @Override
	public IBeaconLocation map(BeaconLocations value) throws Exception {
            NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(new TrilaterationFunction(positions, distances), 
		new LevenbergMarquardtOptimizer());
		Optimum optimum = solver.solve();
		double[] pt = optimum.getPoint().toArray();
            return make(pt);
      }
}).addSink(new InfluxDBSink(InfluxDBConfig.builder().url(influxdbUrl)
			.username(influxUser).password(influxPwd)
		 	.database("db_flink_test").database("db_flink_test").build())));

~~~

### TODO
jdbc또는 redis에서 gateway의 위치 정보, gateway의 coefficent 정보를 datastream에서 이용할 수 있는 방법 찾기   
kalman filter의 적용을 위한 이전 gateway,beacon별 rssi보정값 저장및 재사용 방법 찾기  
flink는 외부 자원을 수집하여, datasteam 으로 만들고, datastream과 datastream간의 join, connect등으로 통해
데이터 가공하는 것으로 보임.  
이 때, 외부 자원의 변경에 대한 datastream 변경 처리 방안과 datastream 연동 방안을 찾아야 함.  
