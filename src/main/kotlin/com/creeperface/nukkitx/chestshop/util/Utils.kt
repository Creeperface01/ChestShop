package com.creeperface.nukkitx.chestshop.util

import cn.nukkit.math.BlockFace
import cn.nukkit.math.BlockFace.Axis

/**
 * @author CreeperFace
 */
object Utils {

    private val BLOCK_FACE_LOOKUP = arrayOfNulls<Array<BlockFace>>(16)

    fun getFace(meta: Int): BlockFace {
        var face = BlockFace.fromIndex(meta)

        if (face.axis == Axis.Y) {
            face = BlockFace.NORTH
        }

        return face
    }

    init {
        BLOCK_FACE_LOOKUP[15] = arrayOf(BlockFace.SOUTH)
        BLOCK_FACE_LOOKUP[0] = arrayOf(BlockFace.SOUTH)
        BLOCK_FACE_LOOKUP[1] = arrayOf(BlockFace.SOUTH)

        BLOCK_FACE_LOOKUP[2] = arrayOf(BlockFace.SOUTH, BlockFace.WEST)

        BLOCK_FACE_LOOKUP[3] = arrayOf(BlockFace.WEST)
        BLOCK_FACE_LOOKUP[4] = arrayOf(BlockFace.WEST)
        BLOCK_FACE_LOOKUP[5] = arrayOf(BlockFace.WEST)

        BLOCK_FACE_LOOKUP[6] = arrayOf(BlockFace.WEST, BlockFace.NORTH)

        BLOCK_FACE_LOOKUP[7] = arrayOf(BlockFace.NORTH)
        BLOCK_FACE_LOOKUP[8] = arrayOf(BlockFace.NORTH)
        BLOCK_FACE_LOOKUP[9] = arrayOf(BlockFace.NORTH)

        BLOCK_FACE_LOOKUP[10] = arrayOf(BlockFace.NORTH, BlockFace.EAST)

        BLOCK_FACE_LOOKUP[11] = arrayOf(BlockFace.EAST)
        BLOCK_FACE_LOOKUP[12] = arrayOf(BlockFace.EAST)
        BLOCK_FACE_LOOKUP[13] = arrayOf(BlockFace.EAST)

        BLOCK_FACE_LOOKUP[14] = arrayOf(BlockFace.EAST, BlockFace.SOUTH)
    }

    fun getSignFace(meta: Int): Array<BlockFace> {
        return BLOCK_FACE_LOOKUP[meta]!!
    }
}
