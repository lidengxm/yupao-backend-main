-- auto-generated definition
create table user
(
    id           bigint auto_increment
        primary key comment '用户id',
    userName     varchar(256)                       null comment '用户名',
    userAccount  varchar(256)                       null comment '账号',
    userRole     int(255) default 0                 not null comment '用户角色：普通用户1,管理员1',
    userStatus   int      default 0                 null comment '状态',
    profile      varchar(512)                       null comment '个人简介',
    avatarUrl    varchar(1024)                      null comment '用户头像',
    gender       tinyint                            null comment '性别',
    userPassword varchar(512)                       not null comment '密码',
    email        varchar(512)                       null comment '邮箱',
    phone        varchar(128)                       null comment '电话',
    createTime   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    planetCode  varchar(512)                       not null comment '星球编号',
    tags         varchar(1024)                      null comment '标签列表 json格式'
)
    charset = utf8mb4 comment '用户表';


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
    charset = utf8mb4 comment '标签表';

create index unique_tagName
    on tag (tagName);

-- auto-generated definition
create table team
(
    id          bigint auto_increment comment 'id'
        primary key,
    name        varchar(256)                       not null comment '队伍名称',
    description varchar(1024)                      null comment '描述',
    maxNum      int      default 1                 not null comment '最大人数',
    expireTime  datetime                           null comment '过期时间',
    userId      bigint                             null comment '用户id',
    status      int      default 0                 not null comment '0 - 公开，1 - 私有，2 - 加密',
    password    varchar(512)                       null comment '密码',
    createTime  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete    tinyint  default 0                 not null comment '是否删除'
)
    comment '队伍表' collate = utf8mb4_unicode_ci;

-- auto-generated definition
create table user_team
(
    id         bigint auto_increment comment 'id'
        primary key,
    userId     bigint                             null comment '用户id',
    teamId     bigint                             null comment '队伍id',
    joinTime   datetime                           null comment '加入时间',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete   tinyint  default 0                 not null comment '是否删除',
    name       varchar(50)                        null comment '姓名'
)
    comment '用户队伍关系表' collate = utf8mb4_unicode_ci;