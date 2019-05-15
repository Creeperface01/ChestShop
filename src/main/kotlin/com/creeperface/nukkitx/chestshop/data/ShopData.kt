package com.creeperface.nukkitx.chestshop.data

import cn.nukkit.item.Item
import cn.nukkit.math.BlockVector3
import cn.nukkit.nbt.tag.CompoundTag
import com.creeperface.nukkitx.chestshop.ChestShop
import com.creeperface.nukkitx.chestshop.util.toList
import com.creeperface.nukkitx.chestshop.util.toNBT
import com.creeperface.nukkitx.chestshop.util.toSimpleItem
import com.creeperface.nukkitx.chestshop.util.toSimpleNBT

/**
 * @author CreeperFace
 */
data class ShopData(var item: Item, var price: Int, var owner: String, val containers: MutableList<BlockVector3>) {

    fun serialize(): CompoundTag {
        return CompoundTag(ChestShop.SHOP_TAG)
                .putCompound("item", item.toSimpleNBT())
                .putInt("price", price)
                .putString("owner", owner.toLowerCase())
                .putList(containers.toNBT("containers") {
                    val data = CompoundTag()

                    data.putInt("x", it.x)
                    data.putInt("y", it.y)
                    data.putInt("z", it.z)

                    return@toNBT data
                })
    }

    companion object {

        fun deserialize(nbt: CompoundTag) = ShopData(
                nbt.getCompound("item").toSimpleItem(),
                nbt.getInt("price"),
                nbt.getString("owner"),
                nbt.getList("containers").toList { _, tag ->
                    tag as CompoundTag

                    return@toList BlockVector3(
                            tag.getInt("x"),
                            tag.getInt("y"),
                            tag.getInt("z")
                    )
                }.toMutableList()
        )
    }
}