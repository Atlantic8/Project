##### 运行环境
- windows10 专业版
- android studio， sdk最低为23
- 服务器为tomcat8，servlet编程
- mysql服务器也需要安装，两个数据库：cipher_key和project_spartan

其中程序需要用到的数据表格创建方法为：
##### cipher_key数据库
- service_key

```java
CREATE TABLE `service_key` (
  `start_time` int(11) NOT NULL,
  `end_time` int(11) NOT NULL,
  `tag` varchar(45) NOT NULL,
  `rsa_public_key` varchar(1030) NOT NULL,
  `rsa_private_key` varchar(1030) NOT NULL,
  PRIMARY KEY (`start_time`,`end_time`,`tag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='存储服务器上不同时间段的标签服务对应的密钥'

```

- user_info

```java
CREATE TABLE `user_info` (
  `username` varchar(20) NOT NULL COMMENT '用户名，主键',
  `password` varchar(45) NOT NULL COMMENT '登陆密码',
  `user_type` varchar(15) NOT NULL COMMENT '用户类型participant or querier',
  PRIMARY KEY (`username`,`user_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='存储用户登陆信息'

```

##### project_spartan数据库
- service_content

```java
CREATE TABLE `service_content` (
  `time` int(11) NOT NULL COMMENT '时间戳',
  `tag` varchar(1200) DEFAULT NULL,
  `data` varchar(1200) DEFAULT NULL,
  PRIMARY KEY (`time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='服务内容'

```

- service_shcedule

```java
CREATE TABLE `service_schedule` (
  `start_time` int(11) NOT NULL,
  `end_time` int(11) NOT NULL,
  `tag` varchar(45) NOT NULL COMMENT '明文存储的服务标签',
  PRIMARY KEY (`start_time`,`end_time`,`tag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='服务器提供的服务列表'

```

- user_info

```java
CREATE TABLE `user_info` (
  `username` varchar(20) NOT NULL COMMENT '用户名，主键',
  `password` varchar(45) NOT NULL COMMENT '登陆密码',
  `user_type` varchar(15) NOT NULL COMMENT '用户类型participant or querier',
  PRIMARY KEY (`username`,`user_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='存储用户登陆信息'

```
