package net.mobon.healthcheck.api.service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Service
@Slf4j
public class OpenRtbKafkaService {

	//TODO 현재는 kakao 방으로 보내고 있는데 나중에 오픈 RTB 방이 생기면 해당 내용을 바꿔 줘야함
	@Value("${telegram.url.kakaodaisy}")
	private String telegramUrl;
    @Value("${telegram.url.kakaodaisy}")
    private String telegramUrlOpenRtb;
    
    
	
    private RestTemplate restTemplate = new RestTemplate();
	
	public void kafkaTopicLagChk() {
		log.info("START OpenRtbService kafkaTopicLagChk");
		try {
			StringBuffer sbf = new StringBuffer("OpenRtb - KAFKA LAG DELAY \n");
			// TODO 각 토픽별 5분 딜레이시 지연된 갯수 확인해서 문제가 없을시 수정 해야함. 아래 적용된 숫자는 빌링에 있는걸 기본으로 산출한 내용임
			// - retry 폴더 쌓이는 지 확인 : DB 문제 확인
			// - errorlog 가 쌓이고 있는지 확인 : 장애 상황 확인
			String[][] info = new String[][] 
					{
						//bid
						{ "OpenRtbBid", "OpenRTBBid_Adfit", "100000" },
						{ "OpenRtbBid", "OpenRTBBid_Google", "100000" },
						{ "OpenRtbBid", "OpenRTBBid_Kakao", "100000" },
						
						//click,view
						{ "OpenRtbClickView", "OpenRTBClick_Adfit", "100000" },
						{ "OpenRtbClickView", "OpenRTBView_Adfit", "100000" },
						{ "OpenRtbClickView", "OpenRTBClick_Google", "100000" },
						{ "OpenRtbClickView", "OpenRTBView_Google", "100000" },
		
						//point
						{ "OpenRtbPoint", "OpenRTBPoint_Adfit", "100000" },
						{ "OpenRtbPoint", "OpenRTBPoint_Google", "100000" },
						
						//conv
						{ "OpenRtbConv", "OpenRTBConversion_Adfit", "10000" },
						{ "OpenRtbConv", "OpenRTBConversion_Google", "10000" },
				
						 
					};
			boolean chk = false;

			for (String[] row : info) {
				ArrayList<HashMap> list = chkingTopicLag(row[0], row[1], Long.parseLong(row[2]));
				if ( list.size() > 0 ) {
					Map map = list.get(0);
					sbf.append(String.format("Topic - %s, Lag Count - %s\n", row[1], map.get(row[1])));
					chk=true;
				}
			}

			if (chk) {
				log.info("msg - {}", sbf.toString());
				
				HttpHeaders headers = new HttpHeaders();
				HttpEntity<String> entity = new HttpEntity<>(headers);
				restTemplate.exchange(telegramUrl + sbf.toString(), HttpMethod.GET, entity, String.class);
			}
		
		}catch(Exception e) {
			log.error("OpenRtbService kafkaTopicLagChk", e);
		}
	}

	private ArrayList chkingTopicLag(String groupName, String topic, long maxLagCnt) {
		
		ArrayList result = new ArrayList();
		try {
			String kafkaGroupSummeryUrl = "http://10.251.0.225:9000/api/status/openrtb-kafka/"+ groupName +"/KF/groupSummary";
			String response = restTemplate.getForObject(kafkaGroupSummeryUrl, String.class);
			
			JsonObject root = new JsonParser().parse(response).getAsJsonObject();
			Gson gson = new Gson();
			for (Entry<String, JsonElement> entry : root.entrySet()) {
				
			    KafkaGroupSummary summery = gson.fromJson(entry.getValue(), KafkaGroupSummary.class);
			    log.debug("lag - {}, entry - {}", summery.getTotalLag(), entry.getKey() );
			    
			    if( topic.equals(entry.getKey()) ) {
				    if( summery.getTotalLag() > maxLagCnt ) {
				    	Map map = new HashMap();
				    	map.put(topic, summery.getTotalLag());
				    	result.add(map);
				    	break;
				    }
			    }
			}
		}catch(Exception e) {
			log.error("err ", e);
		}
		return result;
	}

	public void chkingConsumerQueue() {
		log.info("START OpenRtbService chkingConsumerQueue");
		try {
			StringBuffer sbf = new StringBuffer("OpenRtb - over 1000 QUEUE SIZE \n");
			//sbf.append("-- 테스트 발송입니다. --\n");
			String []kafkaGroupSummeryUrl = 
				{
					"http://10.251.0.215:8001/bid/cron/openrtbConsumerQueueSize.txt",
					"http://10.251.0.215:8001/clickview/cron/openrtbConsumerQueueSize.txt",
					"http://10.251.0.215:8001/conv/cron/openrtbConsumerQueueSize.txt"
				};
			
			boolean chk = false;
			for( String row : kafkaGroupSummeryUrl ) {
				try {
					log.debug("row - {}", row);
					String response = restTemplate.getForObject(row, String.class);
					
					JsonObject root = new JsonParser().parse(response).getAsJsonObject();
					for (Entry<String, JsonElement> entry : root.entrySet()) {
						String queueName = entry.getKey().toString();
						int queueSize = Integer.parseInt(entry.getValue().toString());
						
						if( queueSize > 1000 ) {
							sbf.append(String.format("%s - [%s]\n", queueName, queueSize));
							chk = true;
						}
					}
				}catch(Exception e) {
					log.error("chkingConsumerQuee ", e);
				}
			}
			
			if( chk ) {
				log.info("msg - {}", sbf.toString());
				
				HttpHeaders headers = new HttpHeaders();
				HttpEntity<String> entity = new HttpEntity<>(headers);
				restTemplate.exchange(telegramUrlOpenRtb + sbf.toString(), HttpMethod.GET, entity, String.class);
			}
		
		}catch(Exception e) {
			log.error("check OpenRtb Consumer Error => {}", e);
		}
	}

	 
	
	class KafkaGroupSummary {
		long totalLag;
		int percentageCovered;
		List<String> partitionOffsets;
		List<String> owners;
		
		public long getTotalLag() {
			return totalLag;
		}
		public void setTotalLag(long totalLag) {
			this.totalLag = totalLag;
		}
		public int getPercentageCovered() {
			return percentageCovered;
		}
		public void setPercentageCovered(int percentageCovered) {
			this.percentageCovered = percentageCovered;
		}
		public List<String> getPartitionOffsets() {
			return partitionOffsets;
		}
		public void setPartitionOffsets(List<String> partitionOffsets) {
			this.partitionOffsets = partitionOffsets;
		}
		public List<String> getOwners() {
			return owners;
		}
		public void setOwners(List<String> owners) {
			this.owners = owners;
		}
	}
	 
}
