package cn.cyanbukkit.cyanjoiner.command

import cn.cyanbukkit.cyanjoiner.CyanJoiner
import cn.cyanbukkit.cyanjoiner.CyanJoiner.quickJoin
import cn.cyanbukkit.cyanjoiner.cyanlib.command.CyanCommand
import cn.cyanbukkit.cyanjoiner.cyanlib.command.RegisterCommand
import cn.cyanbukkit.cyanjoiner.cyanlib.command.RegisterSubCommand
import cn.cyanbukkit.cyanjoiner.server.ServerManager
import cn.cyanbukkit.cyanjoiner.utils.LangUtils.sendLang
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@RegisterCommand(name = "cyanjoiner", alia = ["cj"], permission = "cyanjoiner.use")
object MainCommand : CyanCommand() {
    override fun mainExecute(sender: CommandSender?, commandLabel: String?, args: Array<out String>?) {}

    @RegisterSubCommand(subName = "gui", permission = "cyanjoiner.join.gui")
    fun gui(sender: CommandSender, label: String, args: Array<out String>) {
        if (sender !is Player) return
        if (args.isEmpty()) {
            sender.sendMessage("§c请输入分类名称")
            return
        }
        val category = ServerManager.getCategoryByName(args[0])
        if (category == null) {
            sender.sendMessage("§c没有这个分类")
        } else {
            category.openGui(sender)
        }
    }


    @RegisterSubCommand(subName = "quick", permission = "cyanjoiner.join.quick")
    fun quick(sender: CommandSender, label: String,  args: Array<out String>) {
        if (sender !is Player) return
        if (args.isEmpty()) {
            sender.sendMessage("§c请输入服务器名称")
            return
        }
        quickJoin(args[0], sender)
    }


    @RegisterSubCommand(subName = "list", permission = "cyanjoiner.join.list")
    fun list(sender: CommandSender,  label: String, args: Array<out String>) {
        if (sender !is Player) return
        sender.sendMessage("§e分类列表")
        ServerManager.categories.forEach {
            sender.sendMessage("§e${it.setting.displayName}: §a${it.name}")
        }
    }


    @RegisterSubCommand(subName = "reload", permission = "cyanjoiner.join.reload")
    fun reload(sender: CommandSender, label: String,  args: Array<out String>) {
        if (sender !is Player) return
        CyanJoiner.reload()
        sender.sendLang("reload")
    }


    @RegisterSubCommand(subName = "updateServer", permission = "cyanjoiner.join.updateServer")
    fun updateServer(sender: CommandSender, label: String,  args: Array<out String>) {
        if (sender !is Player) return
        ServerManager.updatePing()
        sender.sendLang("update")
    }

}