alter table products
    modify price decimal(10) not null;

alter table reservations
    modify amount decimal(15) not null;
