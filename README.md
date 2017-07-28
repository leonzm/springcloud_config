# Spring Cloud Config 笔记

# 简介
> Spring Cloud Config 用来为分布式系统中的基础设施和微服务应用提供集中化的外部配置支持，它分为服务端与客户端两个部分。其中服务端也称为
分布式配置中心，它是一个独立的微服务应用，用来连接配置仓库并为客户端提供获取配置信息、加密/解密信息等访问接口；而客户端则是微服务架构中的
各个微服务应用或基础设施，它们通过指定的配置中心来管理应用资源与业务相关的配置内容，并在启动的时候从配置中心获取和加载配置信息。
> Spring Cloud Config 默认采用 Git 来存储配置信息，也支持 SVN 仓库、本地化文件系统。

# 构建配置中心（服务端）步骤
* 构建 Spring Boot 工程，引入 config-server 依赖（包含 Eureka、Hystrix）
* 主类上加 @EnableConfigServer 注解开启 Spring Cloud Config 的服务端功能
* 在 application.properties 中添加配置服务的基本信息以及 Git 仓库的相关信息
* 启动后，Spring Cloud Config 会开启五类请求 URL，其与配置文件的映射关系如下：
> 其中，application 代表配置文件名，profile 代表文件的环境，label 代表分支（默认为 master 分支）。如配置文件：didispace-dev.properties，
那么其 application 配置文件名为：didispace，profile 环境为：dev。
> 1. /{application}/{profile}[/{label}]
> 2. /{application}-{profile}.yml
> 3. /{label}/{application}-{profile}.yml
> 4. /{application}-{profile}.properties
> 5. /{label}/{application}-{profile}.properties

> Config 通过 git clone 命令将配置内容复制了一份在本地存储，然后读取这些内容并返回给微服务应用进行加载。通过 Git 在本地仓库暂存，可以有效
防止当 Git 仓库出现故障而引起无法加载信息的情况。

# 构建客户端步骤
* 构建 Spring Boot 工程，引入 config 依赖
* 创建 bootstrap.properties 配置，指定 config server 位置、配置文件名、环境、分支
> 1. spring.application.name： 配置文件名，对应配置文件规则中的{application}部分
> 2. spring.cloud.config.profile：环境，对应配置文件规则中的{profile}部分
> 3. spring.cloud.config.label：分支，对应配置文件规则中的{label}部分
> 4. spring.cloud.config.uri：配置中心 config server 的地址

> 通过 bootstrap.properties 对 config-server 的配置，使得该应用会从 config-server 中获取一些外部配置信息，这些外部配置信息的优先级
比本地的内容要高，从而实现了外部化配置
* 在使用配置的地方通过@Value("${from}")绑定配置服务中配置的from属性，或使用注入的 org.springframework.core.env.Environment 获取配置

# 使用占位符配置 URI
> 可通过 {application} 占位符来实现一个应用对应一个 Git 仓库目录的配置效果，如：
> 1. pring.cloud.config.server.git.uri=http://git.oschina.net/leonzh2017/{application}/

> 其中，{application} 代表了应用名，当客户端向 Config Server 发起获取配置的请求时，Config Server 会根据客户端的 spring.application.name
信息来填充 {application} 占位符以定位配置资源的存储位置，从而实现根据微服务应用的属性动态获取不同位置的配置，同理，{application}-config 
配置也可以同时匹配多个不同服务

# 配置多个仓库

# 子目录存储
* spring.cloud.confi.server.git.searchPaths 参数的配置也支持使用{application}、{profile}和{label}占位符，如：
> 1. spring.cloud.config.server.git.uri=http://git.oschina.net/leonzh2017/SpringCloud-Learning/
> 2. spring.cloud.confi.server.git.searchPaths={application}

> 通过上面的配置，可以实现在 SpringCloud-Learning 仓库中，一个应用一个目录的效果

# SVN 配置仓库

# 本地快照
> 通过 spring.cloud.config.server.git.basedir 或 spring.cloud.config.server.snv.basedir 可指定一个固定的位置来存储快照信息。从而
避免由于随机性以及临时目录的特性，导致一些不可预知的后果。

# 本地文件系统

# 监控检测

# 属性覆盖
* spring.cloud.config.server.overrides 设置键值对参数，这些参数会以 Map 的方式加载到客户端的配置中，如：
> spring.cloud.config.server.overrides.from=shanghai，该属性设置的参数不会被客户端修改，并且客户端都会取得这些信息，可以方便地为 
Spring Cloud 应用配置一些共同属性或默认属性。

# 安全保护

# 加密解密

# 服务化配置中心
> 可以把 Config Server 视为微服务架构中与其他业务服务一样的一个基本单元，将 Config Server 注册到服务中心，并通过服务发现来访问 Config 
Server 并获取 Git 仓库中的配置信息。方法如下：
* 服务端配置
> 1. 引入 Eureka 依赖
> 2. 主类上加@EnableDiscoveryClient注解用来将 config server 注册到服务注册中心上去
> 3. 在 application.properties 中配置参数 eureka.client.service-url.defaultZone 以指定服务注册中心

* 客户端配置
> 1. 引入 Eureka 依赖
> 2. 主类上加@EnableDiscoveryClient注解用来发现 config server 服务
> 3. 在 bootstrap.properties 中配置参数 eureka.client.service-url.defaultZone 以指定服务注册中心，配置 spring.cloud.config.discovery.enabled=true 
开启服务发现 config server，配置 spring.cloud.config.discovery.service-id 以指定 config server 的服务名

# 失败快速响应与重试
* 要实现客户端优先判断 Config Server 获取是否正常，并快速响应失败内容，只需在 bootstrap.properties 中配置参数 spring.cloud.config.failFast=true 
即可。
* Config 客户端还提供了自动重试的功能，可以避免一些间歇性问题引起的失败导致客户端应用无法启动的情况
> 1. 在开启重试机制功能前，确保已经设置 spring.cloud.config.failFast=true 
> 2. 引入 spring-retry 和 spring-boot-starter-aop 依赖

> spring.cloud.config.retry.multiplier：初始重试间隔时间（单位：毫秒），默认为1000；
> spring.cloud.config.retry.initial-interval：下一间隔的乘数，默认为1.1，所以当最初间隔是1000毫秒时，下一次失败后的间隔为1100毫秒；
> spring.cloud.config.retry.max-interval：最大间隔时间，默认为2000毫秒；
> spring.cloud.config.retry.max-attempts：最大重试次数，默认为6次

