## 简介

 Apache Shiro是一个使用广泛的JAVA安全管理框架，功能强大且可灵活定制。但对于普通项目来说，Shiro的设计理念较为复杂，一些概念如Realm，Subject等抽象的级别比较高，如果没有对框架细节进行深入了解的话，很难理解其中的准确含义。要将其应用于实际项目，还需要针对项目的实际情况做大量的配置和改造，时间成本较高。而且Shiro默认的实现主要是针对传统的基于SESSION的web应用程序，对于目前流行的无状态微服务应用并没有提供一个开箱即用的整合方案。

 shrio-with-jwt-spring-boot-starter基于spring-boot环境，使用Shiro作为基础验证框架，整合了JWT（[JSON Web token](https://jwt.io/)）规范，通过简单的一些配置，提供在微服务环境下开箱即用的无状态权限管理框架。

## 特点

-   完全兼容Shiro
-   无状态设计，无需Session
-   基于JWT规范的Token设计
-   在spring-boot环境下自动配置，开箱即用
-   基于注解的权限配置，并且兼容Shiro的层级权限设置
-   通过接口灵活定义获取用户权限（permission）的方式，兼容多种权限模型
-   Token过期前自动刷新（需配合前端的实现）

## 使用方法

1. 引入shrio-with-jwt-spring-boot-starter。
   
```xml
<dependency>
    <groupId>com.github.davidfantasy</groupId>
    <artifactId>shrio-with-jwt-spring-boot-starter</artifactId>
    <version>${version}</version>
</dependency>
```
2. 根据实际业务的需要，实现**com.github.davidfantasy.jwtshiro.JWTUserAuthService**接口。JWTUserAuthService接口是框架的一个扩展点，便于应用端根据自身的业务规则对权限模型，错误处理等进行自定义实现。**getUserInfo**方法用于客户端访问时根据客户端传回token中包含的用户account信息，获取用户的实际权限。获取的方式由应用程序端来控制，可以从配置文件中加载，也可以根据account查询数据库，获取用户实际权限。**getAuthenticatedUser**方法已提供默认实现，用于获取当前请求接口的客户信息，以下是一个例子

```java
@Service
public class JWTUserAuthServiceImpl implements JWTUserAuthService {

    @Autowired
    private UserService userService;

    private Cache<String, UserInfo> userCache = CacheBuilder.newBuilder().maximumSize(1000)
            .expireAfterWrite(30, TimeUnit.MINUTES).build();

    @Override
    public UserInfo getUserInfo(String account) {
        try {
            UserInfo user = userCache.getIfPresent(account);
            if (user == null) {
                user = this.queryUserInfo(account);
                if (user != null) {
                    userCache.put(account, user);
                }
            }
            return user;
        } catch (Exception e) {
            log.error("读取用户缓存信息发生错误:" + e.getMessage());
        }
        return null;
    }

    /**
     * 自定义访问资源认证失败时的处理方式，例如返回json格式的错误信息
     * {\"code\":401,\"message\":\"用户认证失败！\")
     */
    @Override
    public void onAuthenticationFailed(HttpServletRequest req, HttpServletResponse res) {
        res.setStatus(HttpStatus.UNAUTHORIZED.value());
    }

    /**
     * 自定义访问资源权限不足时的处理方式，例如返回json格式的错误信息
     * {\"code\":403,\"message\":\"permission denied！\")
     */
    @Override
    public void onAuthorizationFailed(HttpServletRequest req, HttpServletResponse res) {
        res.setStatus(HttpStatus.FORBIDDEN.value());
    }

    private ShiroUserInfo queryUserInfo(String account) {
		// 这里编写获取ShiroUserInfo的逻辑，例如从数据库进行查询
    }

    /**
     * 调用接口的getAuthenticatedUser获取当前请求的用户信息
     */
    public ShiroUserInfo getCurrentUser(){
        return (ShiroUserInfo)this.getAuthenticatedUser(false);
    }

    /**
     * 刷新指定account的缓存信息
     */
    public void refreshUserCache(String account) {
        this.userCache.invalidate(account);
    }

}
```
**注意**：getUserInfo这个方法在每次接口调用的时候都会触发，用于检查用户权限，请实现时根据需要对接口的返回结果进行缓存（例如使用Guava的Cache）。

返回值com.github.davidfantasy.jwtshiro.UserInfo类封装了一个系统用户必要的权限信息，可以根据实际需要进行扩展：

```java
public class UserInfo {

  /**
    * 用户的唯一标识
    */
   private String account;

   /**
    * accessToken的密钥，用于对accessToken进行加密和解密
    * 建议为每个用户配置不同的密钥（比如使用用户的password）
    */
   private String secret;

   /**
    * 用户权限集合，含义类似于Shiro中的perms
    */
   private Set<String> permissions;

}
```
3. 对需要进行权限控制的Controller添加对应的注解，实现灵活的权限控制。**为了简化配置，框架默认所有被拦截的资源必须是要经过认证的用户才可以被访问。**即如果配置的拦截范围是/api/*,则会添加一条默认的验证规则: /api/*=authc。但任何通过注解添加的验证规则都拥有比默认规则更高的优先级。如果需要精确控制某个接口的用户权限，就需要利用到RequiresPerms和AlowAnonymous注解。添加了AlowAnonymous注解的url允许匿名访问，而RequiresPerms则用于指定某个url所需的用户权限，访问用户必须拥有该权限才允许访问该接口。
**注意**：RequiresPerms比AlowAnonymous拥有更高的优先级，如果一个url同时被设定了两种规则，则AlowAnonymous不会起作用。

下面是一个访问控制规则设置的例子：
```java
@RestController
@RequestMapping("/api/user")
@RequiresPerms("user")
public class UserController {

    @AlowAnonymous
    @PostMapping("/login")
    public String login() {
       return null;
    }

    @GetMapping("/detail")
    public String getUserDetail() {
        return null;
    }

    @PostMapping("/modify")
    @RequiresPerms("modify")
    public String modifyUser() {
        return null;
    }

    @PostMapping("/delete")
    @RequiresPerms("delete")
    public String testPerm2() {
        return null;
    }
}
```
在上面的例子中,接口与用户权限的对应关系如下：
| 接口               | 所需权限                        |
| :----------------- | :------------------------------ |
| /api/user/login   | 无需权限，可匿名访问            |
| /api/user/detail  | 访问用户需具备权限"user"          |
| /api/user/modify | 访问用户需具备权限"user:modify"  |
| /api/user/delete | 访问用户需具备权限"user:delete"  |

类似于Shiro官方的如下配置
```xml
<property name="filterChainDefinitions"> 
    <value>
        /api/user/login     = anon
        /api/user/detail    = perms["user"]
        /api/user/modify    = perms["user:modify"]
        /api/user/delete    = perms["user:delete"]
    </value>
</property>
```
**注意**：和在Shiro中一样，权限是按层级划分的（使用:分割），即在上例中，如果用户拥有的权限中有“user”，则可以同时访问/api/user/detail,/api/user/modify,/api/user/delete三个接口

## 客户端调用

客户端在访问非匿名接口前，都需要调用服务端的登录接口获取accessToken，accessToken有时效限制，在生命周期内由客户端负责对accessToken进行存储和管理。服务端的登录接口生成accessToken的示例代码如下：

```java

@RestController
@RequestMapping("/security")
public class MockController {

    @Autowired
    private MockUserService userService;

    @Autowired
    private JWTHelper jwtHelper;

    @AlowAnonymous
    @PostMapping("/login")
    public Result login(String account,String password) {
        UserInfo user = userService.getUserInfo(account);
        if(user==null||!user.getPassword().equals(password)){
            throw new IllegalArgumentException("用户名或密码错误");
        }
        String accessToken = jwtHelper.sign(user.getAccount(), user.getPassword());
        //后续token的刷新由客服端负责维护
        Result result = new Result();
        result.setToken(accessToken);
        return result;
    }

}
```
客户端登录后获取的accessToken,每次调用接口时，都将accessToken加入到请求的header中供服务端进行权限验证。header中的名称默认为"jwt-token"，也可以通过配置修改为其它名称，请求示例如下：

```http
accept: application/json, text/plain, */*
accept-encoding: gzip, deflate, br
accept-language: zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6
jwt-token: eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE1ODQzNjG5OTtsImFjY291bnQiOiIxODcxNjYxODEzOCJ9.7eJYVmSys6YBu51Al5hdXdPMdrKsQFCqMwHu8ATaOPY
```
## 客户端accessToken自动刷新

accessToken的有效期由两个配置构成，maxAliveMinute和maxIdleMinute。maxAliveMinute定义了accessToken的理论过期时间，而maxIdleMinute定义了
accessToken的最大生存周期。框架会自动注册一个Spring的HandlerInterceptor用来处理Token的自动刷新问题，如果传入的Token已经超过maxAliveMinute设定的时间，但还没有达到maxIdleMinute的限制，则会自动刷新该用户的accessToken并添加在response header（header中的名称取决于配置值），客户端如果在响应头中发现有新的token返回,说明当前token即将失效，需要及时更新自身存储的token。

这个机制实际是提供一个窗口期，让客户端安全的刷新accessToken。试想如果token失效了就必须立即重新登录，那势必会严重影响到用户的实际体验。

## 配置项说明

| 参数名                     | 默认值    | 说明                                                                                          |
| :------------------------- | :-------- | :-------------------------------------------------------------------------------------------- |
| jwt-shiro.urlPattern       | /*        | 需要进行权限拦截的URL pattern, 多个使用url隔开，例如：/api/*,/rest/*                          |
| jwt-shiro.maxAliveMinute   | 30        | accessToken的理论过期时间，单位分钟，token如果超过该时间则接口响应的header中附带新的token信息 |
| jwt-shiro.maxIdleMinute    | 60        | accessToken的最大生存周期，单位分钟，在此时间内的token无需重新登录即可刷新                    |
| jwt-shiro.headerKeyOfToken | jwt-token | accessToken在http header中的name                                                              |
| jwt-shiro.accountAlias     | account   | token中保存的用户名的key name                                                                 |

 
