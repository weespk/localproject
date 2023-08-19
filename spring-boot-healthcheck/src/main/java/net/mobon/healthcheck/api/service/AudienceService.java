package net.mobon.healthcheck.api.service;

import net.mobon.healthcheck.api.model.ClickViewDataSummary;
import net.mobon.healthcheck.api.model.TelegramMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Service
public class AudienceService {
	private static final Logger logger = LoggerFactory.getLogger(AudienceService.class);

    @Value("${telegram.url}")
    private String telegramUrl;
    @Value("${telegram.url.billing}")
    private String telegramUrlBilling;

    @Value("${kafkamanager.url.audiencegroup}")
    private String kafkaGroupSummeryUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private MessageService messageService;


    public void audienceLagCheck(){

        TelegramMessage tm = new TelegramMessage();
        tm.setServiceName("AudienceService");

        ParameterizedTypeReference<ClickViewDataSummary> ptr = new ParameterizedTypeReference<ClickViewDataSummary>() {};

        ResponseEntity<ClickViewDataSummary> response = restTemplate.exchange(kafkaGroupSummeryUrl, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), ptr);

        if(response.getStatusCode().is2xxSuccessful()){
            long totalLag = response.getBody().getClickViewData().getTotalLag().longValue();
            if(totalLag > 1000000){
                tm.setMessage("lag가 많이 쌓였습니다. lagcnt = "+totalLag);
            }

        }else{
            tm.setMessage("서비스가 확인되지 않습니다. kafka Manager를 확인해주세요");
        }

        if(!StringUtils.isEmpty(tm.getMessage())){

          //  messageService.sendMessageBillingTelegram(tm);
            messageService.sendMessageGroupTelegram(tm);

        }


    }


}
