package cn.cyanbukkit.cyanjoiner.server

import com.comphenix.protocol.ProtocolLibrary
import org.bukkit.entity.Player

enum class MineCraftVersion(val version: String, val protocol: Int) {
    V1_8("Java版1.8到1.8.9", 47),
    V1_9("1.9", 107),
    V1_9_1("1.9.1", 108),
    V1_9_2("1.9.2", 109),
    V1_9_3("1.9.3", 110),
    V1_9_4("1.9.4", 110),
    V1_10("1.10", 210),
    V1_10_1("1.10.1", 210),
    V1_10_2("1.10.2", 210),
    V1_11("1.11", 315),
    V1_11_1("1.11.1", 316),
    V1_11_2("1.11.2", 316),
    V1_12("1.12", 335),
    V1_12_1("1.12.1", 338),
    V1_12_2("1.12.2", 340),
    V1_13("1.13", 393),
    V1_13_1("1.13.1", 401),
    V1_13_2("1.13.2", 404),
    V1_14("1.14", 477),
    V1_14_1("1.14.1", 480),
    V1_14_2("1.14.2", 485),
    V1_14_3("1.14.3", 490),
    V1_14_4("1.14.4", 498),
    V1_15("1.15", 573),
    V1_15_1("1.15.1", 575),
    V1_15_2("1.15.2", 578),
    V1_16("1.16", 735),
    V1_16_1("1.16.1", 736),
    V1_16_2("1.16.2", 751),
    V1_16_3("1.16.3", 753),
    V1_16_4("1.16.4", 754),
    V1_16_5("1.16.5", 754),
    V1_17("1.17", 755),
    V1_17_1("1.17.1", 756),
    V1_18("1.18", 757),
    V1_18_1("1.18.1", 757),
    V1_18_2("1.18.2", 758),
    V1_19("1.19", 759),
    V1_19_1("1.19.1", 760),
    V1_19_2("1.19.2", 760),
    V1_19_3("1.19.3", 761),
    V1_19_4("1.19.4", 762),
    V1_20("1.20", 	763),
    V1_20_1("1.20.1", 	763),
    V1_20_2("1.20.2", 	764);



    companion object {
        fun Player.support(): MineCraftVersion {
            val version = ProtocolLibrary.getProtocolManager().getProtocolVersion(player);
            // 通过反射获取玩家的版本号
//            val version = this.javaClass.getMethod("getHandle")
//                .invoke(this).javaClass.getField("playerConnection")
//                .get(this.javaClass.getMethod("getHandle")
//                    .invoke(this)).javaClass.getMethod("getNetworkManager")
//                .invoke(this.javaClass.getMethod("getHandle")
//                    .invoke(this)).javaClass.getMethod("getVersion")
//                .invoke(this.javaClass.getMethod("getHandle")
//                    .invoke(this)).toString()
            return MineCraftVersion.values().find { it.protocol == version } ?: V1_8
        }
    }


}