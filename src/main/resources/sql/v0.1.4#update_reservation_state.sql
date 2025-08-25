alter table reservations
    modify status enum ('PENDING', 'CANCELED', 'PICKED', 'SELF_PICK', 'SELF_PICK_READY') default 'PENDING' not null;