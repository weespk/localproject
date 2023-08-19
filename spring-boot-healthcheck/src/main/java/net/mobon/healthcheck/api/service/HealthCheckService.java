package net.mobon.healthcheck.api.service;

import lombok.extern.slf4j.Slf4j;
import net.mobon.healthcheck.api.model.StateData;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
//@Service
public class HealthCheckService {

    @Value("${telegram.url}")
    private String telegramUrl;

    @Value("${telegram.url.kakaodaisy}")
    private String telegramUrlKakaoDaisy; // 카카오 전용 텔레봇

    @Value("${telegram.url.kakaoMobon}")
    private String telegramUrlKakaoMobon; // 전용 텔레봇

    @Value("${telegram.url.social}")
    private String telegramUrlSocial;    // 소셜링크 URL

    @Value("${telegram.url.cafe}")
    private String telegramUrlCafe;    // cafe24 URL

    @Value("${telegram.url.ipBan}")
    private String telegramUrlIpban;

    @Value("${telegram.url.WMScriptIncorrect}")
    private String telegramWMIncorrect;

    @Value("${telegram.url.mtUseYn}")
    private String telegramMtState;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private MTTelegramService mtTelegramService;

    @Autowired SWTelegramService swTelegramService;

    private static Map<String, Boolean> changeCheckMap = new HashMap<String, Boolean>();
    private static Map<String, Boolean> changeArticleStateMap = new HashMap<String, Boolean>();
    private static Map<String, Boolean> changeInflowStateMap = new HashMap<String, Boolean>();
    private static Map<String, Boolean> changeProductStateMap = new HashMap<String, Boolean>();
    private static Map<String, Boolean> changeInflowAuidStateMap = new HashMap<String, Boolean>();
    private static Map<String, Boolean> changeSWStateMap = new HashMap<String, Boolean>();

    public void kakao() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        List<String> list = new ArrayList<>();
        list.add("10.251.0.228");
        list.add("10.251.0.229");
        list.add("10.251.0.230");
        list.add("10.251.0.231");
        list.add("10.251.0.232");
        list.add("10.251.0.233");
        list.add("10.251.0.234");
        list.add("10.251.0.235");
//        list.add("10.251.0.238");
        list.add("10.251.0.239");
        list.add("10.251.0.246");
        list.add("10.251.0.247");
        list.add("10.251.0.248");
        list.add("10.251.0.249");

        for (String url : list) {
            try {
                ResponseEntity<String> response = restTemplate.exchange("http://" + url + "/rtb/monitor", HttpMethod.GET, entity, String.class);
                HttpStatus statusCode = response.getStatusCode();
                log.debug(url);
                if (!HttpStatus.OK.equals(statusCode)) {
                    restTemplate.exchange(telegramUrlKakaoDaisy + url + " openrtb 응답실패", HttpMethod.GET, entity, String.class);
                }
            } catch (Exception e) {
                e.printStackTrace();
                restTemplate.exchange(telegramUrlKakaoDaisy + url + " openrtb 타임아웃 실패" + e.getMessage(), HttpMethod.GET, entity, String.class);
            }
        }
    }

    public void kakaoHealth() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        List<String> listParams = new ArrayList<>();
        listParams.add("noclick");
        listParams.add("yield");

        String[] typeString = {"kakao","google","igaworks"};

        for (String type: typeString) {
            for (String param : listParams) {
                try {
                    ResponseEntity<String> response = restTemplate.exchange("http://10.251.0.228/rtb/kakaoHealth?type="+type+"&chk=" + param, HttpMethod.GET, entity, String.class);

                    String body = response.getBody().trim();
                    if (body.length() > 20) {
                        try {
                            restTemplate.exchange(telegramUrlKakaoMobon + body, HttpMethod.GET, entity, String.class);
                        } catch (Exception e) {
                        }
                    }
                } catch (Exception e) {
                }
            }
        }
    }


    public void kafka() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        List<String> list = new ArrayList<>();
        list.add("10.251.0.228");
        list.add("10.251.0.229");
        list.add("10.251.0.230");
        list.add("10.251.0.231");
        list.add("10.251.0.232");
        list.add("10.251.0.233");
        list.add("10.251.0.234");
        list.add("10.251.0.235");
