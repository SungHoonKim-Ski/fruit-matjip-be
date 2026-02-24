package store.onuljang.shared.entity.enums;

public enum CourierOrderStatus {
    PENDING_PAYMENT,
    PAID,
    ORDERING,
    ORDER_COMPLETED,
    IN_TRANSIT,
    DELIVERED,
    CANCELED,
    FAILED;

    public String getAdminDisplayName() {
        return switch (this) {
            case PENDING_PAYMENT -> "결제대기";
            case PAID -> "결제완료";
            case ORDERING -> "발주중";
            case ORDER_COMPLETED -> "발주완료";
            case IN_TRANSIT -> "배송중";
            case DELIVERED -> "배송완료";
            case CANCELED -> "취소";
            case FAILED -> "결제실패";
        };
    }

    public String getCustomerDisplayName() {
        return switch (this) {
            case PENDING_PAYMENT -> "결제대기";
            case PAID -> "결제완료";
            case ORDERING -> "상품준비중";
            case ORDER_COMPLETED -> "상품준비완료";
            case IN_TRANSIT -> "배송중";
            case DELIVERED -> "배송완료";
            case CANCELED -> "취소";
            case FAILED -> "결제실패";
        };
    }
}
