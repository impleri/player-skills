package net.impleri.playerskills.api

import net.impleri.playerskills.restrictions.RestrictionsApi
import net.impleri.playerskills.restrictions.fluids.FluidRestriction
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.core.BlockPos
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntitySelector
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.LiquidBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.level.material.FluidState
import net.minecraft.world.level.material.Fluids
import java.lang.reflect.Field
import net.impleri.playerskills.restrictions.Registry as RestrictionsRegistry

class FluidRestrictions private constructor(
  registry: RestrictionsRegistry<FluidRestriction>,
  fields: Array<Field>,
) : RestrictionsApi<Fluid, FluidRestriction>(
  registry,
  fields,
  PlayerSkillsLogger.FLUIDS,
) {
  override fun getTargetName(target: Fluid): ResourceLocation {
    return Registry.FLUID.getKey(target)
  }

  override fun createPredicateFor(target: Fluid): (Fluid) -> Boolean {
    return { it.isSame(target) }
  }

  private fun canHelper(player: Player?, target: Fluid, pos: BlockPos?, fieldName: String): Boolean {
    player ?: return DEFAULT_CAN_RESPONSE

    val dimension = player.level.dimension().location()
    val actualPos = pos ?: player.onPos
    val biome = player.level.getBiome(actualPos).unwrapKey().orElseThrow().location()

    return canPlayer(player, target, dimension, biome, null, fieldName)
  }

  fun isBucketable(player: Player?, fluid: Fluid, pos: BlockPos?): Boolean {
    return canHelper(player, fluid, pos, "bucketable")
  }

  fun isBucketable(player: Player?, fluid: Fluid): Boolean {
    return isBucketable(player, fluid, null)
  }

  fun isProducible(player: Player?, fluid: Fluid, pos: BlockPos?): Boolean {
    PlayerSkillsLogger.FLUIDS.info("Checking if ${getFluidName(fluid)} is producible for ${player?.name?.string ?: "unknown"}")

    return canHelper(player, fluid, pos, "producible")
  }

  fun isProducible(player: Player?, fluid: Fluid): Boolean {
    return isProducible(player, fluid, null)
  }

  fun isConsumable(player: Player?, fluid: Fluid, pos: BlockPos?): Boolean {
    PlayerSkillsLogger.FLUIDS.info("Checking if {getFluidName(fluid)} is consumable for ${player?.name?.string ?: "unknown"}")

    return canHelper(player, fluid, pos, "consumable")
  }

  fun isConsumable(player: Player?, fluid: Fluid): Boolean {
    return isConsumable(player, fluid, null)
  }

  fun isIdentifiable(player: Player?, fluid: Fluid, pos: BlockPos?): Boolean {
    return canHelper(player, fluid, pos, "identifiable")
  }

  fun isIdentifiable(player: Player?, fluid: Fluid): Boolean {
    return isIdentifiable(player, fluid, null)
  }

  // Second layer cache: Finite/infinite value for (fluid, dimension, biome)
  fun getFiniteModeFor(fluid: Fluid, dimension: ResourceLocation?, biome: ResourceLocation?): FluidFiniteMode {
    val fluidName = getFluidName(fluid)
    val cacheKey = FiniteCacheKey(fluidName, dimension, biome)
    return finiteCache.computeIfAbsent(cacheKey) {
      populateFiniteRestriction(
        fluid,
        dimension,
        biome,
      )
    }
  }

  @JvmRecord
  private data class FiniteCacheKey(
    val fluid: ResourceLocation,
    val dimension: ResourceLocation?,
    val biome: ResourceLocation?,
  )

  private val finiteCache: MutableMap<FiniteCacheKey, FluidFiniteMode> = HashMap()

  private fun populateFiniteRestriction(
    fluid: Fluid,
    dimension: ResourceLocation?,
    biome: ResourceLocation?,
  ): FluidFiniteMode {
    val values = getRestrictionsFor(fluid)
      .asSequence()
      .filter(inIncludedDimension(dimension))
      .filter(notInExcludedDimension(dimension))
      .filter(inIncludedBiome(biome))
      .filter(notInExcludedBiome(biome))
      .map { it.finiteMode }
      .toList()

    if (values.contains(FluidFiniteMode.FINITE)) {
      return FluidFiniteMode.FINITE
    } else if (values.contains(FluidFiniteMode.INFINITE)) {
      return FluidFiniteMode.INFINITE
    }

    return FluidFiniteMode.DEFAULT
  }

  // First cache layer: all finite/infinite restrictions for the fluid
  private fun getRestrictionsFor(fluid: Fluid): List<FluidRestriction> {
    return fluidRestrictionsCache.computeIfAbsent(fluid) { populateFluidRestrictions(it) }
  }

  private val fluidRestrictionsCache: MutableMap<Fluid, List<FluidRestriction>> = HashMap()

  private fun populateFluidRestrictions(fluid: Fluid): List<FluidRestriction> {
    val isTargetingFluid = createPredicateFor(fluid)
    return registry.entries()
      .filter { isTargetingFluid(it.target) }
      .filter { it.finiteMode !== FluidFiniteMode.DEFAULT }
      .toList()
  }

  companion object {
    internal val RestrictionRegistry = RestrictionsRegistry<FluidRestriction>()

    private val allRestrictionFields = FluidRestriction::class.java.declaredFields

    internal val INSTANCE = FluidRestrictions(RestrictionRegistry, allRestrictionFields)

    fun add(name: ResourceLocation, restriction: FluidRestriction) {
      RestrictionRegistry.add(name, restriction)
    }

    fun getFluid(fluidState: FluidState): Fluid {
      return fluidState.type
    }

    fun getFluid(fluid: ResourceLocation?): Fluid {
      return Registry.FLUID[fluid]
    }

    fun isEmptyFluid(fluid: Fluid?): Boolean {
      return fluid == null || fluid.isSame(Fluids.EMPTY)
    }

    fun isEmptyFluid(fluidState: FluidState): Boolean {
      return isEmptyFluid(fluidState.type)
    }

    fun isFluidBlock(block: Block?): Boolean {
      return block is LiquidBlock
    }

    fun isFluidBlock(block: BlockState): Boolean {
      return isFluidBlock(block.block) || !isEmptyFluid(block.fluidState)
    }

    fun isReplacedFluid(a: Fluid?, b: Fluid): Boolean {
      return !isEmptyFluid(a) && !b.isSame(a)
    }

    fun getFluidName(fluid: Fluid?): ResourceLocation {
      return Registry.FLUID.getKey(fluid)
    }

    fun getFluidName(fluidState: FluidState): ResourceLocation {
      return getFluidName(getFluid(fluidState))
    }

    fun getReplacementFor(player: Player, original: Fluid, pos: BlockPos?): Fluid {
      if (isEmptyFluid(original)) {
        return original
      }

      val level = player.getLevel()
      val dimension = level.dimension().location()
      val biome = level.getBiome(pos).unwrapKey().orElseThrow().location()

      val replacement = INSTANCE.getReplacementFor(player, original, dimension, biome)

      if (isReplacedFluid(original, replacement)) {
        PlayerSkillsLogger.FLUIDS.debug("Replacing fluid ${getFluidName(original)} in ${dimension.path}/${biome.path} with ${getFluidName(replacement)}")
      }

      return replacement
    }

    fun getReplacementBlock(player: Player, originalBlock: BlockState, pos: BlockPos?): BlockState {
      val fluidState = originalBlock.fluidState
      val original = fluidState.type
      val replacement = getReplacementFor(player, original, pos)

      // We have a replacement fluid
      if (isReplacedFluid(original, replacement)) {
        PlayerSkillsLogger.FLUIDS.debug(
          "Replacing block fluid of ${getFluidName(original)} in ${player.level.dimension().location()}/${player.level.getBiome(pos).unwrapKey().orElseThrow().location()} with ${getFluidName(replacement)}",
        )

        if (isEmptyFluid(replacement)) {
          return Blocks.AIR.defaultBlockState()
        }

        val replacedBlock = Registry.BLOCK[getFluidName(replacement)]
          .defaultBlockState()
        if (!fluidState.isSource) {
          replacedBlock.setValue(LiquidBlock.LEVEL, originalBlock.getValue(LiquidBlock.LEVEL))
        }

        return replacedBlock
      }

      return originalBlock
    }

    fun replaceFluidStateForEntity(entity: Entity, level: Level, blockPos: BlockPos?): FluidState {
      val nearestPlayer = entity as? Player
        ?: // Use the nearest player within spawning distance to apply their restrictions
        level.getNearestPlayer(entity, entity.type.category.despawnDistance.toDouble())

      if (nearestPlayer != null) {
        val blockState = level.getBlockState(blockPos)
        val original = blockState.fluidState.type
        val replacement = getReplacementBlock(nearestPlayer, blockState, blockPos).fluidState
        if (isReplacedFluid(original, replacement.type)) {
          PlayerSkillsLogger.FLUIDS.debug(
            "Replacing fluid ${getFluidName(original)} in ${level.dimension().location().path}/${level.getBiome(blockPos).unwrapKey().orElseThrow().location().path} for entity with ${getFluidName(replacement)}",
          )

          return replacement
        }
      }

      return level.getFluidState(blockPos)
    }

    fun canBucket(player: Player, fluid: Fluid, pos: BlockPos?): Boolean {
      val actual = getReplacementFor(player, fluid, pos)
      return INSTANCE.isBucketable(player, actual, pos)
    }

    private const val MAX_DISTANCE = 10.0

    fun canBucket(fluid: Fluid, levelAccessor: LevelAccessor, blockPos: BlockPos): Boolean {
      val player = levelAccessor.getNearestPlayer(blockPos.x.toDouble(), blockPos.y.toDouble(), blockPos.z.toDouble(), MAX_DISTANCE, EntitySelector.NO_SPECTATORS)

      // We are assuming that the non-spectator player closed to the block being picked up is the one doing the action.
      // If enough mods extend the player's reach past 10 blocks, this will fail here.
      if (player == null) {
        PlayerSkillsLogger.FLUIDS.warn("Could not find a player within $MAX_DISTANCE blocks of fluid ${getFluidName(fluid)} being picked up")
        return DEFAULT_CAN_RESPONSE
      }

      return canBucket(player, fluid, blockPos)
    }

    fun getFiniteModeFor(fluid: Fluid?, dimension: ResourceLocation?, biome: ResourceLocation?): FluidFiniteMode {
      val result = INSTANCE.getFiniteModeFor(fluid!!, dimension, biome)
      PlayerSkillsLogger.FLUIDS.debug(
        "How finite is fluid ${getFluidName(fluid)} in $biome/$dimension ? $result",
      )

      return result
    }
  }
}
