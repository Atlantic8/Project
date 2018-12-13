#### Project Spartan
##### 运行环境
- windows10 专业版
- android studio， sdk最低为23
- 服务器为tomcat8，servlet编程
- mysql服务器也需要安装，两个数据库：cipher_key（CA）和project_spartan（服务器）


##### 方案简介
本项目实现了一种高效的参与式感知社区信息系统，这是结合AES、RSA加密和基于Android系统的手机应用。参与式感知是由云端服务器收集由携有智能移动设备的用户提供的环境或社会数据，经过处理后反馈给信息请求者的系统。
本项目中的主体包括：
- 参与者：参与者根据时间和主题上传主题对应的内容data。
- 查询者：查询者查询固定时间段、主题的内容
- 服务器：服务器发布时间和主题列表。参与者根据这个列表上传自己的数据，查询者查询。
- 注册中心（CA）：  注册中心负责服务器、查询者、参与者的进程初始化工作，它负责生成和分发加密参数(如相关的公钥、私钥等)，在”查询”隐私和”汇报”隐私的保护中扮演着重要的角色

##### 数据设计
假定注册中心（CA）是诚实可信的，但是服务器却诚实不可信的，网络攻击可能存在。
###### CA
CA和服务器端都有一张存储用户信息的表user_info：

	username：用户名，主键
    password：登陆密码
    user_type：用户类型participant or querier

CA端还维护一张名为service_key的表，存储每个（时间、主题）[称之为服务]对应的公私钥对

	注：（时间、主题）对的形式为 (start_time, end_time, tag)元祖
    start_time
    end_time
    tag：tag意味标签，这里也可以理解为兴趣，问题等
    rsa_public_key
    rsa_private_key
    PRIMARY KEY (`start_time`,`end_time`,`tag`)

###### 服务器
除了user_info，服务器也存储了一个名为service_shcedule的表，记录（时间、主题）对（时间、主题对是由服务器创建的），这张表包括一下几个属性：

	start_time
    end_time
    tag

服务器端的另一张表是service_content，包含字段：

	time：时间戳、主键
    tag
    data：内容


##### 具体设计
###### 业务设计
参与者：
- 登录服务器，获取
- 获取服务列表
- 参与者选择列表中的服务，并从CA登录并获取该服务对应的密钥对（pk, aes_key）
  - 服务器生成aes_key的方法：aes_key = MD5(sk)，其中sk为pk对应的私钥
- 根据参与者的输入data，创建服务内容项
  - time为当前时间戳
  - 用pk加密data作为内容data1，aes_key当做tag
  - 用服务器的公钥server_pk加密data1得到content。注意这里两次加密之间应该使用BASE64编码，当然以后显示的时候要解码
  - 上传(time, tag, content)给服务器存储

查询者：
- 登录服务器
- 获取服务列表
- 参与者选择列表中感兴趣的服务
  - 从CA登录并获取该服务对应的密钥对，创建tag。点击向服务器查询，服务器根据tag查询出所有有次tag的服务内容并返回给客户端
  - 客户端列表显示
- 如果查询者没找到自己兴趣相关的服务
  - 那么他可以创建一个服务(start_time, end_time, tag)
  - 将其上传至服务器

服务器：
- 服务器的动作基本上都由客户端出发
- 需要注意一点的事，如果服务器收到新的服务创建请求，它在service_shcedule中创建这个服务，并且要通知CA，CA创建这个服务的公私钥对并存储

###### 界面设计
两张界面的图片，比较简陋，详细情况请运行程序：
![](http://ww4.sinaimg.cn/mw690/9bcfe727jw1f8vlttsbvrj208h0dajs4.jpg)
![](http://ww4.sinaimg.cn/mw690/9bcfe727jw1f8vltu4sqqj209r09swfa.jpg)
