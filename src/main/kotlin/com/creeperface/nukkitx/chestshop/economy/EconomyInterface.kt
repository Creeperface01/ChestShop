package com.creeperface.nukkitx.chestshop.economy

import cn.nukkit.Player

/**
 * @author CreeperFace
 */
interface EconomyInterface {

    fun hasMoney(p: Player, amount: Int) = hasMoney(p.name, amount)

    /**
     * isn't called directly but can be used for name based economy systems
     */
    fun hasMoney(p: String, amount: Int): Boolean

    fun subtractMoney(p: Player, amount: Int) = subtractMoney(p.name, amount)

    /**
     * isn't called directly but can be used for name based economy systems
     */
    fun subtractMoney(p: String, amount: Int)

    fun addMoney(p: Player, amount: Int) = addMoney(p.name, amount)

    fun addMoney(p: String, amount: Int)

    //    @JvmDefault
    fun take(p: Player, amount: Int): Boolean {
        if (!hasMoney(p, amount)) {
            return false
        }

        subtractMoney(p, amount)
        return true
    }

    //    @JvmDefault
    fun transfer(from: String, to: String, amount: Int) {
        subtractMoney(from, amount)
        addMoney(to, amount)
    }
}