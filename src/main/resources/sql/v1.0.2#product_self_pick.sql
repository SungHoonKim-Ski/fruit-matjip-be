alter table products
    add self_pick bool default true not null;

alter table products
    change is_visible visible tinyint(1) default 1 null;