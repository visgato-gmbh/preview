package flinkdemo.connector.mqtt;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MqttSink extends RichSinkFunction<MqttByteArrayMessage> {

	private static final long serialVersionUID = -7344164287387078018L;

	@NonNull private String host;
	@NonNull private Integer port;	
	@NonNull private QoS qos;
	@NonNull private Boolean retain;
	
	private transient BlockingConnection blockingConnection;

	public MqttSink(String host) {
		this(host, 1883, QoS.AT_LEAST_ONCE, false);
	}
	
	@Override
	public void invoke(MqttByteArrayMessage value) throws Exception {
		blockingConnection.publish(value.getTopic(), value.getPayload(), qos, retain);
	}

	@Override
	public void open(Configuration parameters) throws Exception {
		super.open(parameters);
		MQTT mqtt = new MQTT();
		mqtt.setHost(host, port);
		blockingConnection = mqtt.blockingConnection();
		blockingConnection.connect();
	}

	@Override
	public void close() throws Exception {
		super.close();
		blockingConnection.disconnect();
	}

	
}
