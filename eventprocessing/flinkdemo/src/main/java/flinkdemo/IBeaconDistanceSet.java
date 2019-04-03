package flinkdemo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class IBeaconDistanceSet  implements Serializable {

	private static final long serialVersionUID = 1L;

	@Data
	static class IBeaconDistance implements Serializable {
		private static final long serialVersionUID = 1L;
		private Long timestamp;
		private Double distance;
	}
	
	private String mac;
	private String bleName;
	private Integer iBeaconMajor;
	private Integer iBeaconMinor;
	private Map<String, IBeaconDistance> values = new HashMap<String, IBeaconDistance>();
	
}
