-- v2.11.3 : 관리자/사용자 API 로그에 client_ip 컬럼 추가
ALTER TABLE admin_logs
    ADD COLUMN client_ip VARCHAR(45) NULL;

ALTER TABLE user_logs
    ADD COLUMN client_ip VARCHAR(45) NULL;
