package com.creeperface.nukkitx.chestshop.economy

import net.lldv.llamaeconomy.LlamaEconomy

class LlamaEconomyInterface : EconomyInterface {

    private val economy = LlamaEconomy.getAPI()

    override fun hasMoney(p: String, amount: Int): Boolean {
        return economy.getMoney(p) >= amount
    }

    override fun subtractMoney(p: String, amount: Int) {
        economy.reduceMoney(p, amount.toDouble())
    }

    override fun addMoney(p: String, amount: Int) {
        economy.addMoney(p, amount.toDouble())
    }
}