//        list.add("10.251.0.238");
        list.add("10.251.0.239");
        list.add("10.251.0.246");
        list.add("10.251.0.247");
        list.add("10.251.0.248");
        list.add("10.251.0.249");

        StringBuffer sbf = new StringBuffer("openrtb 빌링 시스템 이상 발생!!!!(10000 건 이상시 호출) \n");
        boolean chk = false;

        for (String url : list) {
            try {
                ResponseEntity<String> response = restTemplate.exchange("http://" + url + "/cronwork/monitoring/kafka_error_retrycnt.txt", HttpMethod.GET, entity, String.class);

                int cnt = Integer.valueOf(response.getBody().trim());

                sbf.append(url + " Kafka retrycnt => " + cnt + " cnt \n");

                if (cnt > 10000) {
                    chk = true;
                }

            } catch (Exception e) {
                e.printStackTrace();

            }
        }

        if (chk) {
            restTemplate.exchange(telegramUrlKakaoDaisy + sbf.toString(), HttpMethod.GET, entity, String.class);
        }
    }

    public void mobon() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        List<String> list = new ArrayList<>();
        list.add("www.mediacategory.com");
        list.add("10.251.0.2");
        // list.add("10.251.0.3");
        list.add("10.251.0.4");
        list.add("10.251.0.5");
        list.add("10.251.0.6");
        list.add("10.251.0.7");
        list.add("10.251.0.8");
        list.add("10.251.0.9");
        list.add("10.251.0.10");
        list.add("10.251.0.11");
        list.add("10.251.0.12");
        list.add("10.251.0.13");
        list.add("10.251.0.14");
        list.add("10.251.0.15");
        list.add("10.251.0.16");
        list.add("10.251.0.17");
        list.add("10.251.0.18");
        list.add("10.251.0.19");
        list.add("10.251.0.20");
        list.add("10.251.0.21");
        list.add("10.251.0.22");
        list.add("10.251.0.23");
        list.add("10.251.0.24");
        list.add("10.251.0.25");
        list.add("10.251.0.26");
        list.add("10.251.0.46");
        list.add("10.251.0.47");
        list.add("10.251.0.48");
