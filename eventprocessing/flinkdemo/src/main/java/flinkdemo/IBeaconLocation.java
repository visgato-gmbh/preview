package flinkdemo;

import java.io.Serializable;

import lombok.Data;

@Data
public class IBeaconLocation implements Serializable {

	private static final long serialVersionUID = 1L;

	private String mac;
	private String timestamp;
	private String bleName;
	private Integer iBeaconMajor;
	private Integer iBeaconMinor;
	private String location;
//	private Double positionX;
//	private Double positionY;
}
