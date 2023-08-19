package net.mobon.healthcheck.api.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

@Slf4j
@Service
public class HCAccessLogService {

    @Value("${telegram.url.accesslog}")
    private String telegramUrl;

    @Value("${elastic.url.accesslogcount}")
    private String accessLogCountElasticUrl;

    @Autowired
    private RestTemplate restTemplate;

    public void accessLogHealthCheck() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(accessLogCountElasticUrl, HttpMethod.GET, entity, String.class);
        HttpStatus statusCode = response.getStatusCode();
        Calendar calendar = new GregorianCalendar();
        calendar.add(Calendar.DATE , -1);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        String yesterday = simpleDateFormat.format(calendar.getTime());

        if (HttpStatus.OK.equals(statusCode)) {
            JsonParser jsonParser = new JsonParser();
            JsonElement jsonElement = jsonParser.parse(response.getBody());
            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                JsonElement jsonElementStatus = jsonObject.get("count");
                BigInteger bigIntLogCount = jsonElementStatus.getAsBigInteger();
                String logCount = NumberFormat.getNumberInstance(Locale.US).format(bigIntLogCount);
                restTemplate.exchange(telegramUrl + yesterday + " 액세스 로그 처리 수 = " + logCount, HttpMethod.GET, entity, String.class);
            }
        } else {
            restTemplate.exchange(telegramUrl + yesterday + " AccessLog 검색엔진 응답실패", HttpMethod.GET, entity, String.class);
        }
    }
}
