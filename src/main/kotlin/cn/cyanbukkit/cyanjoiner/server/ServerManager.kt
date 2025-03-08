package cn.cyanbukkit.cyanjoiner.server

import cn.cyanbukkit.cyanjoiner.CyanJoiner
import cn.cyanbukkit.cyanjoiner.cyanlib.launcher.CyanPluginLauncher.cyanPlugin
import cn.cyanbukkit.cyanjoiner.utils.ping
import cn.cyanbukkit.cyanjoiner.utils.pluginScope
import com.comphenix.protocol.ProtocolLibrary
import kotlinx.coroutines.*
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object ServerManager {
    val categories = CopyOnWriteArrayList<ServerCategory>()
    private val index = HashMap<String, ServerCategory>()
    private val pool: ExecutorService = Executors.newFixedThreadPool(100)

    fun loadCategories() {
        categories.clear()
        index.clear()
        cyanPlugin.logger.info("§a| §7正在加载类别.")
        val confSec = CyanJoiner.servers.getConfigurationSection("categories")!!
        confSec.getKeys(false).forEach {
            cyanPlugin.logger.info("§a| §7 寻找 $it")
            val category = loadCategory(confSec.getConfigurationSection(it)!!)
            categories.add(category)
            index[category.name] = category
        }
    }

    private fun loadCategory(confSec: ConfigurationSection): ServerCategory {
        val servers = ArrayList<Server>()
        confSec.getConfigurationSection("servers")!!.getKeys(false).forEach {
            val server = loadServer(confSec.getConfigurationSection("servers.$it")!!)
            servers.add(server)
        }
        return ServerCategory(
            confSec.name,
            MatchSettings(
                confSec.getBoolean("settings.enable"),
                confSec.getString("settings.kdPapi"),
                confSec.getDouble("settings.difference"),
                confSec.getString("settings.version")?: ">1",
                confSec.getString("settings.displayName").replace('&', '§')
                         ),
            CopyOnWriteArrayList(confSec.getStringList("joinable")),
            CopyOnWriteArrayList(confSec.getStringList("spectatable")),
            servers
        )
    }

    private fun loadServer(confSec: ConfigurationSection): Server {
        val url = confSec.getString("ip")!!
        val ip = url.substringBefore(":")
        val port = url.substringAfter(":").toInt()

        return Server(
            confSec.name,
            confSec.getString("name").replace('&', '§'),
            true,
            ip,
            port,
            ServerInfo(false, 0, 0, ""),
            false
        )
    }

    fun Player.canJoin(sc: ServerCategory): Boolean {
        val it = sc.setting.version
        val mcVersion = ProtocolLibrary.getProtocolManager().getProtocolVersion(this)
        val versionParts = it.split("<", ">").filter { it.isNotBlank() }
        println("需要的版本${it} 当前版本${mcVersion}}")
        if (it.contains(">") && it.contains("<")) {
            val lowerBound = versionParts[0].replace(">", "").replace("=", "").trim().toInt()
            val upperBound = versionParts[1].replace("<", "").replace("=", "").trim().toInt()
            return mcVersion in (lowerBound + 1) until upperBound
        } else if (it.contains(">=") && it.contains("<=")) {
            val lowerBound = versionParts[0].replace(">=", "").trim().toInt()
            val upperBound = versionParts[1].replace("<=", "").trim().toInt()
            return mcVersion in lowerBound..upperBound
        } else if (it.contains(">")) {
            val version = it.substringAfter(">").replace("=", "").trim().toInt()
            return mcVersion >= version
        } else if (it.contains("<")) {
            val version = it.substringAfter("<").replace("=", "").trim().toInt()
            return mcVersion <= version
        } else if (it.contains("=")) {
            val version = it.substringAfter("=").trim().toInt()
            return mcVersion == version
        } else {
            val version = it.trim().toInt()
            return mcVersion == version
        }
    }


    @OptIn(kotlin.time.ExperimentalTime::class)
    fun updatePing() {
        pluginScope.launch(pool.asCoroutineDispatcher()) {
            categories.flatMap { category ->
                val defs = category.servers.map { server ->
                    async {
                        val serverInfo = ping(server.url, server.port)
                        server.currentState = serverInfo
                        server.canJoin = category.joinable.firstOrNull { joinStr ->
                            joinStr == serverInfo.lore || server.currentState.lore.matches(Regex(joinStr))
                        } != null
                        server.canSpectate = category.spectatable.firstOrNull { patten ->
                            patten == server.currentState.lore || server.currentState.lore.matches(Regex(patten))
                        } != null
                    }
                }
                withContext(Dispatchers.IO) {
                    try {
                        defs.awaitAll()
                    } catch (e: TimeoutCancellationException) {
                        // 处理超时异常
                        return@withContext
                    }
                    category.sort()
                    category.updateGUI()
                }
                defs
            }.awaitAll()
        }
    }

    fun getCategoryByName(name: String): ServerCategory? {
        return index[name]
    }
}



//        instance.logger.info("§a| §7正在更新服务器信息")
/*pluginScope.launch(pool.asCoroutineDispatcher()) {
//            val start = System.currentTimeMillis()
    categories.flatMap {
        val defs = it.servers.map { server ->
            async {
                val serverInfo = ping(server.url, server.port)
                server.currentState = serverInfo
                var temp = false
                for (joinStr in it.joinable) {
                    if (joinStr == serverInfo.lore || server.currentState.lore.matches(Regex(joinStr))) {
                        temp = true
                        break
                    }
                }
                server.canJoin = temp
                temp = false
                for (patten in it.spectatable) {
                    if (patten == server.currentState.lore || server.currentState.lore.matches(Regex(patten))) {
                        temp = true
                        break
                    }
                }
                server.canSpectate = temp
            }
        }
        Bukkit.getScheduler().runTaskAsynchronously(instance) {
            try {
                // 你的代码
                CompletableFuture.allOf(*defs.map {
                        def -> def.asCompletableFuture()
                }.toTypedArray()).join()
            } catch (e: TimeoutCancellationException) {
                // 处理超时异常
                return@runTaskAsynchronously
            }
            it.sort()
            it.updateGUI()
        }
        defs
    }.awaitAll()
//            instance.logger.info("更新完成！消耗 ${System.currentTimeMillis() - start} ms")
}*/
