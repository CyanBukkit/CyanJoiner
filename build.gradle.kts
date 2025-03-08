plugins {
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    kotlin("jvm") version "1.8.20"
}


group = "cn.cyanbukkit.cyanjoiner"
version = "2.7"


repositories {
    maven("https://nexus.cyanbukkit.cn/repository/maven-public/")
    mavenCentral()
}
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.0-Beta")
    compileOnly("com.github.cryptomorin:XSeries:9.5.0") { isTransitive = false }
    compileOnly(kotlin("stdlib"))
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
    // placehoderapi
    compileOnly("me.clip:placeholderapi:2.10.9")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")
}



bukkit {
    name = rootProject.name // 设置插件的名字 已设置跟随项目名
    description = "带匹配机制的插件" // 设置插件的描述
    authors = listOf("CyanBukkit") // 设置插件作者
    website = "https://cyanbukkit.net" // 设置插件的网站
    main = "${group}.cyanlib.launcher.CyanPluginLauncher" // 设置插件的主类 修改请到group修改
}



tasks {
    compileJava {
        options.encoding = "UTF-8"
    }

    jar {
        archiveFileName.set("${rootProject.name}-${version}.jar")
    }
}

kotlin {
    jvmToolchain(8)
}