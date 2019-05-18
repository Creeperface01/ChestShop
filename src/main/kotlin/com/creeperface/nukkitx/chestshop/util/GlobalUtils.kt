package com.creeperface.nukkitx.chestshop.util

import cn.nukkit.Player
import cn.nukkit.block.Block
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.blockentity.BlockEntitySign
import cn.nukkit.item.Item
import cn.nukkit.level.Level
import cn.nukkit.level.Position
import cn.nukkit.math.BlockVector3
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.ListTag
import cn.nukkit.nbt.tag.Tag
import com.creeperface.nukkitx.chestshop.ChestShop
import com.creeperface.nukkitx.chestshop.Lang
import com.creeperface.nukkitx.chestshop.data.ShopData

/**
 * @author CreeperFace
 */

fun BlockEntity.isShop() = this.namedTag.contains(ChestShop.SHOP_TAG) || this.namedTag.contains(ChestShop.SHOP_CONT_TAG)

fun BlockEntity.getShopData(): ShopData? {

    if (this.namedTag.contains(ChestShop.SHOP_TAG)) {
        this.namedTag.getCompound(ChestShop.SHOP_TAG)
    } else {
        val cont = this.namedTag.getCompound(ChestShop.SHOP_CONT_TAG) ?: return null

        val sign = cont.getCompound("sign") ?: return null

        val pos = sign.toBlockVector3()

        val be = this.level.getBlockEntity(pos.asVector3())

        if (be !is BlockEntitySign || !be.namedTag.contains(ChestShop.SHOP_TAG)) {
            return null
        }

        return be.getShopData()
    }

    return ShopData.deserialize(this.namedTag.getCompound(ChestShop.SHOP_TAG))
}

fun Block.getShopData() = this.getBlockEntity()?.getShopData()

fun BlockEntity.createShop(data: ShopData) {
    this.namedTag.putCompound(ChestShop.SHOP_TAG, data.serialize())
}

fun <T, R : Tag> List<T>.toNBT(name: String, converter: (T) -> R): ListTag<R> {
    val listTag = ListTag<R>(name)

    for (item in this) {
        listTag.add(converter(item))
    }

    return listTag
}

fun <T : Tag, R> ListTag<T>.toList(converter: (index: Int, T) -> R): List<R> {
    val list = mutableListOf<R>()

    if (size() <= 0) {
        return emptyList()
    }

    for (i in 0 until this.size()) {
        list.add(converter(i, get(i)))
    }

    return list
}

fun Item.toSimpleNBT() = CompoundTag()
        .putInt("id", id)
        .putInt("data", damage)
        .putInt("count", count)

fun CompoundTag.toSimpleItem() = Item.get(getInt("id"), getInt("data"), getInt("count"))

fun Block.getBlockEntity(): BlockEntity? = this.level.getBlockEntity(this)

fun Block.isShop() = getBlockEntity()?.isShop() ?: false

fun Position.isShop() = levelBlock.isShop()

fun CompoundTag.toBlockVector3(): BlockVector3 {
    val x = this.getInt("x")
    val y = getInt("y")
    val z = getInt("z")

    return BlockVector3(x, y, z)
}

fun BlockVector3.toCompoundTag(): CompoundTag {
    return CompoundTag()
            .putInt("x", this.x)
            .putInt("y", this.y)
            .putInt("z", this.z)
}

fun StringBuilder.replaceAll(from: String, to: String) {
    var index = this.indexOf(from)

    while (index != -1) {
        this.replace(index, index + from.length, to)
        index += to.length
        index = this.indexOf(from, index)
    }
}

fun Player.sendTranslated(message: String, vararg args: String) {
    this.sendMessage(Lang.translate(message, this, *args))
}

fun Level.getBlockEntity(position: BlockVector3) = this.getBlockEntity(position.asVector3())