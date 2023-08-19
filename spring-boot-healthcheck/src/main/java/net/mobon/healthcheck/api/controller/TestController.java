package net.mobon.healthcheck.api.controller;

import com.google.gson.Gson;
import net.mobon.healthcheck.api.service.BillingService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;

import lombok.extern.slf4j.Slf4j;
import net.mobon.healthcheck.api.service.RebuildService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.ApiOperation;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController
@Controller
public class TestController {

	@Autowired
	private RestTemplate restTemplate;
	@Value("${telegram.url.billingTest}")
	private String telegramUrlBillingTest;

	@Scheduled(cron = "*/5 * * * * *")
	@GetMapping(value = "/testMethod")
	@ResponseBody
	public String testMethod(){
		log.info("START BillingService chkingBatchRuningTime");
		try {
			StringBuffer sbf = new StringBuffer("빌링 - BATCH DELAY\n");
			sbf.append("-- 테스트 발송입니다. --\n");
			boolean chk = false;

//			String []batchRunningTime = {
//					"http://192.168.2.76:8000/batchRunningTime.txt"};

//			for( String row : batchRunningTime ) {
//				String response = restTemplate.getForObject(row, String.class);
//				List<Map> list = new Gson().fromJson(response, ArrayList.class);
//
//				for( Map i : list ) {
//					log.debug("i - {}", i);
//					if( !"OK".equals(i.get("CHKING_STATS").toString()) ) {
//						sbf.append(String.format("마지막 구동시간 - %s", i.get("LAST_EXE_TIME").toString()));
//						chk = true;
//					}
//				}
//			}

			sbf.append("test");
			chk = true;


			if( chk ) {
				log.info("msg - {}", sbf.toString());

				HttpHeaders headers = new HttpHeaders();
				HttpEntity<String> entity = new HttpEntity<>(headers);
				restTemplate.exchange(telegramUrlBillingTest + sbf.toString(), HttpMethod.GET, entity, String.class);
			}
		}catch(Exception e) {
			log.error("chkingBatchRuningTime ", e);
		}
		return "test message";
	}
}
