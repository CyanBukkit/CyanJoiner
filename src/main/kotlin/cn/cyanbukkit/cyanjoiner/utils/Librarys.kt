package cn.cyanbukkit.cyanjoiner.utils

import cn.cyanbukkit.cyanjoiner.CyanJoiner
import cn.cyanbukkit.cyanjoiner.cyanlib.launcher.CyanPluginLauncher.cyanPlugin
import cn.cyanbukkit.cyanjoiner.server.MineCraftVersion
import cn.cyanbukkit.cyanjoiner.server.Server
import cn.cyanbukkit.cyanjoiner.server.ServerInfo
import com.comphenix.protocol.ProtocolLibrary
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.*
import net.md_5.bungee.chat.ComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.SimpleCommandMap
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.ProtocolException
import java.net.Socket
import java.nio.charset.StandardCharsets
import kotlin.experimental.and
import kotlin.time.ExperimentalTime

fun Command.register(prefix: String) {
    val pluginManagerClazz = Bukkit.getServer().pluginManager.javaClass
    val field = pluginManagerClazz.getDeclaredField("commandMap")
    field.isAccessible = true
    val commandMap = field.get(Bukkit.getServer().pluginManager) as SimpleCommandMap
    commandMap.register(prefix, this)
}



fun Listener.register(plugin: Plugin) = Bukkit.getServer().pluginManager.registerEvents(this, plugin)

fun Player.connectTo(server: Server) {

    Bukkit.getMessenger().registerOutgoingPluginChannel(cyanPlugin, "BungeeCord")
    // 这个怎么办
    val buf = ByteArrayOutputStream()
    val out = DataOutputStream(buf)
    try {
        out.writeUTF("Connect")
        out.writeUTF(server.bcName)
        sendPluginMessage(cyanPlugin, "BungeeCord", buf.toByteArray())
    } catch (e1: IOException) {
        e1.printStackTrace()
    }
}

fun String.changeVersion(version: String): String {
    return this.replace("%version%", MineCraftVersion.values().find {
        version.contains(it.protocol.toString())  }?.version ?: version)
}



val timeout: Long
    get() = CyanJoiner.settings.getLong("timeout")

@OptIn(ExperimentalTime::class)
suspend fun ping(url: String, port: Int): ServerInfo = withTimeout(timeout) {
    withContext(Dispatchers.IO) {
        val socket = Socket()
        try {
            socket.connect(InetSocketAddress(url, port))
        } catch (e: Exception) {
            return@withContext ServerInfo(false, 0, 0, "")
        }
        val inputStream = socket.getInputStream()
        val dataInputStream = DataInputStream(inputStream)
        val outputStream = socket.getOutputStream()
        val dataOutputStream = DataOutputStream(outputStream)
        val bs = ByteArrayOutputStream()
        val out = DataOutputStream(bs)

        // Send Handshake
        out.write(0)
        writeVarInt(out, 4)
        writeString(out, url)
        out.writeShort(port)
        writeVarInt(out, 1)
        sendPacket(dataOutputStream, bs.toByteArray())

        // Query
        val result = runCatching {
            sendPacket(dataOutputStream, ByteArray(1))
            readVarInt(dataInputStream)
            val packetId: Int = readVarInt(dataInputStream)
            if (packetId != 0) {
                throw IOException("Invalid packetId")
            }
            val stringLength: Int = readVarInt(dataInputStream)
            if (stringLength < 1) {
                throw IOException("Invalid string length.")
            }
            val responseData = ByteArray(stringLength)
            dataInputStream.readFully(responseData)
            responseData.decodeToString()
        }.getOrElse {
            println("§c| §7 Error while contacting server: ${url}:$port")
            return@withContext ServerInfo(false, 0, 0, "")
        }

        try {
            val jsonObject = JsonParser().parse(result).asJsonObject
            val online = jsonObject.get("players").asJsonObject.get("online").asInt
            val max = jsonObject.get("players").asJsonObject.get("max").asInt
            val description = jsonObject.get("description").let {
                if (it is JsonObject) {
                    val parse = ComponentSerializer.parse(it.toString())
                    val ret = StringBuffer()
                    parse.forEach { comp ->
                        ret.append(comp.toLegacyText())
                    }
                    return@let ret.toString()
                }
                it.asString
            }
            return@withContext ServerInfo(true, online, max, description)
        } catch (e: Throwable) {
            return@withContext ServerInfo(false, 0, 0, "")
        }
    }
}

private fun sendPacket(out: DataOutputStream, data: ByteArray) {
    writeVarInt(out, data.size)
    out.write(data)
    out.flush()
}

private fun readVarInt(`in`: DataInputStream): Int {
    var i = 0
    var j = 0
    var k: Byte
    do {
        k = `in`.readByte()
        i = i or ((k and 127).toInt() shl j++ * 7)
        if (j > 5) {
            throw RuntimeException("VarInt too big")
        }
    } while ((k.toInt() and 128) == 128)
    return i
}

private fun writeVarInt(out: DataOutputStream, paramInt: Int) {
    var temp = paramInt
    while (temp and -128 != 0) {
        out.write(temp and 127 or 128)
        temp = temp ushr 7
    }
    out.write(temp)
}

private fun writeString(out: DataOutputStream, string: String) {
    writeVarInt(out, string.length)
    out.write(string.toByteArray(StandardCharsets.UTF_8))
}


val pluginScope = CoroutineScope(SupervisorJob() + CoroutineExceptionHandler { _, except ->
    except.printStackTrace()
})


/**
 * @param metarial|amout|name|lore{xx==xx==xx==}
 */
fun String.changeItemStack() : ItemStack {
    // metarial|amout|name|lore
    val str = this.split("|")
    val itemStack = ItemStack(Material.valueOf(str[0])).apply {
        this.amount = str[1].toInt()
        val itemMeta = this.itemMeta
        itemMeta.apply {
            this.displayName = str[2]
            lore = str[3].split("==")
        }
        this.itemMeta = itemMeta
    }
    return itemStack
}

