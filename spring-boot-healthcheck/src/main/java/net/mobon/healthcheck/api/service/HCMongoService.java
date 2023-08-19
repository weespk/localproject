package net.mobon.healthcheck.api.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import net.mobon.healthcheck.api.util.MArrayUtil;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class HCMongoService {
	private static String IP_PREFIX = "10.251.0.";
	private static String MEDIA_URL_PREFIX = "www";
	private static String ADVERTISER_URL_PREFIX = "log";
	private static String ADVERTISER_URL_SUFFIX = ".megadata.co.kr";
	private static String MONGOS_SESSION_CHECK_PATH = "/cronwork/monitoring/mongo_proc_check.log";
	private static String MONGO_RW_STATUS_PATH = "/cronwork/monitoring/mongo_rw_status.txt";
	
	@Value("${telegram.url.mongo}")
    private String telegramUrl;
    
    @Value("${mobon.server.hid.media}")
    private String hIdMedias;
    
    @Value("${mobon.server.hid.advertiser}")
    private String hIdAdvertisers;

    
    @Value("${mobon.server.hid.mongo.rwstatus}")
    private int hIdRwStatus;
    
    @Value("${mobon.mongos.check.cnt}")
    private int mongosCnt;
    
    @Autowired
    private RestTemplate restTemplate;


    public void checkSessions() {
        HttpEntity<String> entity = new HttpEntity<>(new HttpHeaders());
    	try {
	        int[] aHIdMedia = MArrayUtil.toInts(hIdMedias, ",");
	        int[] aIdAdvertiser = MArrayUtil.toInts(hIdAdvertisers, ",");
	        List<String> keyList = new ArrayList<>();
	        Map<String, String> urls = new HashMap<>();
	        
	        for(int hId : aHIdMedia) {
	        	String key = MEDIA_URL_PREFIX+hId;
	        	keyList.add(key);
	        	urls.put(key, "http://" +IP_PREFIX + hId + MONGOS_SESSION_CHECK_PATH);
	        }
	        for(int hId : aIdAdvertiser) {
	        	String key = ADVERTISER_URL_PREFIX +hId;
	        	keyList.add(key);
	        	urls.put(key, "http://" +IP_PREFIX + hId + MONGOS_SESSION_CHECK_PATH);
	        }
	        
	        boolean bWarning = false; 
	        Map<String, Integer> results = new HashMap<>();
	        for (Map.Entry<String, String> entry : urls.entrySet()) {
	        	try {
	        		ResponseEntity<String> response = restTemplate.exchange(entry.getValue(), HttpMethod.GET, entity, String.class);
	                if (HttpStatus.OK.equals(response.getStatusCode())) {
	                	int cnt = NumberUtils.toInt(response.getBody(), 0);
	            		results.put(entry.getKey(), cnt);
	                	if(cnt > mongosCnt) {
	                		bWarning= true;
	                	}
	                }
				} catch (Exception e) {
				  log.error("["+entry.getKey()+"]"+e.getMessage());
				}
	        }
	        
	        
	        if(bWarning) {
	    		StringBuffer buf = new StringBuffer();
	        	buf.append("!!!Mongos 쓰레드 수 " +mongosCnt+ " 이상인 서버 발생!!");
	        	for(String key : keyList) {
	        		buf.append("\n").append(key).append("Mongos 쓰레드 수 => ").append(results.get(key));
	        	}
	        	
	        	restTemplate.exchange(telegramUrl + buf.toString(), HttpMethod.GET, entity, String.class);
	        }
    	} catch (Exception e) {
			log.error(e.getMessage());
		}
    	
    }
    
    public void checkRW() {
    	HttpEntity<String> entity = new HttpEntity<>(new HttpHeaders());
    	String url = "http://" + IP_PREFIX + hIdRwStatus + MONGO_RW_STATUS_PATH;
    	try {
	    	ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
			if (HttpStatus.OK.equals(response.getStatusCode())) {
				String body = response.getBody();
				
					JsonObject json = new JsonParser().parse(body).getAsJsonObject();
					if(json.get("res").getAsBoolean()) {
					} else {
						StringBuffer buf = new StringBuffer();
						buf.append("!!!몽고DB Read/Write 이상 발생!!!").append("\n").append(json.get("msg").getAsString())
						.append("\n").append(json.toString());
						restTemplate.exchange(telegramUrl + buf.toString(), HttpMethod.GET, entity, String.class);
					}

		    } else {		    	
		    	restTemplate.exchange(telegramUrl + "!!!몽고DB Read/Write 상태 확인되지 않습니다.("+MEDIA_URL_PREFIX + hIdRwStatus+")!!!", HttpMethod.GET, entity, String.class);
		    }
    	} catch (Exception e) {
			  log.error(e.getMessage());
		}
    }

}