//        list.add("10.251.0.146");
        list.add("10.251.0.147");
        list.add("10.251.0.148");
        list.add("10.251.0.149");

        for (String urlStr : list) {
            try {
                log.info("healthcheck urlStr = {} ", urlStr);
                ResponseEntity<String> response = restTemplate.exchange("http://" + urlStr + "/servlet/monitor?mode=healthcheck", HttpMethod.GET, entity, String.class);
                HttpStatus statusCode = response.getStatusCode();
                log.debug(urlStr);
                if (!HttpStatus.OK.equals(statusCode)) {
                    restTemplate.exchange(telegramUrl + urlStr + " 모비온 응답실패", HttpMethod.GET, entity, String.class);
                }
            } catch (Exception e) {
                restTemplate.exchange(telegramUrl + urlStr + " 모비온 타임아웃 실패" + e.getMessage(), HttpMethod.GET, entity, String.class);
            }

            if (urlStr.indexOf("www.mediacategory.com") > -1) {
                try {
                    URL url = new URL("https://" + urlStr + "/servlet/monitor?mode=healthcheck");
                    HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
                    if (HttpsURLConnection.HTTP_OK != con.getResponseCode()) {
                        restTemplate.exchange(telegramUrl + urlStr + " 모비온 응답실패", HttpMethod.GET, entity, String.class);
                    }
                } catch (Exception e) {
                    restTemplate.exchange(telegramUrl + urlStr + " 모비온 타임아웃 실패" + e.getMessage(), HttpMethod.GET, entity, String.class);
                }
            }
        }
    }

    public void mobonAdverServerCheck() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        List<String> list = new ArrayList<>();
        list.add("www.megadata.co.kr");
        list.add("log.mediacategory.com");

        list.add("10.251.0.32");
        list.add("10.251.0.33");
        list.add("10.251.0.34");
        list.add("10.251.0.35");
        list.add("10.251.0.36");
        list.add("10.251.0.37");
        list.add("10.251.0.38");

        for (String urlStr : list) {
            try {

                ResponseEntity<String> response = restTemplate.exchange("http://" + urlStr + "/servlet/monitor?mode=healthcheck", HttpMethod.GET, entity, String.class);
                HttpStatus statusCode = response.getStatusCode();
                log.debug(urlStr);
                if (!HttpStatus.OK.equals(statusCode) && !HttpStatus.FOUND.equals(statusCode)) {
                    restTemplate.exchange(telegramUrl + urlStr + " 모비온 응답실패", HttpMethod.GET, entity, String.class);
                }
            } catch (Exception e) {
                restTemplate.exchange(telegramUrl + urlStr + " 모비온 타임아웃 실패" + e.getMessage(), HttpMethod.GET, entity, String.class);
            }
            if (urlStr.contains("log.mediacategory.com")) {
                try {
                    URL url = new URL("https://" + urlStr + "/servlet/monitor?mode=healthcheck");
                    HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
                    if (HttpsURLConnection.HTTP_OK != con.getResponseCode()) {
                        restTemplate.exchange(telegramUrl + urlStr + " 모비온 응답실패", HttpMethod.GET, entity, String.class);
                    }
                } catch (Exception e) {
                    restTemplate.exchange(telegramUrl + urlStr + " 모비온 타임아웃 실패" + e.getMessage(), HttpMethod.GET, entity, String.class);
                }
            }
        }
    }

    public void mobonNativeServerCheck() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        List<String> list = new ArrayList<>();
        list.add("native.mediacategory.com");

        list.add("10.251.0.122");
        list.add("10.251.0.123");
        list.add("10.251.0.124");
        list.add("10.251.0.125");
        list.add("10.251.0.126");
        list.add("10.251.0.127");

        for (String urlStr : list) {
            try {

                ResponseEntity<String> response = restTemplate.exchange("http://" + urlStr + "/servlet/monitor?mode=healthcheck", HttpMethod.GET, entity, String.class);
                HttpStatus statusCode = response.getStatusCode();
                log.debug(urlStr);
                if (!HttpStatus.OK.equals(statusCode) && !HttpStatus.FOUND.equals(statusCode)) {
                    restTemplate.exchange(telegramUrl + urlStr + " 모비온 응답실패", HttpMethod.GET, entity, String.class);
                }
            } catch (Exception e) {
                restTemplate.exchange(telegramUrl + urlStr + " 모비온 타임아웃 실패" + e.getMessage(), HttpMethod.GET, entity, String.class);
            }
            if (urlStr.contains("native.mediacategory.com")) {
                try {
                    URL url = new URL("https://" + urlStr + "/servlet/monitor?mode=healthcheck");
                    HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
                    if (HttpsURLConnection.HTTP_OK != con.getResponseCode()) {
                        restTemplate.exchange(telegramUrl + urlStr + " 네이티브 응답실패", HttpMethod.GET, entity, String.class);
                    }
                } catch (Exception e) {
                    restTemplate.exchange(telegramUrl + urlStr + " 네이티브 타임아웃 실패" + e.getMessage(), HttpMethod.GET, entity, String.class);
                }
            }
        }
    }

    public void tokenBatch() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        List<String> list = new ArrayList<>();
        list.add("addata.mediacategory.com");

        StringBuffer sb = new StringBuffer("카페24 token발급 실패 광고주 발생\n");
        boolean chk = false;

        for (String url : list) {
            try {
                ResponseEntity<String> response = restTemplate.exchange("http://" + url + "/EP/cafe24/tokenFail.txt", HttpMethod.GET, entity, String.class);
                String responseBody = StringUtils.defaultIfEmpty(response.getBody(), "");
                if (!"".equals(responseBody)) {
                    String adverid = responseBody.trim();
                    String adveridArray[] = adverid.split("\\|\\|");
                    int cnt = adveridArray.length;
                    sb.append("fail 광고주 수:" + cnt + "\n");
                    for (int i = 0; i < cnt; i++) {
                        sb.append(adveridArray[i]);
                        if (i != cnt - 1)
                            sb.append(",");
                    }
                    if (cnt > 0) {
                        chk = true;
                    }
                }
            } catch (Exception e) {
                log.error("error", e);
            }
            if (chk) {
                restTemplate.exchange(telegramUrlCafe + sb.toString(), HttpMethod.GET, entity, String.class);
            }
        }

    }

    // 잘못 심어진 웹, 모바일 스크립트 알람
    public void webMobileTelegramAlarm() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date();
        String yyyymmdd = dateFormat.format(date);
        String scode = "";
        String adType = "";
        String refererDomain = "";
        String getRedisValue = "";
        String count = "";

        try {
            Jedis jedis = new JedisPool(new JedisPoolConfig(), "10.251.0.102", 8000, 5000, "Fhrmtnwlq2017!23$").getResource();
            jedis.select(15);

            // 레디스에 키가 존재할 경우 값을 가져옴
            String redisKey = "ETC" + ":" + "IncorrectWM" + ":" + yyyymmdd;
            if (jedis.exists(redisKey)) {
                getRedisValue = jedis.get(redisKey);
                String[] arrScript = getRedisValue.split(",");
                List<String> scriptList = Arrays.asList(arrScript);
                // 값을 쪼개서 s값, 배너 타입, url주소로 나눔
                for (String script : scriptList) {
                    String arrScriptInfo[] = script.split("\\|\\|");
                    List<String> scriptInfoList = Arrays.asList(arrScriptInfo);

                    if (scriptInfoList.size() < 3) continue;

                    scode = scriptInfoList.get(0);
                    adType = scriptInfoList.get(1);
                    refererDomain = scriptInfoList.get(2);
                    String redisCountKey = "ETC" + ":" + "IncorrectWM" + ":" + scode;
                    count = jedis.get(redisCountKey);

                    if ("none".equals(refererDomain)) {
                        refererDomain = "없음";
                    }

                    // 알람을 보냄
                    if (StringUtils.isEmpty(count)) continue;

                    HttpHeaders headers = new HttpHeaders();
                    HttpEntity<String> entity = new HttpEntity<>(headers);
                    StringBuffer sb = new StringBuffer();
                    sb.append("알림! 웹 또는 모바일 배너가 잘못 삽입되었습니다.\n");
                    sb.append("S값: " + scode + "\n");
                    sb.append("타입: " + adType + "\n");
                    sb.append("카운트: " + count + "\n");
                    sb.append("URL: " + refererDomain);
                    restTemplate.exchange(telegramWMIncorrect + sb.toString(), HttpMethod.GET, entity, String.class);
                }
            }
            jedis.close();
        } catch (Exception e) {
            log.error("웹/스크립트 알람 에러 :: " + getRedisValue + " :: ", e);
        }
    }

    // 독립몰 EP 상품수집 실패 광고주 알람
    public void epBatch() {

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        List<String> list = new ArrayList<>();
        list.add("addata.mediacategory.com");

        StringBuffer sb = new StringBuffer();
        boolean chk = false;

        for (String url : list) {
            try {
                ResponseEntity<String> response = restTemplate.exchange("http://" + url + "/EP/epFail/epBatchFail.txt", HttpMethod.GET, entity, String.class);
                String adveridArray[] = response.getBody().trim().split("\\|\\|");
                int cnt = adveridArray.length;
                sb.append("EP batch 실패 광고주 발생!\n");
                for (int i = 0; i < adveridArray.length; i++) {
                    sb.append(i + 1 + ". " + adveridArray[i] + "\n");
                }
                if (cnt > 0) {
                    chk = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (chk) {
            restTemplate.exchange(telegramUrlCafe + sb.toString(), HttpMethod.GET, entity, String.class);
        }
    }

    // 고도몰 상품수집 실패 광고주 알람
    public void godoEpBatch() {

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        List<String> list = new ArrayList<>();
        list.add("addata.mediacategory.com");

        StringBuffer sb = new StringBuffer();
        boolean chk = false;

        for (String url : list) {
            try {
                ResponseEntity<String> response = restTemplate.exchange("http://" + url + "/EP/epFail/godoEpBatchFail.txt", HttpMethod.GET, entity, String.class);
                String adveridArray[] = response.getBody().trim().split("\\|\\|");
                int cnt = adveridArray.length;
                sb.append("고도몰 EP batch 실패 광고주 발생!\n");
                for (int i = 0; i < adveridArray.length; i++) {
                    sb.append(i + 1 + ". " + adveridArray[i] + "\n");
                }
                if (cnt > 0) {
                    chk = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (chk) {
            restTemplate.exchange(telegramUrlCafe + sb.toString(), HttpMethod.GET, entity, String.class);
        }
    }


    public void recommend() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));
        String json = "{\"client\":[{\"target\":\"SR\",\"length\":4,\"logo\":\"dabagirl-m1091370850000.png\",\"bntype\":\"30\",\"logo2\":\"dabagirl-482414901000.png\",\"data\":[{\"t1\":\"\",\"pcode\":\"43840\",\"pnm\":\"우아하게모노키니_D2SU 수영복 섹시한 섹시수영복 레이..\",\"price\":\"44000\",\"img\":\"http://m.dabagirl.co.kr/web/product/big/201704/43840_shop1_806036.jpg\",\"purl\":\"/servlet/drc?no=43840&kno=0&s=9498&adgubun=SR&gb=SR&sc=2f7f159a8b8fcf23d9fa43ebbc73b57a&mc=9498&userid=dabagirl&u=dabagirl&product=mbw&slink=http%3A%2F%2Fm.dabagirl.co.kr%2Fproduct%2Fdetail.html%3Fproduct_no%3D43840%26cate_no%3D218%26cafe_mkt%3Due_mb05%26utm_source%3Dmobion%26utm_medium%3Dmbmobile%26utm_campaign%3Dmbmobile&pCode=43840&rtb_param=598bc9d0208f182a601c2b94_1_L&mobonlinkcate=none&freqLog=1&turl=http%3A%2F%2Fm.dabagirl.co.kr%2FBC%2FindexAD.html%3Fcafe_mkt%3Due_mb05%26utm_source%3Dmobion%26utm_medium%3Dmbmobile%26utm_campaign%3Dmbmobile&tgubun=two&viewTime=_9498_1504234405653_7861&hId=43&subadgubun=SR\",\"logo\":\"dabagirl-m1091370850000.png\",\"logo2\":\"dabagirl-482414901000.png\",\"site_name\":\"다바걸\",\"site_title\":\"다바걸\",\"site_url\":\"http://m.dabagirl.co.kr/product/detail.html?product_no=43840&cate_no=218\",\"site_desc\":\"스타일을 만드는사람들 다바걸에서 스타일링하자\",\"site_desc1\":\"남심 녹는 패션 전상품 세일 중 무료배송 독보적인 아름다움 다바걸\",\"site_desc2\":\"Sexy 로맨틱 취향저격 예쁜 신상 가득 자체제작 무료배송 다바걸\",\"site_desc3\":\"스타일을만든다\",\"site_desc4\":\"남자들의 시선 강탈 룩\"},{\"t2\":\"\",\"pcode\":\"45275\",\"pnm\":\"반전있는모노키니_D2SU 수영복 원피스수영복 뒷트임수영..\",\"price\":\"38500\",\"img\":\"http://m.dabagirl.co.kr/web/product/big/201706/45275_shop1_510595.jpg\",\"purl\":\"/servlet/drc?no=45275&kno=0&s=9498&adgubun=CT&gb=CT&sc=2f7f159a8b8fcf23d9fa43ebbc73b57a&mc=9498&userid=dabagirl&u=dabagirl&product=mbw&slink=http%3A%2F%2Fm.dabagirl.co.kr%2Fproduct%2Fdetail.html%3Fproduct_no%3D45275%26cate_no%3D218%26cafe_mkt%3Due_mb05%26utm_source%3Dmobion%26utm_medium%3Dmbmobile%26utm_campaign%3Dmbmobile&pCode=45275&rtb_param=598bc9d0208f182a601c2b94_1_L&mobonlinkcate=none&freqLog=1&turl=http%3A%2F%2Fm.dabagirl.co.kr%2FBC%2FindexAD.html%3Fcafe_mkt%3Due_mb05%26utm_source%3Dmobion%26utm_medium%3Dmbmobile%26utm_campaign%3Dmbmobile&tgubun=two&viewTime=_9498_1504234405942_3331&hId=43&subadgubun=CT\",\"logo\":\"dabagirl-m1091370850000.png\",\"logo2\":\"dabagirl-482414901000.png\",\"site_name\":\"다바걸\",\"site_title\":\"다바걸\",\"site_url\":\"http://m.dabagirl.co.kr/product/detail.html?product_no=43840&cate_no=218\",\"site_desc\":\"스타일을 만드는사람들 다바걸에서 스타일링하자\",\"site_desc1\":\"남심 녹는 패션 전상품 세일 중 무료배송 독보적인 아름다움 다바걸\",\"site_desc2\":\"Sexy 로맨틱 취향저격 예쁜 신상 가득 자체제작 무료배송 다바걸\",\"site_desc3\":\"스타일을만든다\",\"site_desc4\":\"남자들의 시선 강탈 룩\"},{\"t3\":\"\",\"pcode\":\"47046\",\"pnm\":\"핫딜]매니쉬무드자켓원핏_A3OP 자켓원피스 섹시 글램 ..\",\"price\":\"46000\",\"img\":\"http://m.dabagirl.co.kr/web/product/big/201708/47046_shop1_822677.jpg\",\"purl\":\"/servlet/drc?no=47046&kno=0&s=9498&adgubun=CR&gb=CR&sc=2f7f159a8b8fcf23d9fa43ebbc73b57a&mc=9498&userid=dabagirl&u=dabagirl&product=mbw&slink=http%3A%2F%2Fm.dabagirl.co.kr%2Fproduct%2Fdetail.html%3Fproduct_no%3D47046%26cate_no%3D329%26cafe_mkt%3Due_mb05%26utm_source%3Dmobion%26utm_medium%3Dmbmobile%26utm_campaign%3Dmbmobile&pCode=47046&rtb_param=598bc9d0208f182a601c2b94_1_L&mobonlinkcate=none&freqLog=1&turl=http%3A%2F%2Fm.dabagirl.co.kr%2FBC%2FindexAD.html%3Fcafe_mkt%3Due_mb05%26utm_source%3Dmobion%26utm_medium%3Dmbmobile%26utm_campaign%3Dmbmobile&tgubun=two&viewTime=_9498_1504234405987_1751&hId=43&subadgubun=CR\",\"logo\":\"dabagirl-m1091370850000.png\",\"logo2\":\"dabagirl-482414901000.png\",\"site_name\":\"다바걸\",\"site_title\":\"다바걸\",\"site_url\":\"http://m.dabagirl.co.kr/product/detail.html?product_no=43840&cate_no=218\",\"site_desc\":\"스타일을 만드는사람들 다바걸에서 스타일링하자\",\"site_desc1\":\"남심 녹는 패션 전상품 세일 중 무료배송 독보적인 아름다움 다바걸\",\"site_desc2\":\"Sexy 로맨틱 취향저격 예쁜 신상 가득 자체제작 무료배송 다바걸\",\"site_desc3\":\"스타일을만든다\",\"site_desc4\":\"남자들의 시선 강탈 룩\"},{\"t1\":\"\",\"pcode\":\"45275\",\"pnm\":\"반전있는모노키니_D2SU 수영복 원피스수영복 뒷트임수영..\",\"price\":\"38500\",\"img\":\"http://m.dabagirl.co.kr/web/product/big/201706/45275_shop1_510595.jpg\",\"purl\":\"/servlet/drc?no=45275&kno=0&s=9498&adgubun=SR&gb=SR&sc=2f7f159a8b8fcf23d9fa43ebbc73b57a&mc=9498&userid=dabagirl&u=dabagirl&product=mbw&slink=http%3A%2F%2Fm.dabagirl.co.kr%2Fproduct%2Fdetail.html%3Fproduct_no%3D45275%26cate_no%3D218%26cafe_mkt%3Due_mb05%26utm_source%3Dmobion%26utm_medium%3Dmbmobile%26utm_campaign%3Dmbmobile&pCode=45275&rtb_param=598bc9d0208f182a601c2b94_1_L&mobonlinkcate=none&freqLog=1&turl=http%3A%2F%2Fm.dabagirl.co.kr%2FBC%2FindexAD.html%3Fcafe_mkt%3Due_mb05%26utm_source%3Dmobion%26utm_medium%3Dmbmobile%26utm_campaign%3Dmbmobile&tgubun=two&viewTime=_9498_1504234406004_2047&hId=43&subadgubun=RP\",\"logo\":\"dabagirl-m1091370850000.png\",\"logo2\":\"dabagirl-482414901000.png\",\"site_name\":\"다바걸\",\"site_title\":\"다바걸\",\"site_url\":\"http://m.dabagirl.co.kr/product/detail.html?product_no=43840&cate_no=218\",\"site_desc\":\"스타일을 만드는사람들 다바걸에서 스타일링하자\",\"site_desc1\":\"남심 녹는 패션 전상품 세일 중 무료배송 독보적인 아름다움 다바걸\",\"site_desc2\":\"Sexy 로맨틱 취향저격 예쁜 신상 가득 자체제작 무료배송 다바걸\",\"site_desc3\":\"스타일을만든다\",\"site_desc4\":\"남자들의 시선 강탈 룩\"}],\"siteUrl\":\"http://m.dabagirl.co.kr/product/detail.html?product_no=43840&cate_no=218\"}],\"addInfo\":{\"frameCode\":\"MY04E09C02M041\",\"userId\":\"dabagirl\",\"mediaScriptNo\":\"9498\",\"siteCode\":\"2f7f159a8b8fcf23d9fa43ebbc73b57a\"}}";
        HttpEntity<String> entity = new HttpEntity<>(json, headers);
        List<String> list = new ArrayList<>();
        list.add("10.251.0.108");
        list.add("10.251.0.108:8080");
        list.add("10.251.0.108:8081");

        for (String url : list) {
            try {
                ResponseEntity<String> response = restTemplate.exchange("http://" + url + "/apis/enliple/impression/recommendAD", HttpMethod.POST, entity, String.class);
                HttpStatus statusCode = response.getStatusCode();
                log.debug(url);
                if (!HttpStatus.OK.equals(statusCode)) {
                    restTemplate.exchange(telegramUrl + url + " 추천 응답 실패", HttpMethod.GET, entity, String.class);
                }
            } catch (Exception e) {
                restTemplate.exchange(telegramUrl + url + " 추천 타임 아웃 실패" + e.getMessage(), HttpMethod.GET, entity, String.class);
            }
        }
    }

    public void ipBanAutoBatch(String gubun) {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        List<String> list = new ArrayList<>();
        list.add("addata.mediacategory.com");

        StringBuffer sb = new StringBuffer();
        boolean chk = false;

        for (String url : list) {
            try {
                String file_path = "";
                if ("day".equals(gubun)) {
                    log.info("::::: ipBanAuto day Start :::::");
                    file_path = "/ipBanAuto/ipBanAutoListDay.txt";
                    ResponseEntity<String> response = restTemplate.exchange("http://" + url + file_path, HttpMethod.GET, entity, String.class);
                    String strArray[] = response.getBody().trim().split("\\|\\|");
                    if(strArray != null && !strArray.equals("")){
                        int cnt = strArray.length;

                        for (int i = 0; i < strArray.length; i++) {
                            if (i == 0) {
                                sb.append("IP자동차단 해제!! ").append("\n");
                            } else {
                                String ip = strArray[i];
                                sb.append(i).append(". ").append(ip).append("\n");
                            }
                        }
                        if (cnt > 1) {
                            chk = true;
                            log.info("::::: ipBanAuto day Sucess :::::");
                        } else {
                            // 일반IP차단이력이 없을 경우 sb초기화 이후 모비센스 차단이력 조회를 실시
                            sb = new StringBuffer();
                        }
                    }

                } else if ("time".equals(gubun)){
                    log.info("::::: ipBanAuto time Start :::::");
                    file_path = "/ipBanAuto/ipBanAutoList.txt";

                    ResponseEntity<String> response = restTemplate.exchange("http://" + url + file_path, HttpMethod.GET, entity, String.class);
                    String strArray[] = response.getBody().trim().split("\\|\\|");
                    if(strArray != null && !strArray.equals("")){
                        int cnt = strArray.length;

                        for (int i = 0; i < strArray.length; i++) {
                            if (i == 0) {
                                sb.append("IP차단 발생!!! ").append(strArray[i]).append("\n");
                            } else {
                                String ipArray[] = strArray[i].trim().split("\\^");
                                sb.append(i).append(". ").append(ipArray[0]).append(" (").append(ipArray[2]).append(")(").append(ipArray[1]).append("건)\n");
                            }
                        }
                        if (cnt > 1) {
                            chk = true;
                            log.info("::::: ipBanAuto time Sucess :::::");
                        } else {
                            // 일반IP차단이력이 없을 경우 sb초기화 이후 모비센스 차단이력 조회를 실시
                            sb = new StringBuffer();
                        }
                    }
                }

                String file_pathMobsense = "";
                if ("day".equals(gubun)) {
                    file_pathMobsense = "/ipBanAutoMobsense/ipBanAutoListDay.txt";
                } else {
                    file_pathMobsense = "/ipBanAutoMobsense/ipBanAutoList.txt";
                }

                ResponseEntity<String> responseMobsense = restTemplate.exchange("http://" + url + file_pathMobsense, HttpMethod.GET, entity, String.class);
                String strArrayMobsense[] = responseMobsense.getBody().trim().split("\\|\\|");
                int cntMobsense = strArrayMobsense.length;

                for (int i = 0; i < strArrayMobsense.length; i++) {
                    if (i == 0 && strArrayMobsense.length > 1){
                        // index 0 일 때의 로직은 기존과 같지만, 알람목록이 생성된경우에는 IP차단 문구를 삽입하지 않음.
                        //if(chk) continue;
                        // 알람목록이 생성되지 않은 경우 sb를 초기화시켜줬기때문에 문구 재 작성 -> 상단로직을 경로만 바꿔 재 실행하는것과 같음
                        if(chk) {
                          sb.append("--------------------------------------------").append("\n").append("모비센스 IP차단 발생!!! ").append(strArrayMobsense[i]).append("\n");
                        } else {
                            sb.append("모비센스 IP차단 발생!!! ").append(strArrayMobsense[i]).append("\n");
                        }

                    } else if(i != 0){
                        String ipArray[] = strArrayMobsense[i].trim().split("\\^");
                        sb.append(i).append(". ").append(ipArray[0]).append(" (").append(ipArray[2]).append(")(").append(ipArray[1]).append("건)_모비센스\n");
                    }
                }
                if (cntMobsense > 1) {
                    chk = true;
                }

            } catch (Exception e) {
                e.printStackTrace();
                log.error("ipBanAutoBatch error::", e);

            }
        }

        if (chk) {
            restTemplate.exchange(telegramUrlIpban + sb.toString(), HttpMethod.GET, entity, String.class);
        }
    }

    public void mtState() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        List<String> list = getMediaServerInfo();

        Map<String, StateData> mtMap = new HashMap<String, StateData>();

        mtMap.put("mtState", new StateData());
//        mtMap.put("article", new StateData());
        mtMap.put("inflow", new StateData());
        mtMap.put("product", new StateData());

        mtMap.forEach((key, value) -> {
            value.setSendCheck(false);
            value.setSb(new StringBuilder());
            value.setCount(0);
        });

        mtMap.get("mtState").getSb().append("<<< 문맥 매칭 >>>\n").append("ElasticSearch 타임 아웃으로 MT OFF 서버 발생\n");
//        mtMap.get("article").getSb().append("<<< 핵심 키워드 >>>\n").append("ElasticSearch 타임 아웃으로 핵심 키워드 조회 OFF 서버 발생\n");
        mtMap.get("inflow").getSb().append("<<< 유입 + 핵심 키워드 >>>\n").append("유입 + 핵심 키워드 조회 OFF로 문맥 매칭 OFF 서버 발생\n");
        mtMap.get("product").getSb().append("<<< 상품 Like >>>\n").append("ElasticSearch 타임 아웃으로 상품 Like 조회 OFF 서버 발생\n");

        mtMap.get("mtState").setBeforeState(changeCheckMap);
//        mtMap.get("article").setBeforeState(changeArticleStateMap);
        mtMap.get("inflow").setBeforeState(changeInflowStateMap);
        mtMap.get("product").setBeforeState(changeProductStateMap);

        for (String urlStr : list) {
            try {
                mtMap.forEach((key, value) -> value.setUseYn(true));

                log.info("healthcheck urlStr = {} ", urlStr);
                ResponseEntity<String> response = restTemplate.exchange("http://" + urlStr + "/servlet/monitor?mode=mtState", HttpMethod.GET, entity, String.class);
                HttpStatus statusCode = response.getStatusCode();
                String body = response.getBody().trim();

                if(body.contains("MT_SERVICE_USE_YN=false")) {
                    mtMap.get("mtState").setUseYn(false);
                }
//                if (body.contains("MT_ARTICLE_KEYWORD_USE_YN=false")) {
//                    mtMap.get("article").setUseYn(false);
//                }
                if (body.contains("MT_INFWLOW_KEYWORD_USE_YN=false") && body.contains("MT_ARTICLE_KEYWORD_USE_YN=false")) {
                    mtMap.get("inflow").setUseYn(false);
                }
                if (body.contains("MT_PCODE_LIKE_SEARCH_USE_YN=false")) {
                    mtMap.get("product").setUseYn(false);
                }


                mtMap.forEach((key, value) -> {
                    if (value.getBeforeState().get(urlStr) == null) {
                        value.getBeforeState().put(urlStr, value.isUseYn());
                        if(!value.isUseYn()) {
                            value.setSendCheck(true);
                        }
                    }

                    if(!value.isUseYn() && value.getBeforeState().get(urlStr) != value.isUseYn()) {
                        value.setSendCheck(true);
                        value.getBeforeState().put(urlStr, value.isUseYn());
                    }

                    if(value.isUseYn()) {
                        value.getBeforeState().put(urlStr, value.isUseYn());
                        value.setState("ON");
                    } else {
                        value.setState("OFF");
                        value.setCount(value.getCount() + 1);
                    }

                    value.getSb().append(urlStr);
                    if("mtState".equals(key)) {
                        value.getSb().append(" :: 문맥매칭 상태 => ").append(value.getState()).append("\n");
                    } else if("article".equals(key)) {
                        value.getSb().append(" :: 핵심키워드 상태 => ").append(value.getState()).append("\n");
                    } else if("inflow".equals(key)) {
                        value.getSb().append(" :: 키워드조회 상태 => ").append(value.getState()).append("\n");
                    } else {
                        value.getSb().append(" :: 상품 LIKE 상태 => ").append(value.getState()).append("\n");
                    }
                });
                log.debug(urlStr);
            } catch (Exception e) {
            }
        }
        mtMap.forEach((key, value) ->{
            value.getSb().append("OFF 된 서버 수 : ").append(value.getCount()).append("\n");
            if(value.isSendCheck()) {
                restTemplate.exchange(telegramMtState + value.getSb().toString() , HttpMethod.GET, entity, String.class);
            }
        });
    }

    public void swState() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        List<String> list = getMediaServerInfo();

        Map<String, StateData> mtMap = new HashMap<String, StateData>();

        mtMap.put("inflowAuidState", new StateData());
        mtMap.put("swState", new StateData());

        mtMap.forEach((key, value) -> {
            value.setSendCheck(false);
            value.setSb(new StringBuilder());
            value.setCount(0);
        });

        mtMap.get("inflowAuidState").getSb().append("<<< 기사 유입 AUID >>>\n").append("DB 타임 아웃으로 기사 유입 AUID 수집 OFF 서버 발생\n");

        mtMap.get("inflowAuidState").setBeforeState(changeInflowAuidStateMap);

        mtMap.get("swState").getSb().append("<<< 소셜오디언스(SW) >>>\n").append("엘라스틱서치 지연으로 OFF 서버 발생\n");
        mtMap.get("swState").setBeforeState(changeSWStateMap);

        for (String urlStr : list) {
            try {
                mtMap.forEach((key, value) -> value.setUseYn(true));

                log.info("healthcheck urlStr = {} ", urlStr);
                ResponseEntity<String> response = restTemplate.exchange("http://" + urlStr + "/servlet/monitor?mode=swState", HttpMethod.GET, entity, String.class);
                HttpStatus statusCode = response.getStatusCode();
                String body = response.getBody().trim();

                if(body.contains("INFLOW_AUID_USE_YN=false")) {
                    mtMap.get("inflowAuidState").setUseYn(false);
                }
                if(body.contains("SW_SERVICE_USE_YN=false")) {
                    mtMap.get("swState").setUseYn(false);
                }

                mtMap.forEach((key, value) -> {
                    if (value.getBeforeState().get(urlStr) == null) {
                        value.getBeforeState().put(urlStr, value.isUseYn());
                        if(!value.isUseYn()) {
                            value.setSendCheck(true);
                        }
                    }

                    if(!value.isUseYn() && value.getBeforeState().get(urlStr) != value.isUseYn()) {
                        value.setSendCheck(true);
                        value.getBeforeState().put(urlStr, value.isUseYn());
                    }

                    if(value.isUseYn()) {
                        value.getBeforeState().put(urlStr, value.isUseYn());
                        value.setState("ON");
                    } else {
                        value.setState("OFF");
                        value.setCount(value.getCount() + 1);
                    }

                    value.getSb().append(urlStr);
                    if("inflowAuidState".equals(key)) {
                        value.getSb().append(" :: 기사 유입 AUID 수집 상태 => ").append(value.getState()).append("\n");
                    } else if("swState".equals(key)) {
                        value.getSb().append(" :: 소셜 오디언스 상태 => ").append(value.getState()).append("\n");
                    }
                });
                log.debug(urlStr);
            } catch (Exception e) {
            }
        }
        mtMap.forEach((key, value) ->{
            value.getSb().append("OFF 된 서버 수 : ").append(value.getCount()).append("\n");
            if(value.isSendCheck()) {
                restTemplate.exchange(telegramMtState + value.getSb().toString() , HttpMethod.GET, entity, String.class);
            }
        });
    }

    public static List<String> getMediaServerInfo() {
        List<String> list = new ArrayList<>();

        // list.add("www.mediacategory.com");
        list.add("10.251.0.2");
        // list.add("10.251.0.3");
        list.add("10.251.0.4");
        list.add("10.251.0.5");
        list.add("10.251.0.6");
        list.add("10.251.0.7");
        list.add("10.251.0.8");
        list.add("10.251.0.9");
        list.add("10.251.0.10");
        list.add("10.251.0.11");
        list.add("10.251.0.12");
        list.add("10.251.0.13");
        list.add("10.251.0.14");
        list.add("10.251.0.15");
        list.add("10.251.0.16");
        list.add("10.251.0.17");
        list.add("10.251.0.18");
        list.add("10.251.0.19");
        list.add("10.251.0.20");
        list.add("10.251.0.21");
        list.add("10.251.0.22");
        list.add("10.251.0.23");
        list.add("10.251.0.24");
        list.add("10.251.0.25");
        list.add("10.251.0.26");
        list.add("10.251.0.146");
        list.add("10.251.0.147");
        list.add("10.251.0.148");
        list.add("10.251.0.149");

        // 네이티브
        list.add("10.251.0.122");
//        list.add("10.251.0.123");
        list.add("10.251.0.124");
        list.add("10.251.0.125");
        list.add("10.251.0.126");
        list.add("10.251.0.127");
        return list;
    }
}
