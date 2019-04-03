package com.example.demo;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.PostConstruct;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ScheduledTasks {

	private Random random = new Random();
	private RestTemplate restTemplate;
	
	@PostConstruct
	public void init() {
		restTemplate = new RestTemplate();
		//random.ints(0, 99);
	}
	
	@Scheduled(fixedRate = 1000)
    public void reportCurrentTime() {
		List<MinewDataDao> set = new ArrayList<MinewDataDao>();
		//Gateway
		MinewDataDao gwdao = new MinewDataDao();
		gwdao.setTimestamp(Instant.now().toString());
		gwdao.setType("Gateway");
		gwdao.setMac("GWMAC1");
		gwdao.setGatewayFree(100);
		gwdao.setGatewayLoad(10.0F);
		set.add(gwdao);
		
		MinewDataDao dao = new MinewDataDao();
		dao.setTimestamp(Instant.now().toString());
		dao.setBattery(100);
		dao.setBleName("BleName1");
		dao.setIbeaconMajor(10032);
		dao.setIbeaconMinor(240121);
		dao.setIbeaconTxPower(-54);
		dao.setIbeaconUuid("2429asdf1");
		dao.setMac("MAC01");
		dao.setType("iBeacon");
		dao.setRssi(random.nextInt(100) * -1);
		set.add(dao);
		
		dao = new MinewDataDao();
		dao.setTimestamp(Instant.now().toString());
		dao.setBattery(100);
		dao.setBleName("BleName2");
		dao.setIbeaconMajor(2424);
		dao.setIbeaconMinor(242);
		dao.setIbeaconTxPower(-54);
		dao.setIbeaconUuid("2429asdf2");
		dao.setMac("MAC02");
		dao.setType("iBeacon");
		dao.setRssi(random.nextInt(100) * -1);
		set.add(dao);
		
//	    HttpEntity<List<MinewDataDao>> param= new HttpEntity(set);
		restTemplate.postForObject("http://localhost:8080/", set, ResponseEntity.class);
		
    }
	
	
	
	@Scheduled(fixedRate = 1000)
    public void reportCurrentTime1() {
		List<MinewDataDao> set = new ArrayList<MinewDataDao>();
		//Gateway
		MinewDataDao gwdao = new MinewDataDao();
		gwdao.setTimestamp(Instant.now().toString());
		gwdao.setType("Gateway");
		gwdao.setMac("GWMAC2");
		gwdao.setGatewayFree(100);
		gwdao.setGatewayLoad(10.0F);
		set.add(gwdao);
		
		MinewDataDao dao = new MinewDataDao();
		dao.setTimestamp(Instant.now().toString());
		dao.setBattery(100);
		dao.setBleName("BleName1");
		dao.setIbeaconMajor(10032);
		dao.setIbeaconMinor(240121);
		dao.setIbeaconTxPower(-54);
		dao.setIbeaconUuid("2429asdf1");
		dao.setMac("MAC01");
		dao.setType("iBeacon");
		dao.setRssi(random.nextInt(100) * -1);
		set.add(dao);
		
		dao = new MinewDataDao();
		dao.setTimestamp(Instant.now().toString());
		dao.setBattery(100);
		dao.setBleName("BleName2");
		dao.setIbeaconMajor(2424);
		dao.setIbeaconMinor(242);
		dao.setIbeaconTxPower(-54);
		dao.setIbeaconUuid("2429asdf2");
		dao.setMac("MAC02");
		dao.setType("iBeacon");
		dao.setRssi(random.nextInt(100) * -1);
		set.add(dao);
		
//	    HttpEntity<List<MinewDataDao>> param= new HttpEntity(set);
		restTemplate.postForObject("http://localhost:8080/", set, ResponseEntity.class);
		
    }	
}
