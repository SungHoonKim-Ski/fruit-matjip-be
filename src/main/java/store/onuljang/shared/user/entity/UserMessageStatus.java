package store.onuljang.shared.user.entity;

public enum UserMessageStatus {
    PENDING, // 아직 안 보낸 상태
    SENT, // 서버가 보내기로 확정/전달한 상태
    EXPIRED // 유효기간 지나서 못 쓰는 상태
}
