package flinkdemo.connector.mqtt;

import org.apache.flink.streaming.api.functions.source.RichSourceFunction;
import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

import lombok.AllArgsConstructor;
import lombok.Builder;

@AllArgsConstructor
@Builder
public class MqttSource extends RichSourceFunction<MqttByteArrayMessage> {

	private static final long serialVersionUID = 1239712200030564871L;

	private String host;
	private Integer port;
	private String topic;
	private QoS qos;
	
	public MqttSource(String host, String topic) {
		this(host, 1883, topic, QoS.AT_LEAST_ONCE);		
	}
	
	public MqttSource(String host, int port, String topic) {
		this(host, port, topic, QoS.AT_LEAST_ONCE);	
	}
	
	@Override
	public void run(SourceContext<MqttByteArrayMessage> ctx) throws Exception {
		MQTT mqtt = new MQTT();
		mqtt.setHost(host, port);
		BlockingConnection blockingConnection = mqtt.blockingConnection();
		blockingConnection.connect();
		
		@SuppressWarnings("unused")
		byte[] qoses = blockingConnection.subscribe(new Topic[] {new Topic(topic, qos)});
		
		while(blockingConnection.isConnected()) {
			Message message = blockingConnection.receive();
			MqttByteArrayMessage mmsg = new MqttByteArrayMessage(message.getTopic(), message.getPayload());
			message.ack();
			ctx.collect(mmsg);
		}
		blockingConnection.disconnect();
	}

	@Override
	public void cancel() {

	}

}
