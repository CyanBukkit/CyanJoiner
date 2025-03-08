package cn.cyanbukkit.cyanjoiner

import cn.cyanbukkit.cyanjoiner.command.MainCommand
import cn.cyanbukkit.cyanjoiner.cyanlib.launcher.CyanPluginLauncher.cyanPlugin
import cn.cyanbukkit.cyanjoiner.server.ServerManager
import cn.cyanbukkit.cyanjoiner.server.ServerManager.canJoin
import cn.cyanbukkit.cyanjoiner.utils.LangUtils
import cn.cyanbukkit.cyanjoiner.utils.LangUtils.sendLang
import cn.cyanbukkit.cyanjoiner.utils.changeVersion
import cn.cyanbukkit.cyanjoiner.utils.connectTo
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.io.File
import java.util.*

object CyanJoiner {
    lateinit var settings: YamlConfiguration
    private lateinit var settingsFile : File
    lateinit var servers: YamlConfiguration
    private lateinit var serversFile: File
    private var timer = Timer("Server Query")


    fun onEnable() {
        settingsFile = File(cyanPlugin.dataFolder, "settings.yml")
        if (!settingsFile.exists()) {
            cyanPlugin.saveResource("settings.yml", false)
        }
        serversFile = File(cyanPlugin.dataFolder, "servers.yml")
        if (!serversFile.exists()) {
            cyanPlugin.saveResource("servers.yml", false)
        }
        settings = YamlConfiguration.loadConfiguration(settingsFile)
        servers = YamlConfiguration.loadConfiguration(serversFile)
        LangUtils.init(settings.getString("Language"))
        MainCommand.register()
        Bukkit.getServer().messenger.registerOutgoingPluginChannel(cyanPlugin, "BungeeCord")
        timer.schedule(object : TimerTask() {
            override fun run() {
                ServerManager.updatePing()
            }
        }, 0L, settings.getLong("update_delay"))
        ServerManager.loadCategories()
        
    }

    fun reload() {
        settings = YamlConfiguration.loadConfiguration(settingsFile)
        servers = YamlConfiguration.loadConfiguration(serversFile)
        LangUtils.init(settings.getString("Language"))
        ServerManager.loadCategories()
        timer.cancel()
        timer = Timer("Server Query")
        timer.schedule(object : TimerTask() {
            override fun run() {
                ServerManager.updatePing()
            }
        }, 0L, settings.getLong("update_delay"))
    }

    fun onDisable() {
        timer.cancel()
    }

    val isOnQuick = mutableListOf<Player>()

    fun quickJoin(name: String, sender: Player) {
        if (isOnQuick.contains(sender)) {
            sender.sendLang("already_quick")
            return
        }
        isOnQuick.add(sender)
        val category = ServerManager.getCategoryByName(name) // 获取分类
        if (category == null) {
            sender.sendLang("null_category")
            isOnQuick.remove(sender)
            return
        }
        val pollServer = category.pollServer(sender)// 获取服务器
        if (pollServer == null) {
            sender.sendLang("none_server")
            isOnQuick.remove(sender)
            return
        }
        // 在5秒后传送 但是5秒内给玩家展示Title 和播放五个带有Sound note_block 声音的音效
        val configList = LangUtils.getConfigurationSection("time")
        val time = configList.getKeys(false).toMutableList()
        object : BukkitRunnable() {
            override fun run() {
                val it = time.removeAt(0)
                val sec = configList.getConfigurationSection(it)
                // 如果是time 最后一个
                sender.sendTitle(sec.getString("title"), sec.getString("sub_title"))
                val sound = sec.getString("sound").split("|")
                sender.playSound(sender.location, Sound.valueOf(sound[0].trim()), sound[1].toFloat(), sound[2].toFloat())
                if (time.isEmpty()) {
                    if (!pollServer.canJoin) {
                        sender.sendLang("server_offline")
                        isOnQuick.remove(sender)
                        cancel()
                    }
                    if (!sender.canJoin(category)) {
                        sender.sendMessage(LangUtils.getString("not_this_version").changeVersion(category.setting.version))
                        isOnQuick.remove(sender)
                        cancel()
                    }
                    sender.connectTo(pollServer)
                    isOnQuick.remove(sender)
                    cancel()
                    return
                }
            }
        }.runTaskTimer(cyanPlugin, 0L, 20L)
    }

}