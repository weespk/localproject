package net.mobon.healthcheck.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * @author hcson
 *
 */
@Data
public class StateData {
    private String state;
    private boolean useYn;
    private StringBuilder sb;
    private boolean sendCheck;
    private Map<String, Boolean> beforeState;
    private int count = 0;

}
