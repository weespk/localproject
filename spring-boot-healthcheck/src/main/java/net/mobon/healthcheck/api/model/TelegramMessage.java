package net.mobon.healthcheck.api.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TelegramMessage {

    private String serviceName;
    private String message;

    @Override
    public String toString() {
        return "["+this.getServiceName()+"] "+this.getMessage();
    }
}
