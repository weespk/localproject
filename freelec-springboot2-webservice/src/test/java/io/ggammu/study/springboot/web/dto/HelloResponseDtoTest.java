package io.ggammu.study.springboot.web.dto;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HelloResponseDtoTest {
    @Test
    public void 롬복_기능_테스트() {
        // given
        String name = "test";
        int amount = 1000;
        // when
        HelloResponseDto dto = new HelloResponseDto(name, amount);
        // then
        assertEquals(dto.getName(), name);
        assertEquals(dto.getAmount(), amount);
        assertThat(dto.getName()).isEqualTo(name);
        assertThat(dto.getAmount()).isEqualTo(amount);
    }
}