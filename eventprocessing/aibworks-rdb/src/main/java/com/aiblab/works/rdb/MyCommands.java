package com.aiblab.works.rdb;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class MyCommands {

	@Autowired GatewayRepository gatewayRepository;
	
	@ShellMethod("Show gateways")
	public List<Gateway> getAll() {
		List<Gateway> target = new ArrayList<Gateway>();		
		gatewayRepository.findAll().forEach(target::add);
		return target;
	}
	
	@ShellMethod("Add Gateway")
	public Gateway add(@ShellOption({"-m", "--mac"}) String mac, 
			@ShellOption({"-p", "--pos"}) String position, 
			@ShellOption(defaultValue="0.89976,7.7095,0.111") String coefficent) {
		
		String[] pos = position.split(",");
		if ( pos.length != 2 )
			return null;
		String[] coef = coefficent.split(",");
		if ( coef.length != 2 )
			return null;
		try {
			Double posx = Double.parseDouble(pos[0]);
			Double posy = Double.parseDouble(pos[1]);
			Double coefA = Double.parseDouble(coef[0]);
			Double coefB = Double.parseDouble(coef[0]);
			Double coefC = Double.parseDouble(coef[0]);
			
			Gateway obj = Gateway.builder().mac(mac).positionX(posx).positionY(posy).coefficentA(coefA).coefficentB(coefB).coefficentC(coefC).build();
			return gatewayRepository.save(obj);			
		} catch ( Exception e) {
			System.out.println(e.toString());
		}
		
		return null;		
	}
	
	
}
