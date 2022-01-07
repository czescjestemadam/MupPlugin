create table if not exists mup_itemsort (
    id integer primary key autoincrement,
    player varchar(16) not null
);

create table if not exists mup_gallery (
    id integer primary key autoincrement,
    owner varchar(16) not null,
    sort_num integer not null,
    item blob not null,
    amount tinyint not null,
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

create table if not exists mup_discord_linked (
    id integer primary key autoincrement,
    player varchar(16) not null,
    dc_id bigint not null,
    verification_code varchar(16),
    verified bool
);

create table if not exists mup_reports (
    id integer primary key autoincrement,
    from_player varchar(16) not null,
    type varchar(16) not null,
    player varchar(16),
    pos_world varchar(32),
    pos_x int,
    pos_y int,
    pos_z int,
    comment varchar(255),
    sent_at timedate not null,
    checked bool default false
);

create table if not exists mup_reports_blacklist (
    id integer primary key autoincrement,
    player varchar(16) not null,
    applied timedate not null,
    expires timedate,
    expired bool default false
);