package cn.cyanbukkit.cyanjoiner.server

import cn.cyanbukkit.cyanjoiner.cyanlib.launcher.CyanPluginLauncher.cyanPlugin
import cn.cyanbukkit.cyanjoiner.server.MineCraftVersion.Companion.support
import cn.cyanbukkit.cyanjoiner.server.ServerManager.canJoin
import cn.cyanbukkit.cyanjoiner.utils.LangUtils
import cn.cyanbukkit.cyanjoiner.utils.LangUtils.sendLang
import cn.cyanbukkit.cyanjoiner.utils.changeVersion
import cn.cyanbukkit.cyanjoiner.utils.connectTo
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerJoinEvent
import javax.swing.text.html.HTML.Tag.P

class CategoryListener(private val category: ServerCategory) : Listener {

    @EventHandler
    fun onClick(e: InventoryClickEvent) {

        // filter
        if ((e.inventory != category.getQuick()) && (e.inventory != category.getTotal())) {
            return
        }

        e.isCancelled = true

        if (e.currentItem == null || e.currentItem.type == Material.AIR) {
            return
        }

        if (e.currentItem == ServerCategory.show) {
            e.whoClicked.openInventory(category.getTotal())
            return
        }

        if (e.currentItem == ServerCategory.back) {
            e.whoClicked.openInventory(category.getQuick())
            return
        }

        if (e.currentItem == ServerCategory.join) {
            val pollServer = category.pollServer(e.whoClicked as Player)
            if (pollServer == null) {
                e.whoClicked.sendLang("msg.none_server")
                return
            }
            val player = e.whoClicked
            player as Player
            if (!player.canJoin(category)) {
                player.sendMessage(LangUtils.getString("not_this_version")
                        .changeVersion(category.setting.version))
                return
            }
            player.connectTo(pollServer)
            return
        }

        val server = category.itemServerMap[e.currentItem]

        val player = e.whoClicked
        player as Player

        if (server == null) {
            return
        }

        if (!server.currentState.isOnline) {
            player.sendLang("cant_join")
            cyanPlugin.logger.warning("[傻瓜式] 服务器 ${server.name} 不在线")
            return
        }

        if (server.currentState.current >= server.currentState.max) {
            player.sendLang("server_full")
            cyanPlugin.logger.warning("[傻瓜式] 服务器 ${server.name} 已满")
            return
        }

        if (!server.canJoin && !(server.canSpectate)) {
            player.sendLang("cant_join")
            cyanPlugin.logger.warning("[傻瓜式] 服务器 ${server.name} 不允许加入 因为 ${!server.canJoin} ${!server.canSpectate}")
            return
        }
        if (!player.canJoin(category)) {
            player.sendMessage(LangUtils.getString("not_this_version")
                    .changeVersion(category.setting.version))
            cyanPlugin.logger.warning("[傻瓜式] 服务器 ${server.name} 不允许加入 因为 ${player} ${!player.canJoin(category)}")
            return
        }

        player.connectTo(server)
    }




    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        e.player.sendMessage("§a您正在使用的版本为§f${e.player.support().version}")
    }
}