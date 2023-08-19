package net.mobon.healthcheck.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @author kim se joung
 *
 */
@Getter
@Setter
@ToString
public class ClickViewData {
    @JsonProperty("totalLag")
    private Long totalLag;
    @JsonProperty("percentageCovered")
    private Long percentageCovered;
    @JsonProperty("partitionOffsets")
    private List<Long> partitionOffsets = null;
    @JsonProperty("partitionLatestOffsets")
    private List<Long> partitionLatestOffsets = null;
    @JsonProperty("owners")
    private List<String> owners = null;
}
