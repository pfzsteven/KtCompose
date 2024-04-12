
## 新项目引入通用模块的操作流程

1. 拷贝 ```modules``` 文件夹到新项目的工程根目录下
2. 拷贝 ```settings.gradle.kts``` 中注释说明的脚本到项目中
3. 拷贝 ```gradle.properties```的```jdkVersion=17```到新工程
4. ```Gradle```刷新，即可完成通用模块的编译
5. 配置依赖。在新工程根目录的```build.gradle.kts```中增加下列依赖:
```kotlin
    // 配置依赖 
    implementation(project(":engine"))    // 必须
    compileOnly(project(":framework"))    // 可选，外部如果有使用，则添加该依赖
```
6. 将```根目录/src/main/resources```所有配置信息拷贝到新工程，根据情况修改后使用
7. jdk 版本统一。新工程也尽量使用 ```17```，否则Gradle同步过程可能会出现编译错误

## 使用方式

### 做必要的配置工作
- 配置列表说明:

| 配置文件                         | 作用                      |
|------------------------------|-------------------------|
| api_codes.xml                | 配置api错误码(含多语言)，不同工程可自定义 |
| config.properties            | 基础环境配置                  |
| email_conf.json              | 配置邮箱信息，用于给用户发送验证码       |
| logback.xml                  | 日志文件输出格式定义              |
| routers.xml                  | 路由配置                    |
| validation_code_template.xml | 邮箱验证码模板配置               |


### 初始化引擎

```kotlin
// Application.kt
fun main() {
    // 统一调用
    val environment = applicationEngineEnvironment {
        // ServerEngine.start(this) 
        // 或者
        ServerEngine.start(this) { application ->
            // ... do something here ...
        }
    }
    embeddedServer(Tomcat, environment).start(wait = true)
}
```