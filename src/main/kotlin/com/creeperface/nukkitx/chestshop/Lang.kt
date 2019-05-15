package com.creeperface.nukkitx.chestshop

import cn.nukkit.Player
import cn.nukkit.utils.TextFormat
import com.creeperface.nukkitx.chestshop.util.replaceAll

/**
 * @author CreeperFace
 */
object Lang {

    private val langs = mutableMapOf<String, String>()

    fun init(data: Map<String, Any>) {
        langs.clear()
        langs.putAll(data.mapValues { it.value.toString().replace('&', TextFormat.ESCAPE) })
    }

    fun translate(message: String, p: Player, vararg args: String): String {
        val translated = langs[message] ?: message

        if (args.isNotEmpty()) {
            val builder = StringBuilder(translated)

            args.forEachIndexed { index, arg ->
                builder.replaceAll("{%$index}", arg)
            }

            return builder.toString()
        }

        return translated
    }
}