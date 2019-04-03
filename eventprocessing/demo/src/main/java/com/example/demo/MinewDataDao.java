package com.example.demo;

import java.io.Serializable;

import lombok.Data;

@Data
public class MinewDataDao implements Serializable {

	private static final long serialVersionUID = 1L;

	private String timestamp;
	private String type;
	private String mac;
	private String bleName;
	private String ibeaconUuid;
	private Integer ibeaconMajor;
	private Integer ibeaconMinor;
	private Integer ibeaconTxPower;
	private Integer rssi;
	private Integer battery;
	private Integer gatewayFree;
	private Float gatewayLoad;
}
