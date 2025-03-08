package cn.cyanbukkit.cyanjoiner.server

import cn.cyanbukkit.cyanjoiner.CyanJoiner
import cn.cyanbukkit.cyanjoiner.cyanlib.launcher.CyanPluginLauncher.cyanPlugin
import com.cryptomorin.xseries.XMaterial
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.abs


data class MatchSettings(
    val isEnable: Boolean,
    val papi: String,
    val diff: Double,
    val version: String = ">1",
    val displayName: String,
                        )

/**
 * 这取决于 如有有玩家进去kd就是这个玩家的kd 如果没有 就是要进的玩家的kd
 */
val diffKd = mutableMapOf<Server, Double>()

data class ServerCategory(
    val name: String,
    val setting: MatchSettings,
    val joinable: CopyOnWriteArrayList<String>,
    val spectatable: CopyOnWriteArrayList<String>,
                         ) {
    var servers: CopyOnWriteArrayList<Server> = CopyOnWriteArrayList()
        private set


    constructor(
        name: String,
        setting: MatchSettings,
        joinAble: CopyOnWriteArrayList<String>,
        spectacle: CopyOnWriteArrayList<String>,
        inputSer: List<Server>,
               ) : this(name, setting, joinAble, spectacle) {
        servers = CopyOnWriteArrayList(inputSer)
    }

    companion object {
        val show = ItemStack(XMaterial.matchXMaterial(CyanJoiner.settings.getString("icons.show_all.type")!!).orElse(
            XMaterial.AIR).parseMaterial()).apply {
            val itemMeta = this.itemMeta
            itemMeta.apply {
                this.displayName = CyanJoiner.settings.getString("icons.show_all.name")!!.replace("&", "§")
                this.lore = CyanJoiner.settings.getString("icons.show_all.lore")!!.lines()
                if (CyanJoiner.settings.getBoolean("icons.show_all.shiny")) {
                    this.addEnchant(Enchantment.DURABILITY, 1, true)
                    this.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                    this.addItemFlags(ItemFlag.HIDE_ENCHANTS)
                }
            }
            this.itemMeta = itemMeta
        }

        val join = ItemStack(XMaterial.matchXMaterial(CyanJoiner.settings.getString("icons.fast_join.type")!!).orElse(
            XMaterial.AIR).parseMaterial()!!).apply {
            val itemMeta = this.itemMeta
            itemMeta.apply {
                this.displayName = CyanJoiner.settings.getString("icons.fast_join.name")!!.replace("&", "§")
                this.lore = CyanJoiner.settings.getString("icons.fast_join.lore")!!.lines()
                if (CyanJoiner.settings.getBoolean("icons.fast_join.shiny")) {
                    this.addEnchant(Enchantment.DURABILITY, 1, true)
                    this.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                    this.addItemFlags(ItemFlag.HIDE_ENCHANTS)
                }
            }
            this.itemMeta = itemMeta
        }

        val back = ItemStack(XMaterial.matchXMaterial(CyanJoiner.settings.getString("icons.back.type")).orElse(
            XMaterial.AIR).parseMaterial()!!).apply {
            val itemMeta = this.itemMeta
            itemMeta.apply {
                this.displayName = CyanJoiner.settings.getString("icons.back.name")!!.replace("&", "§")
                this.lore = CyanJoiner.settings.getString("icons.back.lore")!!.lines()
                if (CyanJoiner.settings.getBoolean("icons.back.shiny")) {
                    this.addEnchant(Enchantment.DURABILITY, 1, true)
                    this.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                    this.addItemFlags(ItemFlag.HIDE_ENCHANTS)
                }
            }
            this.itemMeta = itemMeta
        }
    }

    val itemServerMap = HashMap<ItemStack, Server>()

    val maxPlayer: Int
        get() {
            var players = 0
            servers.forEach {
                players += it.currentState.max

            }
            return players
        }

    val currentPlayer: Int
        get() {
            var players = 0
            servers.forEach {
                players += it.currentState.current
            }
            return players
        }
    private val slots = CyanJoiner.settings.getIntegerList("sections.slots") ?: listOf(
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34,
        37, 38, 39, 40, 41, 42, 43
    )
    
    private val quick: Inventory = Bukkit.createInventory(null, 6 * 9, setting.displayName)
    private val total: Inventory = Bukkit.createInventory(null, 6 * 9, setting.displayName)
    private val listener = CategoryListener(this)
    // build items
    init {
        Bukkit.getPluginManager().registerEvents(listener, cyanPlugin)
        quick.setItem(CyanJoiner.settings.getInt("sections.quick_join_slot"), join)
        quick.setItem(CyanJoiner.settings.getInt("sections.show_all_slot"), show)
        total.setItem(CyanJoiner.settings.getInt("sections.back_slot"), back)
        // 设置装饰的代码位置 
        // 添加装饰物品
        CyanJoiner.settings.getConfigurationSection("decorations")?.let { decorations ->
            decorations.getKeys(false).forEach { key ->
                val section = decorations.getConfigurationSection(key)
                section?.let {
                    val slot = it.getInt("slot")
                    val material = XMaterial.matchXMaterial(it.getString("type")!!).orElse(XMaterial.AIR).parseMaterial()
                    val item = ItemStack(material).apply {
                        itemMeta = itemMeta?.apply {
                            displayName = it.getString("name")!!.replace("&", "§")
                            lore = it.getString("lore")!!.split("\n")
                            if (it.getBoolean("shiny")) {
                                addEnchant(Enchantment.DURABILITY, 1, true)
                                addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS)
                            }
                        }
                    }
                    quick.setItem(slot, item)
                    total.setItem(slot, item)
                }
            }
        }
    }

    fun openGui(player: Player) {
        player.openInventory(getQuick())
    }

    fun sort() {
        servers.sortDescending()
    }

    /**
     * 加入的服务器
     *
     * @return 可加入的服务器,若无可加入的服务器为null
     */
    fun pollServer(player: Player): Server? {
        sort()
        val playerKd = getPlayerKd(player)
        servers.forEach { server ->
            if (server.canJoin && isKdMatch(playerKd, server)) {
                return server
            }
        }
        return null
    }



    /**
     * Retrieves the player's KD ratio from the placeholder API.
     *
     * @param player The player whose KD ratio is to be retrieved.
     * @return The player's KD ratio.
     */
    private fun getPlayerKd(player: Player): Double {
        val kdString = PlaceholderAPI.setPlaceholders(player, setting.papi)
        return kdString.toDoubleOrNull() ?: 0.0
    }

    /**
     * Checks if the player's KD ratio matches the server's criteria.
     *
     * @param playerKd The player's KD ratio.
     * @param server The server to check against.
     * @return 如果是匹配的则为true 也就是说
     */
    private fun isKdMatch(playerKd: Double, server: Server): Boolean {
        if (!setting.isEnable) return true
        val serverKd = getServerKd(server)
        if (serverKd == 0.0) {
            diffKd[server] = playerKd
            return true
        }
        return abs(playerKd - serverKd) <= setting.diff
    }

    /**
     * Retrieves the server's KD ratio.
     *
     * @param server The server whose KD ratio is to be retrieved.
     * @return The server's KD ratio.
     */
    private fun getServerKd(server: Server): Double {
        if (diffKd.containsKey(server)) {
            return diffKd[server]!!
        } else {
            return 0.0
        }
    } 


    fun updateGUI() {
        // clearMap
        itemServerMap.clear()
        // clear quick
        slots.forEach {
            quick.setItem(it, null)
        }
        // build & fill item in quick
        val temp = ArrayList<Server>()
        servers.forEach {
            if (it.canJoin) temp.add(it)
        }
        temp.sortDescending()
        temp.forEachIndexed { index, server ->
            if (!server.canJoin) {
                return@forEachIndexed
            }
            val serverItem = buildServerItem(server)
            quick.setItem(slots[index], serverItem)
        }

        // clear total
        slots.forEach {
            total.setItem(it, null)
        }

        // build & fill item in total
        servers.forEachIndexed { index, server ->
//            println("Item\nSlot: $index \n Server: $server")
            val serverItem = buildServerItem(server)
            total.setItem(slots[index], serverItem)
        }
    }

    private fun buildServerItem(server: Server): ItemStack {
        // Offline
        if (!server.currentState.isOnline) {
            val builder = ItemStack(XMaterial.matchXMaterial(CyanJoiner.settings.getString("icons.closed.type")).orElse(
                XMaterial.AIR).parseMaterial()!!).apply {
                val itemMeta = this.itemMeta
                itemMeta.apply {
                    this.displayName = CyanJoiner.settings.getString("icons.closed.name")!!.replace("%name%",
                                                                                                    server.name).replace(
                        "&", "§")
                    lore = CyanJoiner.settings.getString("icons.closed.lore")!!.lines()
                    if (CyanJoiner.settings.getBoolean("icons.closed.shiny")) {
                        this.addEnchant(Enchantment.DURABILITY, 1, true)
                        this.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                        this.addItemFlags(ItemFlag.HIDE_ENCHANTS)
                    }
                }
                this.itemMeta = itemMeta
            }
            itemServerMap[builder] = server
            return builder
        }
        // check can spectate
        if (!server.canJoin) {
            // Unjoinable
            if (!server.canSpectate) {
                val builder = ItemStack(
                    XMaterial.matchXMaterial(CyanJoiner.settings.getString("icons.unjoinable.type")).orElse(
                        XMaterial.AIR).parseMaterial()!!).apply {
                    val itemMeta = this.itemMeta
                    itemMeta.apply {
                        this.displayName = CyanJoiner.settings.getString("icons.unjoinable.name")!!.replace("%name%",
                                                                                                            server.name).replace(
                                "&", "§")
                        lore = CyanJoiner.settings.getString("icons.unjoinable.lore")!!.replace("%player%",
                                                                                                server.currentState.current.toString()).replace(
                                "%motd%", server.currentState.lore).lines()
                        if (CyanJoiner.settings.getBoolean("icons.unjoinable.shiny")) {
                            this.addEnchant(Enchantment.DURABILITY, 1, true)
                            this.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                            this.addItemFlags(ItemFlag.HIDE_ENCHANTS)
                        }
                    }
                    this.itemMeta = itemMeta
                }
                itemServerMap[builder] = server
                return builder
            }
            // Can spectate
            let {
                val builder = ItemStack(
                    XMaterial.matchXMaterial(CyanJoiner.settings.getString("icons.spectateble.type")).orElse(
                        XMaterial.AIR).parseMaterial()!!).apply {
                    val itemMeta = this.itemMeta
                    itemMeta.apply {
                        this.displayName = CyanJoiner.settings.getString("icons.spectateble.name")!!.replace("%name%",
                                                                                                             server.name).replace(
                                "&", "§")
                        lore = CyanJoiner.settings.getString("icons.spectateble.lore")!!.replace("%player%",
                                                                                                 server.currentState.current.toString()).replace(
                                "%motd%", server.currentState.lore).lines()
                        if (CyanJoiner.settings.getBoolean("icons.spectateble.shiny")) {
                            this.addEnchant(Enchantment.DURABILITY, 1, true)
                            this.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                            this.addItemFlags(ItemFlag.HIDE_ENCHANTS)
                        }
                    }
                    this.itemMeta = itemMeta
                }
                itemServerMap[builder] = server
                return builder
            }
        }
        // Joinable
        val builder = ItemStack(XMaterial.matchXMaterial(CyanJoiner.settings.getString("icons.joinable.type")).orElse(
            XMaterial.AIR).parseMaterial()!!).apply {
            val itemMeta = this.itemMeta
            itemMeta.apply {
                this.displayName = CyanJoiner.settings.getString("icons.joinable.name")!!.replace("%name%",
                                                                                                  server.name).replace(
                        "&", "§")
                lore = CyanJoiner.settings.getString("icons.joinable.lore")!!.replace("%player%",
                                                                                      server.currentState.current.toString()).replace(
                        "%motd%", server.currentState.lore).lines()
                if (CyanJoiner.settings.getBoolean("icons.joinable.shiny")) {
                    this.addEnchant(Enchantment.DURABILITY, 1, true)
                    this.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                    this.addItemFlags(ItemFlag.HIDE_ENCHANTS)
                }
            }
            this.itemMeta = itemMeta
        }
        itemServerMap[builder] = server
        return builder
    }

    @JvmName("quick") fun getQuick(): Inventory {
        updateGUI()
        return quick
    }

    @JvmName("total") fun getTotal(): Inventory {
        updateGUI()
        return total
    }
}