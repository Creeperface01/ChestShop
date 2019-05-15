package com.creeperface.nukkitx.chestshop

import cn.nukkit.Player
import cn.nukkit.block.Block
import cn.nukkit.block.BlockChest
import cn.nukkit.block.BlockSignPost
import cn.nukkit.block.BlockWallSign
import cn.nukkit.blockentity.BlockEntityChest
import cn.nukkit.blockentity.BlockEntitySign
import cn.nukkit.command.Command
import cn.nukkit.command.CommandSender
import cn.nukkit.event.EventHandler
import cn.nukkit.event.EventPriority
import cn.nukkit.event.Listener
import cn.nukkit.event.block.BlockBreakEvent
import cn.nukkit.event.block.BlockBurnEvent
import cn.nukkit.event.block.BlockIgniteEvent
import cn.nukkit.event.block.BlockPlaceEvent
import cn.nukkit.event.entity.EntityExplodeEvent
import cn.nukkit.event.player.PlayerInteractEvent
import cn.nukkit.inventory.InventoryHolder
import cn.nukkit.item.Item
import cn.nukkit.math.BlockFace
import cn.nukkit.math.BlockFace.Plane
import cn.nukkit.math.BlockVector3
import cn.nukkit.math.Vector3
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.permission.Permissible
import cn.nukkit.plugin.PluginBase
import cn.nukkit.utils.Config
import cn.nukkit.utils.TextFormat
import com.creeperface.nukkitx.chestshop.data.ShopData
import com.creeperface.nukkitx.chestshop.economy.EconomyInterface
import com.creeperface.nukkitx.chestshop.util.*
import java.io.File
import java.util.*

/**
 * @author CreeperFace
 */
class ChestShop : PluginBase(), Listener {

    lateinit var economy: EconomyInterface

    private val actions = mutableMapOf<Long, Action>()

    init {
        INSTANCE = this
    }

    override fun onEnable() {
        this.server.pluginManager.registerEvents(this, this)
        saveResource("czech.yml")
        saveResource("english.yml")

        Lang.init(Config(File(dataFolder, "czech.yml"), Config.YAML).all)
    }

    private fun tryCreateShop(p: Player, b: Block): Boolean {
        val blockEntity = b.getBlockEntity() as? BlockEntitySign ?: return false

        if (blockEntity.isShop()) {
            return false
        }

        val lines = blockEntity.text

        if (lines.size != 4) {
            return false
        }

        val line1 = lines[0].toLowerCase().trim()

        if ((line1 == "[shop]" || line1 == "/shop")) {
            val countString = lines[1].toLowerCase().trim()
            val priceString = lines[2].toLowerCase().trim()
            val itemString = lines[3].toLowerCase().trim()

            val count: Int
            val price: Int

            try {
                count = countString.toInt()
            } catch (ex: NumberFormatException) {
                p.sendTranslated("err_count")
                return false
            }

            if (count <= 0) {
                p.sendTranslated("negative_count")
                return false
            }

            try {
                price = priceString.toInt()
            } catch (ex: NumberFormatException) {
                p.sendTranslated("err_price")
                return false
            }

            if (price < 0) {
                p.sendTranslated("negative_price")
                return false
            }

            val item: Item

            try {
                item = Item.fromString(itemString)
            } catch (ex: Exception) {
                p.sendTranslated("err_item", itemString)
                return false
            }

            if (item.id == 0) {
                p.sendTranslated("item_air")
                return false
            }

            item.setCount(count)

            val chestPos = checkShopCreation(b) ?: return false

            val chest = b.level.getBlockEntity(chestPos) as? BlockEntityChest ?: return false
            val signCompound = CompoundTag().putCompound("sign", b.location.clone().asBlockVector3().toCompoundTag())

            chest.namedTag.putCompound(SHOP_CONT_TAG, signCompound)

            val containers = if (chest.pair != null) {
                chest.pair.namedTag.putCompound(SHOP_CONT_TAG, signCompound)
                listOf(chest.pair.location, chestPos)
            } else {
                listOf(chestPos)
            }.map { it.asBlockVector3() }

            blockEntity.createShop(ShopData(item, price, p.name, containers.toMutableList()))

            blockEntity.setText("${TextFormat.GRAY}[${TextFormat.GREEN}SHOP${TextFormat.GRAY}]", TextFormat.GRAY.toString() + p.name, "${TextFormat.YELLOW}${item.name} ${TextFormat.GRAY}(${TextFormat.GREEN}${item.count}x${TextFormat.GRAY})", "${TextFormat.GRAY}price: ${TextFormat.GREEN}$price")

            p.sendTranslated("success")
            return true
        }

        return false
    }

    private fun checkShopCreation(sign: Block): Vector3? {
        val chests = EnumMap<BlockFace, BlockChest>(BlockFace::class.java)

        for (face in Plane.HORIZONTAL) {
            val chest = sign.getSide(face) as? BlockChest ?: continue

            if (Utils.getFace(chest.damage) != face.opposite) {
                continue
            }

            chests[face] = chest
        }

        val availableChests = EnumMap<BlockFace, BlockChest>(BlockFace::class.java)

        for ((key, chest) in chests) {
            val be = chest.getBlockEntity() ?: continue

            if (be.isShop()) {
                continue
            }

            availableChests[key] = chest
        }

        var face: BlockFace? = null //sign face

        if (sign is BlockWallSign) {
            face = Utils.getFace(sign.getDamage())
        } else {
            val faces = Utils.getSignFace(sign.damage)

            loop@ for (blockFace in availableChests.keys) {
                for (signFace in faces) {
                    if (blockFace.opposite == signFace) {
                        face = signFace
                        break@loop
                    }
                }
            }
        }

        if (face == null || !availableChests.containsKey(face.opposite)) {
            return null
        }

        if (sign !is BlockWallSign) {
            sign.getLevel().setBlock(sign, BlockWallSign(face.index), true, false)
        }

        return sign.location.getSide(face.opposite)
    }

