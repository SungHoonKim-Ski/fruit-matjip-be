package store.onuljang.worker.scheduler.courier;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TrackingResultMessage(
    @JsonProperty("displayCode") String displayCode,
    @JsonProperty("status") String status,
    @JsonProperty("location") String location,
    @JsonProperty("timestamp") String timestamp
) {}
