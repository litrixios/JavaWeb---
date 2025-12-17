# 1.首先，我们得让你的代码跑起来

分为四步

第一步，你需要刷新你的maven

第二步，你需要在application.properties里面配置数据库账户密码

第三步，你得确保你的jdk版本是对的，经过测试之后目前的lombok最高支持jdk22，请确保你的jdk不高于这个版本。最低版本目前没有测试，但也请尽量保证你的jdk版本在7及以上

第四步，启动setup_db.bat



现在，你可以执行CmsBackendApplication并看看有没有报错信息了

旁边的StandaloneDbTest可以用于单独测试你的数据库连接问题

成功运行之后，你现在可以打开 

http://localhost:8080/doc.html

进行一系列的后端相关测试了

# 2.在你开始写代码之前，你需要知道的

（1）统一返回结果

前端将会只认一种数据格式，所有Controller的返回值必须是Result类型

**所在位置**：`com.bjfu.cms.common.result.Result`

```
// 正确写法
@GetMapping("/get")
public Result<User> getUser() {
    User user = userService.getById(1);
    return Result.success(user); // 返回数据
}

// 错误写法 (禁止直接返回对象)

@GetMapping("/get")

public User getUser() { 

return new User(); 

}
```

（2）获取当前登录用户

不要让前端传userId给你，我们有拦截器自动解析token，你只需要一行代码就能拿到当前是谁在操作

**所在位置**：`com.bjfu.cms.common.utils.UserContext`

```
public void submitManuscript(Manuscript m) {
    // 1. 获取当前登录用户的ID
    Integer currentUserId = UserContext.getUserId();

    // 2. 存入业务数据
    m.setAuthorId(currentUserId);
    
    // ...后续逻辑

}
```

# 3.你的开发流程

假设你要开发“新闻模块”（这里以News举例），请按照以下顺序写代码

第一步：写实体类（Entity)

在 `com.bjfu.cms.entity` 下新建 `News.java`

规范：必须加@Data注解（你现在不用再写getter，setter，toString了，lombok替你完成了）

建议：加@Schema注解，这样后面在Swagger里面可以看到字段说明（虽然我自己也嫌打字累没加，但还是建议加上）

第二步：写数据库接口（Mapper）

在 `com.bjfu.cms.mapper` 下新建 `NewsMapper.java` (接口)

在 `src/main/resources/mapper` 下新建 `NewsMapper.xml`

第三步：写业务逻辑（Service）

在 `com.bjfu.cms.service` 下新建 `NewsService.java` (接口)

在 `com.bjfu.cms.service.impl` 下新建 `NewsServiceImpl.java` (实现类)

第四步：些接口（Controller）

在 `com.bjfu.cms.controller` 下新建 `NewsController.java`

这里需要注意的

如果你的前端是打算用vue来写（即前后端分离），注意写法里面需要暴露你的接口，并且我们约定URL以/api开头，比如/api/user/login,/api/manuscript/list

如果你用的是jsp，前后端不分离的写法，建议你的URL用/page开头或者直接用根路径（不建议），然后这个写法是用不了Swagger的（因为你没有暴露的接口）

# 4.你现在可以去接口调试了

先去调用 登录接口 (`/api/auth/login`)，拿到返回的 `token` 字符串。

在文档页面的 “Authorize” (或者全局参数设置) 里：

参数名：`Authorization`

参数值：粘贴你的 Token

现在你可以点击你写的接口 -> “调试” -> “发送”。