    private fun removeShop(b: Block) {
        val nbt = b.getBlockEntity()?.namedTag ?: return

        fun removeShop(pos: BlockVector3, sign: CompoundTag) {
            fun removeNBT(vec: BlockVector3) {
                b.level.getBlockEntity(vec)?.let {
                    it.namedTag.remove(SHOP_CONT_TAG)
                    it.namedTag.remove(SHOP_TAG)
                }
            }

            val shopData = ShopData.deserialize(sign)

            shopData.containers.forEach { removeNBT(it) }

            removeNBT(pos)
            b.level.useBreakOn(pos.asVector3())
        }

        if (nbt.contains(SHOP_CONT_TAG)) {
            val signPos = nbt.getCompound(SHOP_CONT_TAG).getCompound("sign")?.toBlockVector3() ?: return

            val be = b.level.getBlockEntity(signPos) as? BlockEntitySign ?: return

            if (be.namedTag.contains(SHOP_TAG)) {
                removeShop(signPos, be.namedTag.getCompound(SHOP_TAG))
            }
        } else if (nbt.contains(SHOP_TAG)) {
            removeShop(b.asBlockVector3(), nbt.getCompound(SHOP_TAG))
        }
    }

    fun checkAction(b: Block, p: Player): Boolean {
        if (p.hasPermission("chestshop.admin")) return true

        val blockEntity = b.getBlockEntity() ?: return true

        val data = blockEntity.getShopData() ?: return true

        return data.owner.equals(p.name, true)
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    fun onBlockBreak(e: BlockBreakEvent) {
        val b = e.block
        val p = e.player

        if (!checkAction(b, p)) {
            e.setCancelled()
            return
        }

        if (b.isShop()) {
            removeShop(b)
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    fun onBlockPlace(e: BlockPlaceEvent) {
        val b = e.block
        val p = e.player

        if (b.id != Block.CHEST) {
            return
        }

        val direction = p?.direction ?: BlockFace.NORTH

        val side = direction.rotateYCCW()

        for (face in arrayOf(side, side.opposite)) {
            val c = b.getSide(face)

            if (c is BlockChest && Utils.getFace(c.getDamage()) == direction.opposite) {
                val blockEntity = c.getBlockEntity()

                if (blockEntity is BlockEntityChest && !blockEntity.isPaired && blockEntity.isShop()) {
                    p.sendTranslated("chest_is_shop")
                    e.setCancelled()
                    return
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onInteract(e: PlayerInteractEvent) {
        val b = e.block
        val p = e.player

        if (e.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            return
        }

        actions.remove(p.id)?.let { action ->
            if (e.isCancelled) {
                return
            }

            when (action) {
                Action.CREATE -> {
                    if (!tryCreateShop(p, b)) {
                        p.sendTranslated("fail")
                    }
                }
                Action.REMOVE -> {
                    if (checkAction(b, p)) {
                        removeShop(b)
                    }
                }
            }

            e.setCancelled()
            return
        }

        if (b is BlockSignPost && b.isShop()) {
            e.setCancelled()
            val shopData = b.getShopData() ?: return

            if (shopData.owner.equals(p.name, true)) {
                return
            }

            if (shopData.containers.isEmpty()) {
                p.sendMessage("empty containers")
                return
            }

            val be = b.level.getBlockEntity(shopData.containers.singleOrNull()?.asVector3())

            if (be !is InventoryHolder) {
                p.sendTranslated("shop_not_exists")
                return
            }

            val inv = be.inventory

            if (!inv.contains(shopData.item)) {
                p.sendTranslated("shop_empty")
                return
            }

            if (!economy.hasMoney(p, shopData.price)) {
                p.sendTranslated("money")
                return
            }

            p.inventory.addItem(shopData.item.clone())
            economy.transfer(p.name, shopData.owner, shopData.price)
            inv.removeItem(shopData.item)

            p.sendTranslated("purchase", shopData.item.count.toString(), shopData.item.name)
            return
        }

        if (!checkAction(e.block, e.player)) {
            e.setCancelled()
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    fun onBurn(e: BlockBurnEvent) {
        if (e.block.isShop()) {
            e.setCancelled()
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    fun onIgnite(e: BlockIgniteEvent) {
        if (e.block.isShop()) {
            e.setCancelled()
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    fun onExplosion(e: EntityExplodeEvent) {
        e.blockList = e.blockList.filter { !it.isShop() }
    }

    override fun onCommand(p: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        if (p !is Player) {
            return false
        }

        if (!cmd.name.equals("chestshop", true)) {
            return false
        }

        if (args.isEmpty()) {
            return false
        }

        if (!p.hasShopPermission("create")) {
            return false
        }

        when (args[0]) {
            "create" -> actions[p.id] = Action.CREATE
            "remove" -> actions[p.id] = Action.REMOVE
            "reset" -> actions.remove(p.id)
            else -> {
                return false
            }
        }

        p.sendTranslated("click_block")
        return true
    }

    private fun Permissible.hasShopPermission(perm: String) = this.hasPermission("chestshop.admin") || this.hasPermission("chestshop.$perm")

    companion object {

        lateinit var INSTANCE: ChestShop

        const val SHOP_TAG = "chestshop"
        const val SHOP_CONT_TAG = "chestshopcont"
    }

    enum class Action {
        CREATE,
        REMOVE
    }
}
