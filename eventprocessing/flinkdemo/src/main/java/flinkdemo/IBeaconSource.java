package flinkdemo;

import java.io.Serializable;

import lombok.Data;

@Data
public class IBeaconSource implements Serializable {

	private static final long serialVersionUID = 1L;

	private String gatewayMac;
	private String mac;
	private Long timestamp;
	private String bleName;
	private Integer iBeaconMajor;
	private Integer iBeaconMinor;
	private Integer iBeaconTxPower;
	private Integer rssi;
	private Integer battery;
	private Double distance;
}
