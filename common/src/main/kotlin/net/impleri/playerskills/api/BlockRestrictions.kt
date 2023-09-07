package net.impleri.playerskills.api

import net.impleri.playerskills.restrictions.RestrictionsApi
import net.impleri.playerskills.restrictions.blocks.BlockRestriction
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.core.BlockPos
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import java.lang.reflect.Field
import net.impleri.playerskills.restrictions.Registry as RestrictionsRegistry

class BlockRestrictions private constructor(
  registry: RestrictionsRegistry<BlockRestriction>,
  fields: Array<Field>,
) : RestrictionsApi<Block, BlockRestriction>(
  registry,
  fields,
  PlayerSkillsLogger.BLOCKS,
) {
  override fun getTargetName(target: Block): ResourceLocation {
    return getName(target)
  }

  override fun createPredicateFor(target: Block): (Block) -> Boolean {
    return { isSameBlock(it.defaultBlockState(), target) }
  }

  private fun canHelper(player: Player?, target: Block, pos: BlockPos?, fieldName: String): Boolean {
    player ?: return DEFAULT_CAN_RESPONSE
    val dimension = player.level.dimension().location()
    val actualPos = pos ?: player.onPos
    val biome = player.level.getBiome(actualPos).unwrapKey().orElseThrow().location()

    return canPlayer(
      player,
      target,
      dimension,
      biome,
      null,
      fieldName,
    )
  }

  fun isBreakable(player: Player?, block: Block, pos: BlockPos): Boolean {
    return canHelper(player, block, pos, "breakable")
  }

  fun isBreakable(player: Player?, block: BlockState, pos: BlockPos): Boolean {
    return isBreakable(player, block.block, pos)
  }

  fun isHarvestable(player: Player?, block: Block, pos: BlockPos): Boolean {
    return canHelper(player, block, pos, "harvestable")
  }

  fun isHarvestable(player: Player?, block: BlockState, pos: BlockPos): Boolean {
    return isHarvestable(player, block.block, pos)
  }

  fun isUsable(player: Player?, block: Block, pos: BlockPos): Boolean {
    return canHelper(player, block, pos, "usable")
  }

  fun isUsable(player: Player?, block: BlockState, pos: BlockPos): Boolean {
    return isUsable(player, block.block, pos)
  }

  companion object {
    internal val RestrictionRegistry = RestrictionsRegistry<BlockRestriction>()

    private val allRestrictionFields = BlockRestriction::class.java.declaredFields

    internal val INSTANCE = BlockRestrictions(RestrictionRegistry, allRestrictionFields)

    fun add(name: ResourceLocation, restriction: BlockRestriction) {
      RestrictionRegistry.add(name, restriction)
    }

    fun getBlockState(blockPos: BlockPos?, level: Level): BlockState {
      return blockPos?.let { level.getBlockState(it) } ?: Blocks.AIR.defaultBlockState()
    }

    fun getValue(blockState: BlockState): Block {
      return blockState.block
    }

    fun getValue(block: ResourceLocation?): Block {
      return Registry.BLOCK[block]
    }

    fun isSameBlock(a: BlockState, b: Block?): Boolean {
      return b?.let { a.`is`(it) } ?: false
    }

    fun isEmptyBlock(block: Block?): Boolean {
      val defaultBlock = Blocks.AIR.defaultBlockState()
      return !isBlock(block) || isSameBlock(defaultBlock, block)
    }

    fun isBlock(block: Block?): Boolean {
      return block != null
    }

    fun isBlock(blockState: BlockState?): Boolean {
      return blockState != null
    }

    fun isReplacedBlock(a: BlockState, b: Block?): Boolean {
      return isBlock(a) && isBlock(b) && !isSameBlock(a, b)
    }

    fun isReplacedBlock(a: BlockState, b: BlockState): Boolean {
      return isBlock(a) && isBlock(b) && !isSameBlock(a, b.block)
    }

    fun getName(block: Block?): ResourceLocation {
      return Registry.BLOCK.getKey(block ?: Blocks.AIR)
    }

    fun getName(blockState: BlockState): ResourceLocation {
      return getName(getValue(blockState))
    }

    private fun getFluidBlockReplacement(player: Player, original: BlockState, pos: BlockPos): BlockState? {
      if (FluidRestrictions.isFluidBlock(original)) {
            return FluidRestrictions.getReplacementBlock(player, original, pos)
        }

        return null
    }

    private fun getBlockReplacement(player: Player, original: BlockState, pos: BlockPos): BlockState {
      val level = player.getLevel()
      val replacement = INSTANCE.getReplacementFor(
        player,
        original.block,
        level.dimension().location(),
        level.getBiome(pos).unwrapKey().orElseThrow().location(),
      )
      if (isReplacedBlock(original, replacement)) {
        PlayerSkillsLogger.BLOCKS.debug("Replacement for ${getName(original)} is ${getName(replacement)}")
        return replacement.defaultBlockState()
      }

      return original
    }

    fun getReplacement(player: Player, original: BlockState, pos: BlockPos): BlockState {
      return getFluidBlockReplacement(player, original, pos) ?: getBlockReplacement(player, original, pos)
    }

    // Used in mixins for detecting if the block should burn
    fun getReplacement(level: BlockGetter, blockPos: BlockPos): BlockState {
      val original = level.getBlockState(blockPos)

      // If we have a level, we can find the nearest player and get a restriction
      if (level is Level) {
        val player = level.getNearestPlayer(blockPos.x.toDouble(), blockPos.y.toDouble(), blockPos.z.toDouble(), 128.0, false)
        player?.let { return getReplacement(it, original, blockPos) }
      }

      return original
    }

    fun maybeGetReplacement(player: Player, original: BlockState, pos: BlockPos): BlockState? {
      val replacement = getReplacement(player, original, pos)

      return if (isReplacedBlock(original, replacement)) replacement else null
    }

    fun getReplacementId(player: Player?, original: BlockState, pos: BlockPos?): Int {
      var replacement: BlockState = original

      // Inject replacement block
      player?.let {
        // Slightly hacky here but we don't know the blockPos when this method is called in Palettes, so we're assuming
        // the player's current location for biome matches
        val actualPos = pos ?: it.blockPosition()
        val maybeReplacement = getReplacement(it, original, actualPos)
        if (isReplacedBlock(original, maybeReplacement)) {
          replacement = maybeReplacement
        }
      }

      return Block.BLOCK_STATE_REGISTRY.getId(replacement)
    }

    fun getReplacementsCountFor(player: Player?): Long {
      return INSTANCE.countReplacementsFor(player!!)
    }

    fun isUsable(player: Player?, blockState: BlockState?, pos: BlockPos?): Boolean {
      return INSTANCE.isUsable(player, blockState!!, pos!!)
    }

    fun isBreakable(player: Player?, blockState: BlockState?, pos: BlockPos?): Boolean {
      return INSTANCE.isBreakable(player, blockState!!, pos!!)
    }

    fun isBreakable(instance: BlockGetter, blockPos: BlockPos): Boolean {
      val original = instance.getBlockState(blockPos)

      // If we have a level, we can find the nearest player and get a restriction
      if (instance is Level) {
        val player =
          instance.getNearestPlayer(blockPos.x.toDouble(), blockPos.y.toDouble(), blockPos.z.toDouble(), 128.0, false)
        if (player != null) {
          val replacement = getReplacement(player, original, blockPos)
          return INSTANCE.isBreakable(player, replacement, blockPos)
        }
      }
      return true
    }

    fun isHarvestable(player: Player, original: BlockState, blockPos: BlockPos): Boolean {
      val replacement = getReplacement(player, original, blockPos)
      val canHarvest = checkHarvestable(player, replacement, blockPos)
      PlayerSkillsLogger.BLOCKS.debug("Can ${player.name.string} harvest ${getName(original)} (as ${getName(replacement)})  right now? $canHarvest")

      return canHarvest
    }

    private fun checkHarvestable(player: Player, blockState: BlockState, pos: BlockPos): Boolean {
      return INSTANCE.isHarvestable(player, blockState, pos) && isBreakable(player, blockState, pos)
    }

    private val EMPTY_DROPS: List<ItemStack> = ArrayList()
    
    fun getDrops(
      player: Player,
      original: BlockState,
      serverLevel: ServerLevel,
      blockPos: BlockPos,
      blockEntity: BlockEntity?,
      tool: ItemStack,
    ): List<ItemStack>? {
      val replacement = getReplacement(player, original, blockPos)

      // Determine drops from replacement block
      if (isReplacedBlock(original, replacement)) {
        val drops = if (checkHarvestable(player, replacement, blockPos)) {
          Block.getDrops(
            replacement,
            serverLevel,
            blockPos,
            blockEntity,
            player,
            tool,
          )
        } else {
          EMPTY_DROPS
        }
        PlayerSkillsLogger.BLOCKS.debug("Drops for ${getName(original)} (${getName(replacement)}) are: $drops")

        return drops
      }

      // Maybe prevent drops
      if (!checkHarvestable(player, original, blockPos)) {
        PlayerSkillsLogger.BLOCKS.debug("Block ${getName(original)} is not droppable")

        return EMPTY_DROPS
      }

      return null
    }
  }
}
