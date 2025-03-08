package cn.cyanbukkit.cyanjoiner.utils

import cn.cyanbukkit.cyanjoiner.cyanlib.launcher.CyanPluginLauncher.cyanPlugin
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

object LangUtils : YamlConfiguration() {
    val langFolder = File(cyanPlugin.dataFolder, "lang")
    lateinit var langFile : File

    override fun getString(path: String): String {
        if (super.contains(path)) {
            return super.getString(path)!!.replace('&', '§')
        } else {
            set(path, "§c没有设置的语言")
            save(langFile)
            return "§c没有设置的语言"
        }
    }

    fun init(lang: String) {
        if (!langFolder.exists()) {
            langFolder.mkdir()
        }
        langFile = File(langFolder, "${lang}.yml")
        if (!langFile.exists()) {
            val inputStream = object {}.javaClass.getResourceAsStream("/lang/${lang}.yml")
            if (inputStream != null) {
                langFile.outputStream().use { ops ->
                    inputStream.copyTo(ops)
                }
            }
        }
        this.load(langFile)

    }


    fun  CommandSender.sendLang(path: String) {
        this.sendMessage(LangUtils.getString(path))
    }


}