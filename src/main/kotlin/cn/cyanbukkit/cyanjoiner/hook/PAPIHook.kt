package cn.cyanbukkit.cyanjoiner.hook

import cn.cyanbukkit.cyanjoiner.server.ServerManager
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player

object PAPIHook : PlaceholderExpansion() {

    override fun onPlaceholderRequest(player: Player?, args: String): String {
        val split = args.split("_")
        if (split.size != 2) {
            return ""
        }
        when (split[0]) {
            "current" -> {
                val serverCategory = ServerManager.getCategoryByName(split[1]) ?: return ""
                return serverCategory.currentPlayer.toString()
            }

            "max" -> {
                val serverCategory = ServerManager.getCategoryByName(split[1]) ?: return ""
                return serverCategory.maxPlayer.toString()
            }

            else -> {
                return ""
            }
        }

    }

    override fun getIdentifier(): String {
        return "cyanjoiner"
    }

    override fun getAuthor(): String {
        return "TONY_All"
    }

    override fun getVersion(): String {
        return "1.0"
    }
}