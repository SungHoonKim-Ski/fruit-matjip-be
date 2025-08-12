alter table refresh_tokens
    modify issued_at TIMESTAMP not null;

alter table refresh_tokens
    modify expires_at TIMESTAMP not null;
