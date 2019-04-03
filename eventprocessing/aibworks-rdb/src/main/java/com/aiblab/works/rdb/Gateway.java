package com.aiblab.works.rdb;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Builder;
import lombok.Data;

@Entity
@Data
@Builder
public class Gateway implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(length=55)
	private String mac;
	private final Double positionX;
	private final Double positionY;
	private final Double coefficentA;
	private final Double coefficentB;
	private final Double coefficentC;
	
}
