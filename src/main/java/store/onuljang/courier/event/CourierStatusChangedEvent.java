package store.onuljang.courier.event;

import store.onuljang.shared.entity.enums.CourierOrderStatus;

public record CourierStatusChangedEvent(
    String displayCode,
    CourierOrderStatus newStatus,
    String receiverPhone,
    String receiverName,
    String courierCompanyName,
    String waybillNumber,
    String productSummary
) {}
