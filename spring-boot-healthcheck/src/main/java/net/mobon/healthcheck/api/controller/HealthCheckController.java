//package net.mobon.healthcheck.api.controller;
//
//import net.mobon.healthcheck.api.service.UserLogMongoService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.web.bind.annotation.*;
//
//import io.swagger.annotations.ApiOperation;
//import lombok.extern.slf4j.Slf4j;
//import net.mobon.healthcheck.api.service.ADIDMongoService;
//import net.mobon.healthcheck.api.service.BillingService;
//import net.mobon.healthcheck.api.service.FrameRtbService;
//import net.mobon.healthcheck.api.service.HCAccessLogService;
//import net.mobon.healthcheck.api.service.HCElasticsearchService;
//import net.mobon.healthcheck.api.service.HCMongoService;
//import net.mobon.healthcheck.api.service.HCRedisService;
//import net.mobon.healthcheck.api.service.HealthCheckService;
//import net.mobon.healthcheck.api.service.OpenRtbKafkaService;
//import net.mobon.healthcheck.api.service.OpenRtbService;
//import net.mobon.healthcheck.api.service.RebuildService;
//
//import javax.servlet.http.HttpServletRequest;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Map;
//
//@Slf4j
////@RestController
//public class HealthCheckController {
//
//    @Autowired
//    private HealthCheckService healthCheckService;
//
//    @Autowired
//    private HCRedisService redisHealthChecker;
//
//    @Autowired
//    private HCMongoService mongoHealthChecker;
//
//    @Autowired
//    private HCElasticsearchService elasticsearchHealthCheckService;
//
//    @Autowired
//    private HCAccessLogService accessLogHealthCheckService;
//
//    @Autowired
//    private BillingService billingService;
//
//    @Autowired
//    private ADIDMongoService adidMongoService;
//
//    @Autowired
//    private UserLogMongoService userLogMongoService;
//
//    //OpenRTB
//    @Autowired
//    private OpenRtbService OpenRtbService;
//
//    @Autowired
//    private FrameRtbService frameRtbService;
//
//	@Autowired
//	private RebuildService rebuildService;
//
//
//	@Scheduled(cron = "0 */5 * * * *")
//	public void conversionServiceChk() {
//		try {
//			rebuildService.conversionServiceChk();
//
//		} catch (Exception e) {
//			log.error("conversionServiceChk ", e);
//		}
//	}
//	@Scheduled(cron = "0 */5 * * * *")
//	public void kafkaTopicLagChk() {
//		try {
//			rebuildService.kafkaTopicLagChk();
//
//		} catch (Exception e) {
//			log.error("kafkaTopicLagChk ", e);
//		}
//	}
//
//    @ApiOperation(value = "kafka 서버 상태확인", notes = "kafka 서버 상태확인을 위한 API 입니다.")
//    @ResponseStatus(HttpStatus.OK)
//    @RequestMapping(value = "/kafka", method = RequestMethod.GET)
//    public void kafka() {
//        //healthCheckService.kafka();
//    }
//
//    @Scheduled(fixedDelay = 60000)
//    public void kafkaScheduled() {
//        try {
//            healthCheckService.kafka();
//        } catch (Exception e) {
//            log.error("kafkaLogFilePath ", e);
//        }
//    }
//
//    @ApiOperation(value = "kakao 서버 상태확인", notes = "kakao 서버 상태확인을 위한 API 입니다.")
//    @ResponseStatus(HttpStatus.OK)
//    @RequestMapping(value = "/kakao", method = RequestMethod.GET)
//    public void kakao() {
//        healthCheckService.kakao();
//    }
//
//    @Scheduled(fixedDelay = 60000)
//    public void kakaoScheduled() {
//        try {
//            healthCheckService.kakao();
//        } catch (Exception e) {
//            log.error("kakaoLogFilePath ", e);
//        }
//    }
//
//    @ApiOperation(value = "mobon 서버 상태확인", notes = "mobon 서버 상태확인을 위한 API 입니다.")
//    @ResponseStatus(HttpStatus.OK)
//    @RequestMapping(value = "/mobon", method = RequestMethod.GET)
//    public void mobon() {
//        healthCheckService.mobon();
//    }
//
//    @Scheduled(fixedDelay = 60000)
//    public void mobonScheduled() {
//        try {
//            healthCheckService.mobon();
//        } catch (Exception e) {
//            log.error("mobonLogFilePath ", e);
//        }
//    }
//
//    @ApiOperation(value = "mobon광고주 서버 상태확인", notes = "mobon 광고주 서버 상태확인을 위한 API 입니다.")
//    @ResponseStatus(HttpStatus.OK)
//    @RequestMapping(value = "/mobonRf", method = RequestMethod.GET)
//    public void mobonRf() {
//        healthCheckService.mobonAdverServerCheck();
//    }
//
//    @Scheduled(fixedDelay = 60000)
//    public void mobonRfScheduled() {
//        try {
//            healthCheckService.mobonAdverServerCheck();
//        } catch (Exception e) {
//            log.error("mobonRfLogFilePath ", e);
//        }
//    }
//
//    @ApiOperation(value = "native 서버 상태확인", notes = "native 서버 상태확인을 위한 API 입니다.")
//    @ResponseStatus(HttpStatus.OK)
//    @RequestMapping(value = "/native", method = RequestMethod.GET)
//    public void mobonNative() {
//        healthCheckService.mobonNativeServerCheck();
//    }
//
//    @Scheduled(fixedDelay = 60000)
//    public void mobonNativeScheduled() {
//        try {
//            healthCheckService.mobonNativeServerCheck();
//        } catch (Exception e) {
//            log.error("mobonLogFilePath ", e);
//        }
//    }
//
//    // 3시간 주기로 매40분마다
//    @Scheduled(cron = "0 40 0/3 * * *")
//    public void tokenBatchFirst() {
//        try {
//            healthCheckService.tokenBatch();
//        } catch (Exception e) {
//            log.error("tokenBatchLogFilePath ", e);
//        }
//    }
//
//    // 3시간 주기로 매10분마다
//    @Scheduled(cron = "0 10 2/3 * * *")
//    public void tokenBatchSecond() {
//        try {
//            healthCheckService.tokenBatch();
//        } catch (Exception e) {
//            log.error("tokenBatchLogFilePath ", e);
//        }
//    }
//
//    // 매일 9시
//    @Scheduled(cron = "0 0 09 * * *")
//    public void epBatch() {
//        try {
//            healthCheckService.epBatch();
//        } catch (Exception e) {
//            log.error("epBatchLogFilePath ", e);
//        }
//    }
//
//    // 고도몰 상품수집 실패 광고주 알람
//    @Scheduled(cron = "0 30 10 * * *")
//    public void godoEpBatch() {
//        try {
//            healthCheckService.godoEpBatch();
//        } catch (Exception e) {
//            log.error("godoEpBatchLogFilePath ", e);
//        }
//    }
//
//    @Scheduled(cron = "0 10 * * * *")
//    public void ipBanAutoBatch() {
//	    try {
//	        healthCheckService.ipBanAutoBatch("time");
//        } catch (Exception e) {
//	        log.error("ipBanAutoBatch :: time error :::: ", e);
//        }
//    }
//
//    // 잘못 심어진 웹, 모바일 스크립트 알람
//    @Scheduled(cron = "0 */30 * * * *")
//	public void webMobileTelegramAlarm() {
//		try {
//			healthCheckService.webMobileTelegramAlarm();
//		} catch (Exception e) {
//			log.error("webMobileTelegramAlarmError:: ", e);
//		}
//
//	}
//
//    @Scheduled(cron = "0 15 00 * * *")
//    public void ipBanAutoDayBatch() {
//	    try {
//            healthCheckService.ipBanAutoBatch("day");
//        } catch (Exception e){
//	      log.error("ipBanAutoBatch :: day error :::: ", e);
//        }
//    }
//
//    @Scheduled(fixedDelay = 60000)
//    public void elasticsearchScheduled() {
//        try {
//            elasticsearchHealthCheckService.elasticsearch();
//        } catch (Exception e) {
//            log.error("elasticLogFilePath ", e);
//        }
//    }
//
//    @Scheduled(cron = "0 30 13 * * *")
//    @ResponseStatus(HttpStatus.OK)
//    @RequestMapping(value = "/accesslog", method = RequestMethod.GET)
//    public void accessLog() {
//        accessLogHealthCheckService.accessLogHealthCheck();
//    }
//
//    @Scheduled(fixedDelay = 60000)
//    public void redisHealthChecker() {
//        try {
//            redisHealthChecker.execute();
//        } catch (Exception e) {
//            log.error("redisLogFilePath ", e);
//        }
//    }
//
//    @Scheduled(fixedDelay = 60000)
//    public void checkMongosScheduled() {
//        try {
//            mongoHealthChecker.checkSessions();
//        } catch (Exception e) {
//            log.error("mongoSessionLogFilePath ", e);
//        }
//    }
//
//    @Scheduled(fixedDelay = 60000)
//    public void checkMongoRWScheduled() {
//        try {
//            mongoHealthChecker.checkRW();
//        } catch (Exception e) {
//            log.error("mongoReadwriteLogFilePath ", e);
//        }
//    }
//
//
//    @ApiOperation(value = "kakao 상태확인", notes = "kakao 상태확인을 위한 API 입니다.")
//    @ResponseStatus(HttpStatus.OK)
//    @RequestMapping(value = "/kakaoHealth", method = RequestMethod.GET)
//    public void kakaoHealth() {
//        healthCheckService.kakaoHealth();
//    }
//
//    // 1시간 간격으로 호출함.
//    @Scheduled(fixedDelay = 3600000)
//    public void kakaoHealthScheduled() {
//        healthCheckService.kakaoHealth();
//    }
//
//    /**
//     * 빌링시스템
//     */
//    @Scheduled(cron = "0 */30 * * * *")
//    public void chkingBilling30M() {
//        try {
//        	log.info("BILING chkingBilling30M");
//
//        	// logging
//            billingService.chkingBeforeHourData();
//            billingService.chkingZeroViewClickConv();
//            billingService.chkingBatchRuningTime();
//            billingService.chkingReportCtr();
//
//            // kafka
//            billingService.kafkaTopicLagChk();
//            billingService.chkingunderReplicatedPartitions();
//
//        } catch (Exception e) {
//            log.error("err chkingBilling20M", e);
//        }
//    }
//
//    @Scheduled(cron = "* */20 * * * *")
//    public void chkingBilling1S() {
//		try {
//			log.info("BILING chkingBilling1S");
//
//			// consumer
//			billingService.chkingConsumerRetryFile();
//		}catch(Exception e) {
//			log.error("err chkingBilling1S", e);
//		}
//    }
//
//    @Scheduled(cron = "0 0 10 * * *")
//    public void chkingBillingDailyScheduled() {
//        try {
//            log.info("BILLING chkingBillingDailyScheduled");
//
//            // biling 전일자 데이터 체크
//            billingService.chkingBillingDaily(false);
//        } catch (Exception e) {
//            log.error("err chkingBillingDailyScheduled", e);
//        }
//    }
//
//    @ApiOperation(value = "kafka group id lag 상태확인", notes = "kafka group id lag 상태확인을 위한 API 입니다.")
//    @ResponseStatus(HttpStatus.OK)
//    @RequestMapping(value = "/kafka/chklag/{groupId}", method = RequestMethod.GET)
//    @ResponseBody
//    public ArrayList<Map<String , Object>> chkingKafkaLag(@PathVariable(name="groupId") String groupId) {
//        //해당 uri 기준으로 topic 과 groupId 를 받아서 해당 kafka lag 을 조회한다.
//        log.info("groupId- {}", groupId);
//        ArrayList<Map<String , Object>> result = new ArrayList<>();
//
//        if (groupId == null) {
//            Map<String , Object> map = new HashMap<>();
//            map.put("result", "Error To None GroupId or Topic");
//            result.add(map);
//            return result;
//        }
//
//        // return type = null || offsetData , total Lag
//        result = billingService.chkingKakkaLagForUser(groupId);
//
//        if (result == null || result.size() == 0) {
//            Map<String , Object> map = new HashMap<>();
//            map.put("result", "Error To Find GroupId or Topic");
//            result.add(map);
//        }
//
//        return result;
//    }
//
//
//
//
//
//
//    /**
//     * ADID 몽고 헬스체크(mongostat, mongotop)
//     */
//    @Scheduled(fixedDelay = 60000)
//    public void checkADIDMongoRWScheduled() {
//        adidMongoService.health();
//    }
//
//  /**
//   * USERLOG 몽고 헬스체크(mongostat, mongotop)
//   */
//  @Scheduled(fixedDelay = 60000)
//  public void checkUserLogMongoRWScheduled() {
//    try {
//      userLogMongoService.health();
//    } catch (Exception e) {
//      log.error("UserLogFilePath ", e);
//    }
//  }
//
//
//    /*******************************************
//     * 오픈RTB 시작
//     *******************************************/
//    /*
//     * 오픈RTB 컨슈머 체크
//     */
//    @Scheduled(fixedDelay = 60000)
//    public void openRtbChkingConsumerQueue() {
//        try {
//            OpenRtbService.chkingConsumerQueue();
//            OpenRtbService.kafkaTopicLagChk();
//        } catch (Exception e) {
//            log.error("openRtbConsumerLogFilePath ", e);
//        }
//    }
//
//    /*
//     *오픈 RTB batch 체크
//     */
//    @Scheduled(cron = "0 */5 * * * *")
//    public void openRtbBatchRuningTime() {
//        try {
//            OpenRtbService.chkingBatchRuningTime();
//        } catch (Exception e) {
//            log.error("openRtbBatchLogFilePath ", e);
//        }
//    }
//
//    /*
//     *프레임RTB 미클릭 프레임 체크
//     *매주 월요일 오전 10시
//     */
//    @Scheduled(cron = "0 0 10 * * MON")
//    @ResponseStatus(HttpStatus.OK)
//    @RequestMapping(value = "/frameRtb", method = RequestMethod.GET)
//    public void frameRtb() {
//        try {
//            frameRtbService.chkFrameRtbAbnormalList();
//        } catch (Exception e) {
//            log.error("frameRtb ", e);
//        }
//    }
//
//    @Autowired
//    private OpenRtbKafkaService OpenRtbKafkaService;
//
//    /*******************************************
//     * 오픈RTB 시작
//     *******************************************/
//    /*
//     * 오픈RTB 컨슈머 체크
//     */
//    @Scheduled(fixedDelay = 60000)
//    public void setOpenRtbKafkaChkingConsumerQueue() {
//        try {
//            OpenRtbKafkaService.chkingConsumerQueue();
//            OpenRtbKafkaService.kafkaTopicLagChk();
//        } catch (Exception e) {
//            log.error("openRtbConsumerLogFilePath ", e);
//        }
//    }
//
//    /**
//     * 문맥 매칭 OFF 헬스체크
//     * 문맥 매칭 OFF가 발생했을 때만 알람
//     */
//    @Scheduled(fixedDelay = 60000)
//    public void mtState() {
//        try {
//            healthCheckService.mtState();
//            healthCheckService.swState();
//        } catch (Exception e) {
//            log.error("mtState Check ", e);
//        }
//    }
//
//    @ApiOperation(value = "문맥매칭 서버별 상태확인", notes = "문맥매칭 서버별 상태확인을 위한 API 입니다.")
//    @RequestMapping(value = "/mtState", method = RequestMethod.GET)
//    public void mtAllState() {
//        try {
//            healthCheckService.mtState();
//        } catch (Exception e) {
//            log.error("mtState Check ", e);
//        }
//    }
//}
