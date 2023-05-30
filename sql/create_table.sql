-- auto-generated definition
create table user
(
    id          bigint auto_increment primary key,
    userName    varchar(256)                           null,
    userAccount varchar(256)                           null,
    role        int(255)     default 0                 not null comment '用户角色：普通用户1,管理员1',
    status      int          default 0                 null,
    avatarUrl   varchar(1024)                          null,
    gender      tinyint                                null,
    password    varchar(512)                           not null,
    email       varchar(512)                           null,
    phone       varchar(128)                           null,
    createTime  datetime     default CURRENT_TIMESTAMP null,
    updateTime  datetime     default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete    tinyint      default 0                 not null,
    plannetCode varchar(512) default '0'               not null comment '星球编号'
);

