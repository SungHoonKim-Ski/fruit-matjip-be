alter table reservations
    add sell_price decimal(10) not null default(0);
update reservations
set sell_price = amount / quantity where sell_price = 0;