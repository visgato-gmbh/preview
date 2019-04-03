package flinkdemo;

import java.io.IOException;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.TypeExtractor;

import com.fasterxml.jackson.databind.ObjectMapper;

public class IBeaconDeserializationSchema implements DeserializationSchema<IBeaconSource> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public TypeInformation<IBeaconSource> getProducedType() {
		return TypeExtractor.getForClass(IBeaconSource.class);
	}

	@Override
	public IBeaconSource deserialize(byte[] message) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(message, IBeaconSource.class);
	}

	@Override
	public boolean isEndOfStream(IBeaconSource nextElement) {
		// TODO Auto-generated method stub
		return false;
	}

}
