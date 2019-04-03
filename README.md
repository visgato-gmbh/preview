# preview
devel server
~~~
192.168.0.210 mac mojave 10.14
brew install influxdb
brew install redis
brew install kafka
brew install apache-flink

influxdb 1.7.4
kafka 2.1.1
redis 5.0.4
apache-flink 1.7.2
zookeeper 3.4.13
~~~

server start command 
~~~bash
zKServer start &
influxd -config /usr/local/etc/influxdb.conf &
redis-server /usr/local/etc/redis.conf &
kafka-server-start /usr/local/etc/kafka/server.properties &
/usr/local/Celler/apache-flink/1.7.2/libexec/start-cluster.sh 

/usr/local/etc/redis.conf
bind 127.0.0.1 ::1 -> comment
~~~


redis gateway:position:{id} 