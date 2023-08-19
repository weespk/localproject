package net.mobon.healthcheck.api.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BillingService {
	@Value("${telegram.url.billingctr}")
	private String telegramUrlBillingCTR;

	@Value("${telegram.url.billing}")
	private String telegramUrlBilling;

//    @Value("${telegram.url.billingTest}")
//    private String telegramUrlBillingTest;

	@Autowired
	private RestTemplate restTemplate;

	private String kafkaUrl = "http://192.168.2.77:9000";

	public void kafkaTopicLagChk() {
		log.info("START BillingService kafkaTopicLagChk");
		try {
			StringBuffer sbf = new StringBuffer("빌링 - KAFKA LAG DELAY \n");
			String[][] info = new String[][] {	// 각 토픽별 5분 딜레이시 지연된 갯수
					{ "group-billing", "ClickViewData", "4000000" },
					{ "group-billing-branch", "ClickViewData", "1000000" },
					{ "group-billing2", "ConversionData", "10000" },
					{ "group-billing2", "ShopInfoData", "500000" },
					{ "group-billing2", "ShopStatsInfoData", "500000" },
					{ "group-nativeNonAdReport", "NativeNonAdReport", "30600" },
					{ "group-billing-branchAction", "ClickViewData", "1000000" },
					{ "group-billing-SubjectCopy2", "ClickViewData", "1000000" },
					{ "group-billing-openrtb", "OpenRTBViewData", "2000" },
					{ "group-billing-viewclicklog", "ClickViewData", "100000" },
					{ "group-billing-brandingConv", "ConversionData", "10000" },
					{ "group-billing2", "ExternalData", "1500000" } };
			boolean chk = false;

			for (String[] row : info) {
				ArrayList<HashMap> list = chkingTopicLag(row[0], row[1], Long.parseLong(row[2]));
				if ( list.size() > 0 ) {
					Map map = list.get(0);
					sbf.append(String.format("%s, %s\n", row[0], map.get(row[1])));
					chk=true;
				}
			}

			if (chk) {
				log.info("msg - {}", sbf.toString());

				HttpHeaders headers = new HttpHeaders();
				HttpEntity<String> entity = new HttpEntity<>(headers);
				restTemplate.exchange(telegramUrlBilling + sbf.toString(), HttpMethod.GET, entity, String.class);
			}

		}catch(Exception e) {
			log.error("BillingService kafkaTopicLagChk", e);
		}
	}

	private ArrayList chkingTopicLag(String groupName, String topic, long maxLagCnt) {
		ArrayList result = new ArrayList();
		try {
			String kafkaGroupSummeryUrl = kafkaUrl+"/api/status/kafka/"+ groupName +"/KF/groupSummary";
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

	public void chkingBatchRuningTime() {
		log.info("START BillingService chkingBatchRuningTime");
		try {
			StringBuffer sbf = new StringBuffer("빌링 - BATCH DELAY\n");
//			sbf.append("-- 테스트 발송입니다. --\n");
			boolean chk = false;

			String []batchRunningTime = {
					"http://192.168.2.76:8000/batchRunningTime.txt"};

			for( String row : batchRunningTime ) {
				String response = restTemplate.getForObject(row, String.class);
				List<Map> list = new Gson().fromJson(response, ArrayList.class);

				for( Map i : list ) {
					log.debug("i - {}", i);
					if( !"OK".equals(i.get("CHKING_STATS").toString()) ) {
						sbf.append(String.format("마지막 구동시간 - %s", i.get("LAST_EXE_TIME").toString()));
						chk = true;
					}
				}
			}

			if( chk ) {
				log.info("msg - {}", sbf.toString());

				HttpHeaders headers = new HttpHeaders();
				HttpEntity<String> entity = new HttpEntity<>(headers);
				restTemplate.exchange(telegramUrlBilling + sbf.toString(), HttpMethod.GET, entity, String.class);
			}
		}catch(Exception e) {
			log.error("chkingBatchRuningTime ", e);
		}
	}

	public void chkingReportCtr() {
		log.info("START BillingService chkingReportCtr");
		try {
			StringBuffer sbf = new StringBuffer("빌링 - CTR CHECKER\n");
//			sbf.append("-- 테스트 발송입니다. ---\n");
			boolean chk = false;

			String []reportCtr = {
					"http://192.168.2.76:8000/reportCtr.txt"};

			for( String row : reportCtr ) {
				String response = restTemplate.getForObject(row, String.class);
				List<Map> list = new Gson().fromJson(response, ArrayList.class);

				for( Map i : list ) {
					log.debug("i - {}", i);
					if( "alam1".equals(i.get("is_alam")) && !"none".equals(i.get("alam")) ) {
						sbf.append(String.format("[누적 - %s%% (전일대비 80%%~120%% 초과)]\n", i.get("ctr_pct")));
						sbf.append(String.format("%s - %s%% (%s / %s)\n", i.get("bdate"), i.get("ctr"), i.get("clickcnt"), i.get("viewcnt2") ));
						sbf.append(String.format("%s - %s%% (%s / %s)\n", i.get("tdate"), i.get("this_ctr"), i.get("this_clickcnt"), i.get("this_viewcnt2") ));
						chk=true;
					}
					if( "alam2".equals(i.get("is_alam")) && !"none".equals(i.get("alam")) ) {
						sbf.append(String.format("[%s시 - %s%% (전일대비 70%%~130%% 초과)]\n", i.get("hh"), i.get("ctr_pct")));
						sbf.append(String.format("%s - %s%% (%s / %s)\n", i.get("bdate"), i.get("ctr"), i.get("clickcnt"), i.get("viewcnt2") ));
						sbf.append(String.format("%s - %s%% (%s / %s)\n", i.get("tdate"), i.get("this_ctr"), i.get("this_clickcnt"), i.get("this_viewcnt2") ));
						chk=true;
					}
				}
			}

			if( chk ) {
				log.info("msg - {}", sbf.toString());

				HttpHeaders headers = new HttpHeaders();
				HttpEntity<String> entity = new HttpEntity<>(headers);
				restTemplate.exchange(telegramUrlBilling + sbf.toString(), HttpMethod.GET, entity, String.class);
				restTemplate.exchange(telegramUrlBillingCTR + sbf.toString(), HttpMethod.GET, entity, String.class);
			}

		}catch(Exception e) {
			log.error("chkingReportCtr ", e);
		}
	}

	public void chkingZeroViewClickConv() {
		log.info("START BillingService ChkingZeroViewClickConv");
		try {
			StringBuffer sbf = new StringBuffer("빌링 - Chking ZERO\n");
//			sbf.append("-- 테스트 발송입니다. ---\n");
			boolean chk = false;

			String []reportCtr = {
					"http://192.168.2.76:8000/ChkingZeroViewClickConv.txt"};

			for( String row : reportCtr ) {
				String response= restTemplate.getForObject(row, String.class);
				List<Map> list= new Gson().fromJson(response, ArrayList.class);

				for( Map i : list ) {
					if(i.get("CNT").equals("0.0") || i.get("CNT").equals("0")) {
						sbf.append(String.format("%s > %s\n", i.get("GG"), i.get("CNT")));
						chk= true;
					}
				}
			}

			if( chk ) {
				log.info("msg - {}", sbf.toString());
				HttpHeaders headers = new HttpHeaders();
				HttpEntity<String> entity = new HttpEntity<>(headers);
				restTemplate.exchange(telegramUrlBilling + sbf.toString(), HttpMethod.GET, entity, String.class);
			}

		}catch(Exception e) {
			log.error("chkingReportCtr ", e);
		}
	}

//	public void chkingConsumerQueue() {
//		log.info("START BillingService chkingConsumerQueue");
//		try {
//			StringBuffer sbf = new StringBuffer("빌링 - over 1000 QUEUE SIZE \n");
//			//sbf.append("-- 테스트 발송입니다. --\n");
//			String []kafkaGroupSummeryUrl = {
//					"http://192.168.2.75:8080/consumerInfo",
////					"http://192.168.2.77:8000/consumerQueueSize.txt",
////					"http://192.168.2.78:8000/ConsumerBranchAction.txt"
//					};
//
//			boolean chk = false;
//			for( String row : kafkaGroupSummeryUrl ) {
//				try {
//					log.debug("row - {}", row);
//					String response = restTemplate.getForObject(row, String.class);
//
//					JsonObject root = new JsonParser().parse(response).getAsJsonObject();
//					for (Entry<String, JsonElement> entry : root.entrySet()) {
//						String queueName = entry.getKey().toString();
//						int queueSize = Integer.parseInt(entry.getValue().toString());
//
//						if( queueSize > 1000 ) {
//							sbf.append(String.format("%s - [%s]\n", queueName, queueSize));
//							chk = true;
//						}
//					}
//				}catch(Exception e) {
//					log.error("chkingConsumerQuee ", e);
//				}
//			}
//
//			if( chk ) {
//				log.info("msg - {}", sbf.toString());
//
//				HttpHeaders headers = new HttpHeaders();
//				HttpEntity<String> entity = new HttpEntity<>(headers);
//				restTemplate.exchange(telegramUrlBilling + sbf.toString(), HttpMethod.GET, entity, String.class);
//			}
//
//		}catch(Exception e) {
//			log.error("chkingConsumerQueue ", e);
//		}
//	}

	public void chkingBeforeHourData() {
		log.info("START BillingService chkingBeforeHourData");
		try {
			StringBuffer sbf = new StringBuffer("빌링 - chkingBeforeHourData \n");
			//sbf.append("-- 테스트 발송입니다. --\n");
			String []kafkaGroupSummeryUrl = {
					"http://192.168.2.76:8000/chkingBeforeHourData.txt"
			};

			boolean chk = false;
			for( String row : kafkaGroupSummeryUrl ) {
				try {
					log.debug("row - {}", row);
					String response = restTemplate.getForObject(row, String.class);

					BeforeHourData[] array = new Gson().fromJson(response, BeforeHourData[].class);
					List<BeforeHourData> root = Arrays.asList(array);

					for(BeforeHourData row2 : root) {
						int CNT= row2.getCNT();
						if (CNT>0) {
							sbf.append(String.format("SITE_CODE:%s, CNT:%s\n", row2.getSITE_CODE(), row2.getCNT() ));
							chk = true;
						}
					}
				}catch(Exception e) {
					log.error("chkingMediaChrg ", e);
				}
			}

			if( chk ) {
				log.info("msg - {}", sbf.toString());

				HttpHeaders headers = new HttpHeaders();
				HttpEntity<String> entity = new HttpEntity<>(headers);
				restTemplate.exchange(telegramUrlBilling + sbf.toString(), HttpMethod.GET, entity, String.class);
			}

		}catch(Exception e) {
			log.error("chkingConsumerQueue ", e);
		}
	}

	public void chkingunderReplicatedPartitions() {
		StringBuffer sbf = new StringBuffer("빌링 - ISR GROUP \n");
		boolean chk = false;
		try {
			String url = kafkaUrl + "/api/status/kafka/ClickViewData/underReplicatedPartitions";
//			String url = "http://192.168.136.130:9000/api/status/192/ClickViewData/underReplicatedPartitions";
			String response = restTemplate.getForObject(url, String.class);

			JsonObject root = new JsonParser().parse(response).getAsJsonObject();
			underReplicatedPartitions data = new Gson().fromJson(root, underReplicatedPartitions.class);
			if( data.getUnderReplicatedPartitions().size()>0 ) {
				sbf.append(String.format("underReplicated - %s", data.getUnderReplicatedPartitions()));
				chk = true;
			}

			if (chk) {
				log.info("msg - {}", sbf.toString());

				HttpHeaders headers = new HttpHeaders();
				HttpEntity<String> entity = new HttpEntity<>(headers);
				restTemplate.exchange(telegramUrlBilling + sbf.toString(), HttpMethod.GET, entity, String.class);
			}
		}catch(Exception e) {
			log.error("err ", e);
		}
	}

	public void chkingConsumerRetryFile() {
		StringBuffer sbf = new StringBuffer("빌링 - RETRY FILE \n");
		boolean chk = false;
		try {
			String[][] info = new String[][] {
					{ "branchAction", "http://192.168.2.78:8070/consumerInfo", "" }
					,{ "branchConv", "http://192.168.2.78:8071/consumerInfo", "" }
					,{ "clickview", "http://192.168.2.112:8070/consumerInfo", "" }
					,{ "clickviewHA", "http://192.168.2.79:8070/consumerInfo", "" }
//				,{ "branch", "http://192.168.2.75:8070/consumerInfo", "" }
					,{ "branch2", "http://192.168.2.117:8070/consumerInfo", "" }
					,{ "subjectCopy", "http://192.168.2.70:8075/consumerInfo", "" }
					,{ "subjectCopyHA", "http://192.168.2.74:8075/consumerInfo", "" }
			};

			for (String[] row : info) {

				try {
					String response = restTemplate.getForObject(row[1], String.class);

					JsonObject root = new JsonParser().parse(response).getAsJsonObject();
					consumerInfo data= new Gson().fromJson(root, consumerInfo.class);

					if( data.getRetry().fileLength>4 ) {
						sbf.append(String.format("%s > %s", row[0], data.getRetry().fileLength ));
						chk = true;
					}
				}catch(Exception e) {
				}
			}

			if (chk) {
				log.info("msg - {}", sbf.toString());

				HttpHeaders headers = new HttpHeaders();
				HttpEntity<String> entity = new HttpEntity<>(headers);
				restTemplate.exchange(telegramUrlBilling + sbf.toString(), HttpMethod.GET, entity, String.class);
			}
		}catch(Exception e) {
			log.error("err ", e);
		}
	}

	/**
	 * 빌링 전일자 데이터 체커
	 * @param testChk true:테스트, false:운영
	 */
	public void chkingBillingDaily(boolean testChk) {
		log.info("START BillingService chkingBillingDaily");
		try {
			StringBuffer sbf = new StringBuffer("빌링 - DAILY CHECKER\n");
			boolean chk = false;
			String[] reportDailyData = {
					"http://192.168.2.76:8000/DailyChk.txt"};

			if (testChk) {
				sbf.append("-- 테스트 발송입니다. ---\n");
			}

			for (String row : reportDailyData) {
				String response = restTemplate.getForObject(row, String.class);
				List<Map> list = new Gson().fromJson(response, ArrayList.class);

				for (Map i : list) {
					log.debug("i - {}", i);

					if (Boolean.parseBoolean((String) i.get("CHK"))) {
						sbf.append(String.format("%s\n", i.get("MSG")));
						chk = true;
					}
				}
			}

			if (chk) {
				log.info("msg - {}", sbf.toString());

				HttpHeaders headers = new HttpHeaders();
				HttpEntity<String> entity = new HttpEntity<>(headers);
				restTemplate.exchange(telegramUrlBilling + sbf.toString(), HttpMethod.GET, entity, String.class);
			}
			log.info("END BillingService chkingBillingDaily");

		} catch (Exception e) {
			log.error("chkingBillingDaily ", e);
		}
	}

	public ArrayList<Map<String, Object>> chkingKakkaLagForUser(String groupId) {
		ArrayList<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		try {
			String kafkaGroupSummeryUrl = kafkaUrl+"/api/status/kafka/"+ groupId +"/KF/groupSummary";
			String response = restTemplate.getForObject(kafkaGroupSummeryUrl, String.class);
			JsonObject root = new JsonParser().parse(response).getAsJsonObject();
			Gson gson = new Gson();

			
			
			for (Entry<String, JsonElement> entry : root.entrySet()) {
				KafkaGroupSummary summery = gson.fromJson(entry.getValue(), KafkaGroupSummary.class);
				Map<String , Object> map = new HashMap<>();	
				log.debug("lag - {}, entry - {}", summery.getTotalLag(), entry.getKey());
				map.put("topic_name ", entry.getKey());
				map.put("topic_lag ", summery.getTotalLag());
				result.add(map);
			}
		} catch (Exception e) {
			log.error("err ", e);
		}
		return result;
	}

	class BeforeHourData{
		private String SITE_CODE="";
		private int CNT=0;

		public int getCNT() {
			return CNT;
		}

		public void setCNT(int cNT) {
			CNT = cNT;
		}

		public String getSITE_CODE() {
			return SITE_CODE;
		}

		public void setSITE_CODE(String sITE_CODE) {
			SITE_CODE = sITE_CODE;
		}
	}
	class MediaChrgResult{
		int STATS_DTTM;
		String MEDIA_ID;
		int MEDIA_SCRIPT_NO;
		int EPRS_REST_RATE;
		String CHRG_FOM_TP_CODE;
		String ALARM;
		int PAR;
		int PAR2;
		float P;
		float P2;
		float MP;
		float MP2;
		int CLICK;
		int CLICK2;
		int DIFF_CLICK;
		int DIFF_PAR;
		float DIFF_AMT;

		public int getSTATS_DTTM() {
			return STATS_DTTM;
		}
		public void setSTATS_DTTM(int sTATS_DTTM) {
			STATS_DTTM = sTATS_DTTM;
		}
		public String getMEDIA_ID() {
			return MEDIA_ID;
		}
		public void setMEDIA_ID(String mEDIA_ID) {
			MEDIA_ID = mEDIA_ID;
		}
		public int getMEDIA_SCRIPT_NO() {
			return MEDIA_SCRIPT_NO;
		}
		public void setMEDIA_SCRIPT_NO(int mEDIA_SCRIPT_NO) {
			MEDIA_SCRIPT_NO = mEDIA_SCRIPT_NO;
		}
		public int getEPRS_REST_RATE() {
			return EPRS_REST_RATE;
		}
		public void setEPRS_REST_RATE(int ePRS_REST_RATE) {
			EPRS_REST_RATE = ePRS_REST_RATE;
		}
		public String getCHRG_FOM_TP_CODE() {
			return CHRG_FOM_TP_CODE;
		}
		public void setCHRG_FOM_TP_CODE(String cHRG_FOM_TP_CODE) {
			CHRG_FOM_TP_CODE = cHRG_FOM_TP_CODE;
		}
		public String getALARM() {
			return ALARM;
		}
		public void setALARM(String aLARM) {
			ALARM = aLARM;
		}
		public int getPAR() {
			return PAR;
		}
		public void setPAR(int pAR) {
			PAR = pAR;
		}
		public int getPAR2() {
			return PAR2;
		}
		public void setPAR2(int pAR2) {
			PAR2 = pAR2;
		}
		public float getP() {
			return P;
		}
		public void setP(float p) {
			P = p;
		}
		public float getP2() {
			return P2;
		}
		public void setP2(float p2) {
			P2 = p2;
		}
		public float getMP() {
			return MP;
		}
		public void setMP(float mP) {
			MP = mP;
		}
		public float getMP2() {
			return MP2;
		}
		public void setMP2(float mP2) {
			MP2 = mP2;
		}
		public int getCLICK() {
			return CLICK;
		}
		public void setCLICK(int cLICK) {
			CLICK = cLICK;
		}
		public int getCLICK2() {
			return CLICK2;
		}
		public void setCLICK2(int cLICK2) {
			CLICK2 = cLICK2;
		}
		public int getDIFF_CLICK() {
			return DIFF_CLICK;
		}
		public void setDIFF_CLICK(int dIFF_CLICK) {
			DIFF_CLICK = dIFF_CLICK;
		}
		public int getDIFF_PAR() {
			return DIFF_PAR;
		}
		public void setDIFF_PAR(int dIFF_PAR) {
			DIFF_PAR = dIFF_PAR;
		}
		public float getDIFF_AMT() {
			return DIFF_AMT;
		}
		public void setDIFF_AMT(float dIFF_AMT) {
			DIFF_AMT = dIFF_AMT;
		}
	}
	class consumerInfo {
		private queue queue;
		private retry retry;
		private retry errorlog;

		public retry getRetry() {
			return retry;
		}
		public void setRetry(retry retry) {
			this.retry = retry;
		}
		public queue getQueue() {
			return queue;
		}
		public void setQueue(queue queue) {
			this.queue = queue;
		}

		class queue{
			int externalQueueSize=0;
			int actionQueueSize=0;
			int shopInfoQueueSize=0;
			int shopStatsQueueSize=0;
			int nearQueueSize=0;
			int clickViewPointQueueSize=0;
			int convQueueSize=0;
			int clientEnvirmentQueueSize=0;
			int clickViewQueueSize=0;
		}
		class retry{
			int fileLength=0;
			ArrayList fileList;
		}
	}
	class underReplicatedPartitions{
		String topic;
		ArrayList<Integer> underReplicatedPartitions;

		public ArrayList<Integer> getUnderReplicatedPartitions() {
			return underReplicatedPartitions;
		}
		public void setUnderReplicatedPartitions(ArrayList<Integer> underReplicatedPartitions) {
			this.underReplicatedPartitions = underReplicatedPartitions;
		}
		public String getTopic() {
			return topic;
		}
		public void setTopic(String topic) {
			this.topic = topic;
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
	class ConsumerQueueSize{
		int externalQueueSize=0;
		int actionQueueSize=0;
		int shopInfoQueueSize=0;
		int nearQueueSize=0;
		int shopStatsQueueSize=0;
		int convQueueSize=0;
		int clickViewQueueSize=0;

		public int getExternalQueueSize() {
			return externalQueueSize;
		}
		public void setExternalQueueSize(int externalQueueSize) {
			this.externalQueueSize = externalQueueSize;
		}
		public int getActionQueueSize() {
			return actionQueueSize;
		}
		public void setActionQueueSize(int actionQueueSize) {
			this.actionQueueSize = actionQueueSize;
		}
		public int getShopInfoQueueSize() {
			return shopInfoQueueSize;
		}
		public void setShopInfoQueueSize(int shopInfoQueueSize) {
			this.shopInfoQueueSize = shopInfoQueueSize;
		}
		public int getNearQueueSize() {
			return nearQueueSize;
		}
		public void setNearQueueSize(int nearQueueSize) {
			this.nearQueueSize = nearQueueSize;
		}
		public int getShopStatsQueueSize() {
			return shopStatsQueueSize;
		}
		public void setShopStatsQueueSize(int shopStatsQueueSize) {
			this.shopStatsQueueSize = shopStatsQueueSize;
		}
		public int getConvQueueSize() {
			return convQueueSize;
		}
		public void setConvQueueSize(int convQueueSize) {
			this.convQueueSize = convQueueSize;
		}
		public int getClickViewQueueSize() {
			return clickViewQueueSize;
		}
		public void setClickViewQueueSize(int clickViewQueueSize) {
			this.clickViewQueueSize = clickViewQueueSize;
		}
	}
}
