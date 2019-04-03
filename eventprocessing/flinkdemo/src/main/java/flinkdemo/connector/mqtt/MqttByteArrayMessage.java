package flinkdemo.connector.mqtt;

import java.io.Serializable;

import lombok.Data;

@Data
public class MqttByteArrayMessage implements Serializable {

	private static final long serialVersionUID = -310737515047585538L;

	private final String topic;
	private final byte[] payload;
	
}
