package net.mobon.healthcheck.api.service;

import lombok.extern.slf4j.Slf4j;
import net.mobon.healthcheck.api.model.StateData;
import org.aspectj.bridge.IMessage;
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
public class SWTelegramService {
    @Value("${telegram.url.mtUseYn}")
    private String telegramSWState;

    @Autowired
    private RestTemplate restTemplate;

    public void swTelegramState(String code, boolean checkAuto) {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        List<String> list = HealthCheckService.getMediaServerInfo();

        Map<String, StateData> swMap = new HashMap<String, StateData>();

        if("27".equals(code) || "28".equals(code)) {
            swMap.put("inflowAuidState", new StateData());
            swMap.get("inflowAuidState").setSb(new StringBuilder());
            if(checkAuto) {
                swMap.get("inflowAuidState").getSb().append("<<< 기사 유입 AUID 수집 자동 기능 ON >>>\n");
            }
            swMap.get("inflowAuidState").getSb().append("<<< 기사 유입 AUID 수집 상태 확인 >>>\n");
            swMap.get("inflowAuidState").setCount(0);
        }

        if("27".equals(code) || "37".equals(code)) {
            swMap.put("swState", new StateData());
            swMap.get("swState").setSb(new StringBuilder());
            if(checkAuto) {
                swMap.get("swState").getSb().append("<<< 소셜오디언스 자동 기능 ON >>>\n");
            }
            swMap.get("swState").getSb().append("<<< 소셜오디언스 수집 상태 확인 >>>\n");
            swMap.get("swState").setCount(0);
        }

        for (String urlStr : list) {
            try {
                swMap.forEach((key, value) -> value.setUseYn(true));

                log.info("healthcheck urlStr = {} ", urlStr);
                ResponseEntity<String> response = restTemplate.exchange("http://" + urlStr + "/servlet/monitor?mode=swState", HttpMethod.GET, entity, String.class);
                HttpStatus statusCode = response.getStatusCode();
                String body = response.getBody().trim();

                if("27".equals(code) || "28".equals(code)) {
                    if (body.contains("INFLOW_AUID_USE_YN=false")) {
                        swMap.get("inflowAuidState").setUseYn(false);
                    }
                }
                if("27".equals(code) || "37".equals(code)) {
                    if (body.contains("SW_SERVICE_USE_YN=false")) {
                        swMap.get("swState").setUseYn(false);
                    }
                }

                swMap.forEach((key, value) -> {
                    if(value.isUseYn()) {
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
                log.error(e.getMessage());
            }
        }
        swMap.forEach((key, value) ->{
            value.getSb().append("OFF 된 서버 수 : ").append(value.getCount()).append("\n");
            restTemplate.exchange(telegramSWState + value.getSb().toString() , HttpMethod.GET, entity, String.class);
        });
    }

    public void swTelegramOnOff(String code) {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        List<String> list = HealthCheckService.getMediaServerInfo();

        boolean state = true;

        Map<String, StateData> swMap = new HashMap<String, StateData>();

        if("31".equals(code) || "32".equals(code) || "39".equals(code)) {
            state = false;
        }

        if("29".equals(code) || "30".equals(code) || "31".equals(code) || "32".equals(code)) {
            swMap.put("inflowAuidState", new StateData());
            swMap.get("inflowAuidState").setSb(new StringBuilder());
            swMap.get("inflowAuidState").getSb().append("<<< 기사 유입 AUID 수집 상태 확인 >>>\n");
            swMap.get("inflowAuidState").setCount(0);
        }

        if("29".equals(code) || "31".equals(code) || "38".equals(code) || "39".equals(code)) {
            swMap.put("swState", new StateData());
            swMap.get("swState").setSb(new StringBuilder());
            swMap.get("swState").getSb().append("<<< 소셜오디언스(SW) 상태 확인 >>>\n");
            swMap.get("swState").setCount(0);
        }

        for (String urlStr : list) {
            try {
                swMap.forEach((key, value) -> value.setUseYn(true));

                log.info("healthcheck urlStr = {} ", urlStr);
                ResponseEntity<String> response;
                if("30".equals(code) || "32".equals(code)) {
                    response = restTemplate.exchange("http://" + urlStr + "/servlet/monitor?mode=swReload" + "&gubun=inflowAuid&state=" + state, HttpMethod.GET, entity, String.class);
                } else if("38".equals(code) || "39".equals(code)) {
                    response = restTemplate.exchange("http://" + urlStr + "/servlet/monitor?mode=swReload" + "&gubun=service&state=" + state, HttpMethod.GET, entity, String.class);
                } else {
                    response = restTemplate.exchange("http://" + urlStr + "/servlet/monitor?mode=swReload" + "&gubun=all&state=" + state, HttpMethod.GET, entity, String.class);
                }
                HttpStatus statusCode = response.getStatusCode();
                String body = response.getBody().trim();

                if("29".equals(code) || "30".equals(code) || "31".equals(code) || "32".equals(code)) {
                    if (body.contains("INFLOW_AUID_USE_YN=false")) {
                        swMap.get("inflowAuidState").setUseYn(false);
                    }
                }
                if("29".equals(code) || "31".equals(code) || "38".equals(code) || "39".equals(code)) {
                    if (body.contains("SW_SERVICE_USE_YN=false")) {
                        swMap.get("swState").setUseYn(false);
                    }
                }

                swMap.forEach((key, value) -> {
                    if(value.isUseYn()) {
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
                log.error(e.getMessage());
            }
        }
        swMap.forEach((key, value) ->{
            value.getSb().append("OFF 된 서버 수 : ").append(value.getCount()).append("\n");
            restTemplate.exchange(telegramSWState + value.getSb().toString() , HttpMethod.GET, entity, String.class);
        });
    }

    public void swUseServerInfo() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        List<String> list = HealthCheckService.getMediaServerInfo();

        StringBuffer sb = new StringBuffer();
        sb.append("<<< 기사 유입 AU_ID 수집 서버 프로퍼티 정보 >>>\n");
        for (String urlStr : list) {
            try {
                log.info("healthcheck urlStr = {} ", urlStr);
                ResponseEntity<String> response = restTemplate.exchange("http://" + urlStr + "/servlet/monitor?mode=swState", HttpMethod.GET, entity, String.class);
                HttpStatus statusCode = response.getStatusCode();
                String body = response.getBody().trim();

                String message = body.substring(body.indexOf("INFLOW_AUID_SERVER_NO="), body.length());
                message = message.substring(0, message.indexOf("<br>"));
                sb.append(urlStr);
                sb.append(":: ").append(message).append("\n");
                log.debug(urlStr);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        restTemplate.exchange(telegramSWState + sb.toString() , HttpMethod.GET, entity, String.class);
    }

}
