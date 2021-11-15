create table if not exists mup_itemsort (
    id integer primary key autoincrement,
    player varchar(16) not null,
);

create table if not exists mup_gallery (
    id integer primary key autoincrement,
    owner varchar(16) not null,
    sort_num integer,
    item blob not null,
    placed timedate not null,
    lock_id varchar(16)
);

create table if not exists mup_gallery_userdata (
    id integer primary key autoincrement,
    player varchar(16) not null,
    unlocked_slots int,
    unlocked_borders blob,
    current_border varchar(40),
    viewed_galleries blob,
    liked_galleries blob
);