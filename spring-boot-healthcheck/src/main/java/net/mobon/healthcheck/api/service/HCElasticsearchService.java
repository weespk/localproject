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

@Slf4j
@Service
public class HCElasticsearchService {

    @Value("${telegram.url.elasticsearch}")
    private String telegramUrl;

    @Value("${elastic.url.cluster.health}")
    private String elasticHealthCheckUrl;

    @Autowired
    private RestTemplate restTemplate;

    public void elasticsearch() {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(elasticHealthCheckUrl, HttpMethod.GET, entity, String.class);
        HttpStatus statusCode = response.getStatusCode();
        if (HttpStatus.OK.equals(statusCode)) {
            JsonParser jsonParser = new JsonParser();
            JsonElement jsonElement = jsonParser.parse(response.getBody());
            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                JsonElement jsonElementStatus = jsonObject.get("status");
                String status = jsonElementStatus.getAsString();
                if ("red".equals(status)) {
                    restTemplate.exchange(telegramUrl + " 검색엔진 상태 [RED]", HttpMethod.GET, entity, String.class);
                }
            }
        } else {
            restTemplate.exchange(telegramUrl + " 검색엔진 응답실패", HttpMethod.GET, entity, String.class);
        }
    }
}
