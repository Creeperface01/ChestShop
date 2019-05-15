package com.creeperface.nukkitx.chestshop.economy

import cn.nukkit.Player
import me.onebone.economyapi.EconomyAPI

class EconomyAPIInterface : EconomyInterface {

    private val economy: EconomyAPI = EconomyAPI.getInstance()

    override fun hasMoney(p: String, amount: Int) = economy.myMoney(p) >= amount


    override fun hasMoney(p: Player, amount: Int) = economy.myMoney(p) >= amount

    override fun subtractMoney(p: String, amount: Int) {
        economy.reduceMoney(p, amount.toDouble())
    }

    override fun addMoney(p: String, amount: Int) {
        economy.addMoney(p, amount.toDouble())
    }
}