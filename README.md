# SifCap

Sif国服个人数据收集服务

项目使用Java语言，依赖jnetpcap库，实现对网卡流量的监控，并存入数据库。数据库交互部分使用MyBatis框架编写。

## 环境配置 (for Linux)

1. 安装libpcap、libpcap-devel、sqlite3库

``` bash
yum install -y libpcap libpcap-devel sqlite-devel
```

2. 下载jnetpcap

可以根据环境下载对应的版本

``` bash
wget -O jnetpcap-1.4.r1425 https://downloads.sourceforge.net/project/jnetpcap/jnetpcap/Latest/jnetpcap-1.4.r1425-1.linux64.x86_64.tgz
```

3. 添加so文件到系统库

``` bash
tar -xvf jnetpcap-1.4.r1425
cp jnetpcap-1.4.r1425/libjnetpcap.so /lib/
```

## 快速开始

### 环境需求

项目需运行在Java 1.8，MySQL 5.6及以上环境

### 数据库配置

首次运行需配置数据库。

初始化：执行script/initial.sql，生成需要的数据表

``` bash
mysql -h$host -u$user -p$pass -D$db < script/initial.sql
```

仿照下例添加文件：src/main/resources/mybatis-config.xml

``` xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
        PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-config.dtd">

<configuration>
    <!-- 环境配置 -->
    <environments default="development">
        <environment id="development">
            <transactionManager type="JDBC"/>
            <!-- 数据库连接相关配置 ,这里动态获取config.properties文件中的内容-->
            <dataSource type="POOLED">
                <property name="driver" value="com.mysql.cj.jdbc.Driver" />
                <property name="url" value="jdbc:mysql://{host}:{port}/{db}" />
                <property name="username" value="{user}" />
                <property name="password" value="{passwd}" />
            </dataSource>
        </environment>
    </environments>
    <!-- mapping文件路径配置 -->
    <mappers>
        <mapper resource="mapper/userMapper.xml"/>
        <mapper resource="mapper/liveMapper.xml"/>
    </mappers>

</configuration>
```

### 编译

``` bash
sh build.sh
```

### 运行

``` bash
cd output
sh server-start.sh
```

## 如何贡献

欢迎提Issue和Pull request

