package net.mobon.healthcheck.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import net.mobon.healthcheck.api.model.TelegramMessage;

@Service
public class MessageService {
	private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    @Value("${telegram.url}")
    private String telegramUrl;
    @Value("${telegram.url.billing}")
    private String telegramUrlBilling;

    @Autowired
    private RestTemplate restTemplate;


    public void sendMessageGroupTelegram(TelegramMessage message){

        restTemplate.exchange(telegramUrl + message.toString(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);

    }

    public void sendMessageBillingTelegram(TelegramMessage message){

        restTemplate.exchange(telegramUrlBilling + message.toString(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);

    }

}
