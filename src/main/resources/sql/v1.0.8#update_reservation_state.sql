alter table reservations
    modify status enum ('PENDING', 'CANCELED', 'PICKED', 'SELF_PICK', 'SELF_PICK_READY', 'NO_SHOW') default 'PENDING' not null;

alter table reservations drop column is_no_show;
