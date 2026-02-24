package store.onuljang.courier.scheduler;

public record TrackingResultMessage(String displayCode, String status, String location, String timestamp) {}
