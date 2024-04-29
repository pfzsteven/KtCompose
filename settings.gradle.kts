import java.io.FileFilter
import java.io.FileInputStream
import java.util.*

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "NeoBackend"

// 自动构建通用依赖模块
// -------- 新项目拷贝以下通用代码 ----------
val modules: ArrayList<File> = ArrayList()
fun buildModules(module: File, parentModule: File? = null) {
    if (module.exists() && module.isDirectory) {
        val gradleFile = File(module, "build.gradle.kts")
        val currentIsAModule = gradleFile.exists()
        if (currentIsAModule) {
            val moduleName = ":${module.name}"
            include(module.name)
            project(moduleName).projectDir = module
            println("==> building module:[${moduleName}] ...")
        }
        module.listFiles(FileFilter { ff ->
            ff.isDirectory && !ff.name.equals("src")
        })?.takeIf { it.isNotEmpty() }?.let { list ->
            val parent = if (currentIsAModule) {
                module
            } else {
                null
            }
            list.forEach { dir ->
                buildModules(dir, parent)
            }
        }
    }
}
buildModules(file("modules"))
//--------------------------------------------

val gradleProperties = File("gradle.properties")
if (gradleProperties.exists()) {
    val properties = Properties()
    val inputStream = FileInputStream(gradleProperties)
    properties.load(inputStream)
    inputStream.close()
    val moduleName: String = properties.getProperty("buildModule")!!
    include(moduleName)
    println("---- compile module[$moduleName] success ----")
} else {
    println("${gradleProperties.name} is not exists.")
}


