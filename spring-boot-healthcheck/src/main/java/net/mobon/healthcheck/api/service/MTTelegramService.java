package net.mobon.healthcheck.api.service;

import lombok.extern.slf4j.Slf4j;
import net.mobon.healthcheck.api.model.StateData;
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
public class MTTelegramService {
    @Value("${telegram.url.mtUseYn}")
    private String telegramMtState;

    @Autowired
    private RestTemplate restTemplate;

    public void mtTelegramState(String code, boolean checkAuto) {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        List<String> list = HealthCheckService.getMediaServerInfo();

        Map<String, StateData> mtMap = new HashMap<String, StateData>();

        if("14".equals(code) || "15".equals(code)) {
            mtMap.put("mtState", new StateData());
            mtMap.get("mtState").setSb(new StringBuilder());
            if(checkAuto) {
                mtMap.get("mtState").getSb().append("<<< 문맥 매칭 자동 기능 ON >>>\n");
            }
            mtMap.get("mtState").getSb().append("<<< 문맥 매칭 상태 확인 >>>\n");
            mtMap.get("mtState").setCount(0);
        }
        if("14".equals(code) || "16".equals(code)) {
            mtMap.put("inflow", new StateData());
            mtMap.get("inflow").setSb(new StringBuilder());
            if(checkAuto) {
                mtMap.get("inflow").getSb().append("<<< 유입 키워드 자동 기능 ON >>>\n");
            }
            mtMap.get("inflow").getSb().append("<<< 유입 키워드 상태 확인 >>>\n");
            mtMap.get("inflow").setCount(0);
        }
        if("14".equals(code) || "17".equals(code)) {
            mtMap.put("article", new StateData());
            mtMap.get("article").setSb(new StringBuilder());
            if(checkAuto) {
                mtMap.get("article").getSb().append("<<< 핵심 키워드 자동 기능 ON >>>\n");
            }
            mtMap.get("article").getSb().append("<<< 핵심 키워드 상태 확인 >>>\n");
            mtMap.get("article").setCount(0);
        }
        if("14".equals(code) || "18".equals(code)) {
            mtMap.put("product", new StateData());
            mtMap.get("product").setSb(new StringBuilder());
            if(checkAuto) {
                mtMap.get("product").getSb().append("<<< 상품 Like 자동 기능 ON >>>\n");
            }
            mtMap.get("product").getSb().append("<<< 상품 Like 상태 확인 >>>\n");
            mtMap.get("product").setCount(0);
        }

        for (String urlStr : list) {
            try {
                mtMap.forEach((key, value) -> value.setUseYn(true));

                log.info("healthcheck urlStr = {} ", urlStr);
                ResponseEntity<String> response = restTemplate.exchange("http://" + urlStr + "/servlet/monitor?mode=mtState", HttpMethod.GET, entity, String.class);
                HttpStatus statusCode = response.getStatusCode();
                String body = response.getBody().trim();

                if("14".equals(code) || "15".equals(code)) {
                    if (body.contains("MT_SERVICE_USE_YN=false")) {
                        mtMap.get("mtState").setUseYn(false);
                    }
                }
                if("14".equals(code) || "16".equals(code)) {
                    if (body.contains("MT_INFWLOW_KEYWORD_USE_YN=false")) {
                        mtMap.get("inflow").setUseYn(false);
                    }
                }
                if("14".equals(code) || "17".equals(code)) {
                    if (body.contains("MT_ARTICLE_KEYWORD_USE_YN=false")) {
                        mtMap.get("article").setUseYn(false);
                    }
                }
                if("14".equals(code) || "18".equals(code)) {
                    if (body.contains("MT_PCODE_LIKE_SEARCH_USE_YN=false")) {
                        mtMap.get("product").setUseYn(false);
                    }
                }

                mtMap.forEach((key, value) -> {
                    if(value.isUseYn()) {
                        value.setState("ON");
                    } else {
                        value.setState("OFF");
                        value.setCount(value.getCount() + 1);
                    }

                    value.getSb().append(urlStr);
                    if("mtState".equals(key)) {
                        value.getSb().append(" :: 문맥 매칭 상태 => ").append(value.getState()).append("\n");
                    }
                    if("article".equals(key)) {
                        value.getSb().append(" :: 핵심키워드 상태 => ").append(value.getState()).append("\n");
                    }
                    if("inflow".equals(key)) {
                        value.getSb().append(" :: 유입키워드 상태 => ").append(value.getState()).append("\n");
                    }
                    if("product".equals(key)){
                        value.getSb().append(" :: 상품 LIKE 상태 => ").append(value.getState()).append("\n");
                    }
                });
                log.debug(urlStr);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        mtMap.forEach((key, value) ->{
            value.getSb().append("OFF 된 서버 수 : ").append(value.getCount()).append("\n");
            restTemplate.exchange(telegramMtState + value.getSb().toString() , HttpMethod.GET, entity, String.class);
        });
    }

    public void mtTelegramOnOff(String code) {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        List<String> list = HealthCheckService.getMediaServerInfo();

        boolean state = true;

        Map<String, StateData> mtMap = new HashMap<String, StateData>();

        if("09".equals(code) || "10".equals(code) || "11".equals(code) || "12".equals(code) || "13".equals(code)) {
            state = false;
        }

        if("04".equals(code) || "05".equals(code) || "09".equals(code) || "10".equals(code)) {
            mtMap.put("mtState", new StateData());
            mtMap.get("mtState").setSb(new StringBuilder());
            mtMap.get("mtState").getSb().append("<<< 문맥 매칭 상태 확인 >>>\n");
            mtMap.get("mtState").setCount(0);
        }
        if("04".equals(code) || "06".equals(code) || "09".equals(code) || "11".equals(code)) {
            mtMap.put("inflow", new StateData());
            mtMap.get("inflow").setSb(new StringBuilder());
            mtMap.get("inflow").getSb().append("<<< 유입 키워드 상태 확인 >>>\n");
            mtMap.get("inflow").setCount(0);
        }
        if("04".equals(code) || "07".equals(code) || "09".equals(code) || "12".equals(code)) {
            mtMap.put("article", new StateData());
            mtMap.get("article").setSb(new StringBuilder());
            mtMap.get("article").getSb().append("<<< 핵심 키워드 상태 확인 >>>\n");
            mtMap.get("article").setCount(0);
        }
        if("04".equals(code) || "08".equals(code) || "09".equals(code) || "13".equals(code)) {
            mtMap.put("product", new StateData());
            mtMap.get("product").setSb(new StringBuilder());
            mtMap.get("product").getSb().append("<<< 상품 Like 상태 확인 >>>\n");
            mtMap.get("product").setCount(0);
        }

        for (String urlStr : list) {
            try {
                mtMap.forEach((key, value) -> value.setUseYn(true));

                log.info("healthcheck urlStr = {} ", urlStr);
                ResponseEntity<String> response;
                if("05".equals(code) || "10".equals(code)) {
                    response = restTemplate.exchange("http://" + urlStr + "/servlet/monitor?mode=mtReload" + "&gubun=service&state=" + state, HttpMethod.GET, entity, String.class);
                } else if("06".equals(code) || "11".equals(code)) {
                    response = restTemplate.exchange("http://" + urlStr + "/servlet/monitor?mode=mtReload" + "&gubun=inflow&state=" + state, HttpMethod.GET, entity, String.class);
                } else if("07".equals(code) || "12".equals(code)) {
                    response = restTemplate.exchange("http://" + urlStr + "/servlet/monitor?mode=mtReload" + "&gubun=article&state=" + state, HttpMethod.GET, entity, String.class);
                } else if("08".equals(code) || "13".equals(code)) {
                    response = restTemplate.exchange("http://" + urlStr + "/servlet/monitor?mode=mtReload" + "&gubun=like&state=" + state, HttpMethod.GET, entity, String.class);
                } else {
                    response = restTemplate.exchange("http://" + urlStr + "/servlet/monitor?mode=mtReload" + "&gubun=all&state=" + state, HttpMethod.GET, entity, String.class);
                }
                HttpStatus statusCode = response.getStatusCode();
                String body = response.getBody().trim();

                if("04".equals(code) || "05".equals(code) || "09".equals(code) || "10".equals(code)) {
                    if (body.contains("MT_SERVICE_USE_YN=false")) {
                        mtMap.get("mtState").setUseYn(false);
                    }
                }
                if("04".equals(code) || "06".equals(code) || "09".equals(code) || "11".equals(code)) {
                    if (body.contains("MT_INFWLOW_KEYWORD_USE_YN=false")) {
                        mtMap.get("inflow").setUseYn(false);
                    }
                }
                if("04".equals(code) || "07".equals(code) || "09".equals(code) || "12".equals(code)) {
                    if (body.contains("MT_ARTICLE_KEYWORD_USE_YN=false")) {
                        mtMap.get("article").setUseYn(false);
                    }
                }
                if("04".equals(code) || "08".equals(code) || "09".equals(code) || "13".equals(code)) {
                    if (body.contains("MT_PCODE_LIKE_SEARCH_USE_YN=false")) {
                        mtMap.get("product").setUseYn(false);
                    }
                }

                mtMap.forEach((key, value) -> {
                    if(value.isUseYn()) {
                        value.setState("ON");
                    } else {
                        value.setState("OFF");
                        value.setCount(value.getCount() + 1);
                    }

                    value.getSb().append(urlStr);
                    if("mtState".equals(key)) {
                        value.getSb().append(" :: 문맥 매칭 상태 => ").append(value.getState()).append("\n");
                    }
                    if("article".equals(key)) {
                        value.getSb().append(" :: 핵심키워드 상태 => ").append(value.getState()).append("\n");
                    }
                    if("inflow".equals(key)) {
                        value.getSb().append(" :: 유입키워드 상태 => ").append(value.getState()).append("\n");
                    }
                    if("product".equals(key)){
                        value.getSb().append(" :: 상품 LIKE 상태 => ").append(value.getState()).append("\n");
                    }
                });
                log.debug(urlStr);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        mtMap.forEach((key, value) ->{
            value.getSb().append("OFF 된 서버 수 : ").append(value.getCount()).append("\n");
            restTemplate.exchange(telegramMtState + value.getSb().toString() , HttpMethod.GET, entity, String.class);
        });
    }

}
