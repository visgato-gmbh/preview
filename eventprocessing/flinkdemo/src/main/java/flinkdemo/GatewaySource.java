package flinkdemo;

import java.io.Serializable;

import lombok.Data;

@Data
public class GatewaySource implements Serializable {

	private static final long serialVersionUID = 1L;

	private String gatewayMac;
	private String timestamp;
	private Integer gatewayFree;
	private Float gatewayLoad;
}
