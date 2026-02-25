package store.onuljang.courier.scheduler;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TrackingResultMessage(
    @JsonProperty("displayCode") String displayCode,
    @JsonProperty("status") String status,
    @JsonProperty("location") String location,
    @JsonProperty("timestamp") String timestamp
) {}
