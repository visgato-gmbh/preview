package flinkdemo;

import org.apache.flink.api.common.serialization.SerializationSchema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GatewaySerializationSchema implements SerializationSchema<GatewaySource> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public byte[] serialize(GatewaySource element) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsBytes(element);
		} catch (JsonProcessingException e) {
			return null;
		}
	}

}
