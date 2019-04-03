package flinkdemo;

import java.io.IOException;
import java.util.List;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.TypeExtractor;

import com.aiblab.data.minewg1.MinewDataDao;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MinewDeserializationSchema implements DeserializationSchema<List<MinewDataDao>> {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	@Override
	public TypeInformation<List<MinewDataDao>> getProducedType() {
		return TypeExtractor.getForClass((Class<List<MinewDataDao>>)(Class<?>)List.class);
	}

	@Override
	public List<MinewDataDao> deserialize(byte[] message) throws IOException {
		
		ObjectMapper mapper = new ObjectMapper();
		List<MinewDataDao> dataArray = mapper.readValue(message, new TypeReference<List<MinewDataDao>>() {});
		
		return dataArray;
	}

	@Override
	public boolean isEndOfStream(List<MinewDataDao> nextElement) {
		// TODO Auto-generated method stub
		return false;
	}

}
