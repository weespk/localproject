package net.mobon.healthcheck.api.util;

import lombok.extern.slf4j.Slf4j;
import net.mobon.healthcheck.api.service.HealthCheckService;
import net.mobon.healthcheck.api.service.MTTelegramService;
import net.mobon.healthcheck.api.service.SWTelegramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class MTTelegramUtil extends TelegramLongPollingBot {

    @Autowired
    MTTelegramService mtTelegramService;
    @Autowired
    SWTelegramService swTelegramService;

    @Value("${telegram.token.mt}")
    String botToken;

    @Value("${telegram.name.mt}")
    String botName;

    /**
     * Method for receiving messages.
     * @param update Contains a message from the user.
     */
    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage()) {
            if("/".equals(update.getMessage().getText())) {
                sendMsg(update.getMessage().getChatId().toString());
            } else if("/start".equals(update.getMessage().getText())) {
                setStateButton(update.getMessage().getChatId().toString());
            }
        } else  if(update.hasCallbackQuery()) {
            String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
            String code = update.getCallbackQuery().getData();
            switch (code) {
                case "01" : setMtStateInfoButton(chatId); break;
                case "03" : setMtStateOffButton(chatId); break;

                case "02" :
                case "04" :
                case "05" :
                case "06" :
                case "07" :
                case "08" :
                    sendMtMsg(chatId);
                    break;
                case "09" :
                case "10" :
                case "11" :
                case "12" :
                case "13" :
                    mtTelegramService.mtTelegramOnOff(code);
                    break;
                case "14" :
                case "15" :
                case "16" :
                case "17" :
                case "18" :
                    mtTelegramService.mtTelegramState(code, false);
                    break;
                case "22" :
                    setMtStateButton(chatId);
                    break;
                case "23" :
                    setSwStateButton(chatId);
                    break;
                case "24" :
                    setSwStateInfoButton(chatId);
                    break;
                case "25" :
                    setSwStateOnButton(chatId);
                    break;
                case "26" :
                    setSwStateOffButton(chatId);
                    break;
                case "27" :
                case "28" :
                case "37":
                    swTelegramService.swTelegramState(code, false);
                    break;
                case "29" :
                case "30" :
                case "31" :
                case "32" :
                case "38" :
                case "39" :
                    swTelegramService.swTelegramOnOff(code);
                    break;
                case "36":
                    swTelegramService.swUseServerInfo();
                    break;
            }
        }
    }

    /**
     * This method returns the bot's name, which was specified during registration.
     * @return bot name
     */
    @Override
    public String getBotUsername() {
        return botName;
    }

    /**
     * This method returns the bot's token for communicating with the Telegram server
     * @return the bot's token
     */
    @Override
    public String getBotToken() {
        return botToken;
    }

    /**
     * Method for creating a message and sending it.
     * @param chatId chat id
     */
    public void sendMsg(String chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        String info = "/start\n명령어로 실행 가능합니다";
        sendMessage.setText(info);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Exception: ", e.toString());
        }
    }

    private void setStateButton(String chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText("서비스 선택");

        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> buttons1 = new ArrayList<>();
        List<InlineKeyboardButton> buttons2 = new ArrayList<>();

        buttons1.add(new InlineKeyboardButton().setText("문맥 매칭").setCallbackData("22"));
        buttons2.add(new InlineKeyboardButton().setText("소셜 오디언스").setCallbackData("23"));

        buttons.add(buttons1);
        buttons.add(buttons2);

        InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
        markupKeyboard.setKeyboard(buttons);
        sendMessage.setReplyMarkup(markupKeyboard);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Exception: ", e.toString());
        }
    }


    /**
     * 문맥 매칭 기능 ON, OFF, 상태 확인 버튼 생성 API
     * @param chatId
     */
    private void setMtStateButton(String chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText("문맥 기능 선택");

        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> buttons1 = new ArrayList<>();
        List<InlineKeyboardButton> buttons2 = new ArrayList<>();
        List<InlineKeyboardButton> buttons3 = new ArrayList<>();

        buttons1.add(new InlineKeyboardButton().setText("상태 확인").setCallbackData("01"));
        buttons2.add(new InlineKeyboardButton().setText("기능 ON").setCallbackData("02"));
        buttons3.add(new InlineKeyboardButton().setText("기능 OFF").setCallbackData("03"));

        buttons.add(buttons1);
        buttons.add(buttons2);
        buttons.add(buttons3);

        InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
        markupKeyboard.setKeyboard(buttons);
        sendMessage.setReplyMarkup(markupKeyboard);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Exception: ", e.toString());
        }
    }

    /**
     * 문맥 매칭 기능 ON, OFF, 상태 확인 버튼 생성 API
     * @param chatId
     */
    private void setSwStateButton(String chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText("소셜 오디언스 선택");

        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> buttons1 = new ArrayList<>();
        List<InlineKeyboardButton> buttons2 = new ArrayList<>();
        List<InlineKeyboardButton> buttons3 = new ArrayList<>();

        buttons1.add(new InlineKeyboardButton().setText("상태 확인").setCallbackData("24"));
        buttons2.add(new InlineKeyboardButton().setText("기능 ON").setCallbackData("25"));
        buttons3.add(new InlineKeyboardButton().setText("기능 OFF").setCallbackData("26"));

        buttons.add(buttons1);
        buttons.add(buttons2);
        buttons.add(buttons3);

        InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
        markupKeyboard.setKeyboard(buttons);
        sendMessage.setReplyMarkup(markupKeyboard);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Exception: ", e.toString());
        }
    }

    /**
     * 문맥 매칭 기능 ON 텔레그램 버튼 생성 메소드
     * @param chatId
     */
    private void setMtStateOnButton(String chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText("ON 할 기능 선택");

        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> buttons1 = new ArrayList<>();
        List<InlineKeyboardButton> buttons2 = new ArrayList<>();
        List<InlineKeyboardButton> buttons3 = new ArrayList<>();
        List<InlineKeyboardButton> buttons4 = new ArrayList<>();

        buttons1.add(new InlineKeyboardButton().setText("전체 기능 ON").setCallbackData("04"));
        buttons2.add(new InlineKeyboardButton().setText("문맥 기능 ON").setCallbackData("05"));
        buttons2.add(new InlineKeyboardButton().setText("유입키워드 ON").setCallbackData("06"));
        buttons3.add(new InlineKeyboardButton().setText("핵심키워드 ON").setCallbackData("07"));
        buttons3.add(new InlineKeyboardButton().setText("상품LIKE ON").setCallbackData("08"));

        buttons.add(buttons1);
        buttons.add(buttons2);
        buttons.add(buttons3);
        buttons.add(buttons4);

        InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
        markupKeyboard.setKeyboard(buttons);
        sendMessage.setReplyMarkup(markupKeyboard);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Exception: ", e.toString());
        }
    }

    /**
     * 문맥 매칭 기능 OFF 텔레그램 버튼 생성 메소드
     * @param chatId
     */
    private void setMtStateOffButton(String chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText("OFF 할 기능 선택");

        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> buttons1 = new ArrayList<>();
        List<InlineKeyboardButton> buttons2 = new ArrayList<>();
        List<InlineKeyboardButton> buttons3 = new ArrayList<>();
        List<InlineKeyboardButton> buttons4 = new ArrayList<>();

        buttons1.add(new InlineKeyboardButton().setText("전체 기능 OFF").setCallbackData("09"));
        buttons2.add(new InlineKeyboardButton().setText("문맥 기능 OFF").setCallbackData("10"));
        buttons2.add(new InlineKeyboardButton().setText("유입키워드 OFF").setCallbackData("11"));
        buttons3.add(new InlineKeyboardButton().setText("핵심키워드 OFF").setCallbackData("12"));
        buttons3.add(new InlineKeyboardButton().setText("상품LIKE OFF").setCallbackData("13"));

        buttons.add(buttons1);
        buttons.add(buttons2);
        buttons.add(buttons3);
        buttons.add(buttons4);

        InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
        markupKeyboard.setKeyboard(buttons);
        sendMessage.setReplyMarkup(markupKeyboard);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Exception: ", e.toString());
        }
    }

    /**
     * 문맥 매칭 상태 확인 텔레그램 버튼 생성 메소드
     * @param chatId
     */
    private void setMtStateInfoButton(String chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText("상태확인 종류 선택");

        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> buttons1 = new ArrayList<>();
        List<InlineKeyboardButton> buttons2 = new ArrayList<>();
        List<InlineKeyboardButton> buttons3 = new ArrayList<>();
        List<InlineKeyboardButton> buttons4 = new ArrayList<>();

        buttons1.add(new InlineKeyboardButton().setText("전체 기능 상태 확인").setCallbackData("14"));
        buttons2.add(new InlineKeyboardButton().setText("문맥 매칭 상태 확인").setCallbackData("15"));
        buttons2.add(new InlineKeyboardButton().setText("유입키워드 상태 확인").setCallbackData("16"));
        buttons3.add(new InlineKeyboardButton().setText("핵심키워드 상태 확인").setCallbackData("17"));
        buttons3.add(new InlineKeyboardButton().setText("상품LIKE 상태 확인").setCallbackData("18"));

        buttons.add(buttons1);
        buttons.add(buttons2);
        buttons.add(buttons3);
        buttons.add(buttons4);

        InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
        markupKeyboard.setKeyboard(buttons);
        sendMessage.setReplyMarkup(markupKeyboard);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Exception: ", e.toString());
        }
    }

    /**
     * 소셜 오디언스 매칭 상태 확인 텔레그램 버튼 생성 메소드
     * @param chatId
     */
    private void setSwStateInfoButton(String chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText("상태확인 종류 선택");

        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> buttons1 = new ArrayList<>();
        List<InlineKeyboardButton> buttons2 = new ArrayList<>();
        List<InlineKeyboardButton> buttons3 = new ArrayList<>();
        List<InlineKeyboardButton> buttons4 = new ArrayList<>();


        buttons1.add(new InlineKeyboardButton().setText("전체 기능 상태 확인").setCallbackData("27"));
        buttons2.add(new InlineKeyboardButton().setText("기사 유입 AUID 상태 확인").setCallbackData("28"));
        buttons4.add(new InlineKeyboardButton().setText("프로퍼티 등록된 서버 정보").setCallbackData("36"));
        buttons3.add(new InlineKeyboardButton().setText("소셜 오디언스 상태 확인").setCallbackData("37"));

        buttons.add(buttons1);
        buttons.add(buttons2);
        buttons.add(buttons3);
        buttons.add(buttons4);

        InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
        markupKeyboard.setKeyboard(buttons);
        sendMessage.setReplyMarkup(markupKeyboard);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Exception: ", e.toString());
        }
    }

    /**
     * 소셜 오디언스 기능 ON 텔레그램 버튼 생성 메소드
     * @param chatId
     */
    private void setSwStateOnButton(String chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText("ON 할 기능 선택");

        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> buttons1 = new ArrayList<>();
        List<InlineKeyboardButton> buttons2 = new ArrayList<>();
        List<InlineKeyboardButton> buttons3 = new ArrayList<>();

        buttons1.add(new InlineKeyboardButton().setText("전체 기능 ON").setCallbackData("29"));
        buttons2.add(new InlineKeyboardButton().setText("기사 유입 AUID 수집 ON").setCallbackData("30"));
        buttons3.add(new InlineKeyboardButton().setText("소셜 오디언스 기능 ON").setCallbackData("38"));

        buttons.add(buttons1);
        buttons.add(buttons2);
        buttons.add(buttons3);

        InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
        markupKeyboard.setKeyboard(buttons);
        sendMessage.setReplyMarkup(markupKeyboard);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Exception: ", e.toString());
        }
    }

    /**
     * 소셜 오디언스 기능 OFF 텔레그램 버튼 생성 메소드
     * @param chatId
     */
    private void setSwStateOffButton(String chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText("OFF 할 기능 선택");

        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> buttons1 = new ArrayList<>();
        List<InlineKeyboardButton> buttons2 = new ArrayList<>();
        List<InlineKeyboardButton> buttons3 = new ArrayList<>();

        buttons1.add(new InlineKeyboardButton().setText("전체 기능 OFF").setCallbackData("31"));
        buttons2.add(new InlineKeyboardButton().setText("기사 유입 AUID 수집 OFF").setCallbackData("32"));
        buttons3.add(new InlineKeyboardButton().setText("소셜 오디언스 기능 OFF").setCallbackData("39"));

        buttons.add(buttons1);
        buttons.add(buttons2);
        buttons.add(buttons3);

        InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
        markupKeyboard.setKeyboard(buttons);
        sendMessage.setReplyMarkup(markupKeyboard);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Exception: ", e.toString());
        }
    }

    public void sendMtMsg(String chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        String info = "기능 ON 은 불가능합니다";
        sendMessage.setText(info);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Exception: ", e.toString());
        }
    }
}
