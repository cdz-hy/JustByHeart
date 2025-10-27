plugins {
    id("com.android.application") version "8.4.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.23" apply false
    id("org.jetbrains.kotlin.kapt") version "1.9.23" apply false
    id("org.jetbrains.kotlin.plugin.parcelize") version "1.9.23" apply false
}

// 项目级配置应该只包含适用于所有模块的配置
// android 块应该在模块级 build.gradle.kts 文件中定义