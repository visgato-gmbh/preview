# eventprocessing

* apache flink 1.7.2 https://flink.apache.org/  
* influxdb 1.7.4 https://www.influxdata.com/  
* mosquitto 1.5.8 https://mosquitto.org/  
* http://www.geotools.org/
* https://github.com/lemmingapex/Trilateration
* flink mqtt sample : https://github.com/ajiniesta/flink-connector-mqtt  
* rssi sample source(kalman filter) : https://github.com/fgroch/beacon-rssi-resolver  
* GPS data online generator : https://nmeagen.org/  
* https://github.com/jpias/beacon-pfilter-simulation/wiki  
* https://gis.stackexchange.com/questions/66/trilateration-using-3-latitude-longitude-points-and-3-distances
* http://www.tothenew.com/blog/indoor-positioning-systemtrilateration/  

~~~xml
<!-- https://mvnrepository.com/artifact/org.geotools/gt-referencing -->
<dependency>
    <groupId>org.geotools</groupId>
    <artifactId>gt-referencing</artifactId>
    <version>21.0</version>
</dependency>
~~~  

### developer environment 
os : macOS Mojave  
homebrew : 2.0.4

### prepare servers
brew install apache-flink  
brew install influxdb  
brew install mosquitto  

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


### sketch  
apache flink stream data flow  
<pre>
|⎻⎻⎻⎻⎻⎻⎻⎻|         |⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻|         |⎻⎻⎻⎻⎻⎻⎻⎻|
| source |  ---->  | transformation |  ---->  | sink   |
|⎽⎽⎽⎽⎽⎽⎽⎽|         |⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽|         |⎽⎽⎽⎽⎽⎽⎽⎽|



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
                   | window 1 min|
                   | event 1 sec |
                   |⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽|
                         |
                         ˅
                    |⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻|         |⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻|
                    | calculate |  ---->  | mqtt sink |
                    |⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽|         |⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽|


 
</pre>