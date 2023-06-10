package io.ggammu.study.springboot.config.auth.dto;

import io.ggammu.study.springboot.domain.user.User;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class SessionUser implements Serializable {
    private String email;
    private String name;
    private String picture;

    public SessionUser(User user) {
        this.email = user.getEmail();
        this.name = user.getName();
        this.picture = user.getPicture();
    }
}
