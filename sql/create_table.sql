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
alter table user add column tags varchar(1024) null comment '标签列表';


-- auto-generated definition
create table tag
(
    id         bigint auto_increment
        primary key,
    tagName    varchar(256)                       null comment '标签名称',
    userId     bigint                             null comment '用户id',
    parentId   bigint                             null comment '父标签id',
    isParent   tinyint                            null comment '0：不是, 1：是',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete   tinyint  default 0                 not null comment '是否删除'
)
    comment '标签';

create index unique_tagName on tag (tagName);