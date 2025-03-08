package cn.cyanbukkit.cyanjoiner.cyanlib.launcher;


import cn.cyanbukkit.cyanjoiner.CyanJoiner;
import cn.cyanbukkit.cyanjoiner.cyanlib.loader.KotlinBootstrap;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 嵌套框架
 */

public class CyanPluginLauncher extends JavaPlugin {

    public static CyanPluginLauncher cyanPlugin;

    public CyanPluginLauncher() {
        cyanPlugin = this;
        KotlinBootstrap.init();
        KotlinBootstrap.loadDepend("com.github.cryptomorin", "XSeries", "9.5.0");
        KotlinBootstrap.loadDepend("org.jetbrains.kotlinx", "kotlinx-coroutines-core-jvm", "1.7.0-Beta");
    }


    @Override
    public void onEnable() {
        CyanJoiner.INSTANCE.onEnable();
    }




}