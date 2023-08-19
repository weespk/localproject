package net.mobon.healthcheck.api.util;

import jdk.nashorn.internal.objects.annotations.Constructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import net.mobon.healthcheck.api.util.MTTelegramUtil;

import javax.annotation.PostConstruct;

@Component
public class TelegramUtil {
    @Autowired
    MTTelegramUtil mtTelegramUtil;

    @PostConstruct
    private void init() {
        TelegramBotsApi telegramBotApi = new TelegramBotsApi();
        try {
            telegramBotApi.registerBot(mtTelegramUtil);
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }

    }
}
