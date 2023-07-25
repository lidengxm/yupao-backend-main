## 项目简介

 伙伴汇，一个让大家可以找到志同道合的学习伙伴的移动端网站（APP 风格）。包括用户登录注册、更新个人信息、按标签搜索用户、推荐相似用户、组队等功能。



## 项目页面展示：

伙伴汇主页：

![image-20230716112210855](https://alylmengbucket.oss-cn-nanjing.aliyuncs.com/pictures/202307161122978.png)



**搜索伙伴：**

![image-20230716112326656](https://alylmengbucket.oss-cn-nanjing.aliyuncs.com/pictures/202307161123695.png)

**组队功能：**

![image-20230716112406963](https://alylmengbucket.oss-cn-nanjing.aliyuncs.com/pictures/202307161124009.png)



**创建队伍：**

![image-20230716112509972](https://alylmengbucket.oss-cn-nanjing.aliyuncs.com/pictures/202307161125006.png)



**后台管理页：**

![image-20230716112531487](https://alylmengbucket.oss-cn-nanjing.aliyuncs.com/pictures/202307161125528.png)



**用户更新页面：**

![image-20230716112548937](https://alylmengbucket.oss-cn-nanjing.aliyuncs.com/pictures/202307161125979.png)



## 项目背景

伙伴汇项目基本覆盖了企业开发中常见的需求以及对应的解决方案，比如登录注册、批量数据导入、信息检索展示、定时任务、资源抢占等。并且涵盖了分布式、并发编程、锁、事务、缓存、性能优化、幂等性、数据一致性、大数据、算法等后端程序员必须了解的知识与实践。

从需求分析、技术选型、系统设计、前后端开发再到最后上线，整个项目全部过程。



## 技术选型

### 前端

- Vue 3
- Vant UI 组件库
- TypeScript
- Vite 脚手架
- Axios 请求库

### 后端

- Java SpringBoot 2.7.x 框架
- MySQL 数据库
- MyBatis-Plus
- MyBatis X 自动生成
- Redis 缓存（Spring Data Redis 等多种实现方式）
- Redisson 分布式锁
- Easy Excel 数据导入
- Spring Scheduler 定时任务
- Swagger + Knife4j 接口文档
- Gson：JSON 序列化库
- 相似度匹配算法





## 项目大纲

1. 项目简介和计划
2. 需求分析
3. 技术选型（各技术作用讲解）
4. 前端项目初始化 

1. 1. 脚手架
   2. 组件 / 类库引入

1. 前端页面设计及通用布局开发
2. 后端数据库表设计
3. 按标签搜索用户功能 

1. 1. 前端开发
   2. 后端开发
   3. 性能分析
   4. 接口调试

1. Swagger + Knife4j 接口文档整合
2. 后端分布式登录改造（Session 共享）
3. 用户登录功能开发
4. 修改个人信息功能开发
5. 主页开发（抽象通用列表组件）
6. 批量导入数据功能 

1. 1. 几种方案介绍及对比
   2. 测试及性能优化（并发编程）

1. 主页性能优化 

1. 1. 缓存和分布式缓存讲解
   2. Redis 讲解
   3. 缓存开发和注意事项
   4. 缓存预热设计与实现
   5. 定时任务介绍和实现
   6. 锁 / 分布式锁介绍
   7. 分布式锁注意事项讲解
   8. Redisson 分布式锁实战
   9. 控制定时任务执行的几种方案介绍及对比

1. 组队功能 

1. 1. 需求分析
   2. 系统设计
   3. 多个接口开发及测试
   4. 前端多页面开发
   5. 权限控制

1. 随机匹配功能 

1. 1. 匹配算法介绍及实现
   2. 性能优化及测试

1. 项目优化及完善
2. 免备案方式上线前后端











