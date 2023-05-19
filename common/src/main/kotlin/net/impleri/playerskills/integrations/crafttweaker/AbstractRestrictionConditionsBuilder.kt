package net.impleri.playerskills.integrations.crafttweaker

import com.blamejared.crafttweaker.api.annotation.ZenRegister
import net.impleri.playerskills.restrictions.AbstractRestriction
import net.impleri.playerskills.restrictions.RestrictionConditionsBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.entity.player.Player
import org.openzen.zencode.java.ZenCodeType

@ZenRegister
@ZenCodeType.Name("mods.playerskills.restrictions.AbstractRestrictionBuilder")
abstract class AbstractRestrictionConditionsBuilder<Target, Restriction : AbstractRestriction<Target>>(
  val id: ResourceLocation,
  override val server: MinecraftServer,
) : RestrictionConditionsBuilder<Target, Player, Restriction> {
  override var rawCondition = { _: Player -> true }
  override val includeBiomes: MutableList<ResourceLocation> = ArrayList()
  override val excludeBiomes: MutableList<ResourceLocation> = ArrayList()
  override val includeDimensions: MutableList<ResourceLocation> = ArrayList()
  override val excludeDimensions: MutableList<ResourceLocation> = ArrayList()

  val condition: (Player) -> Boolean
    get() = rawCondition

  @ZenCodeType.Method
  override fun condition(predicate: (Player) -> Boolean): AbstractRestrictionConditionsBuilder<Target, Restriction> {
    @Suppress("UNCHECKED_CAST")
    return super.condition(predicate) as AbstractRestrictionConditionsBuilder<Target, Restriction>
  }

  @ZenCodeType.Method
  override fun unless(predicate: (Player) -> Boolean): AbstractRestrictionConditionsBuilder<Target, Restriction> {
    @Suppress("UNCHECKED_CAST")
    return super.unless(predicate) as AbstractRestrictionConditionsBuilder<Target, Restriction>
  }

  @ZenCodeType.Method
  override fun inDimension(dimension: String): AbstractRestrictionConditionsBuilder<Target, Restriction> {
    return super.inDimension(dimension) as AbstractRestrictionConditionsBuilder<Target, Restriction>
  }

  @ZenCodeType.Method
  override fun notInDimension(dimension: String): AbstractRestrictionConditionsBuilder<Target, Restriction> {
    return super.notInDimension(dimension) as AbstractRestrictionConditionsBuilder<Target, Restriction>
  }

  @ZenCodeType.Method
  override fun inBiome(biome: String): AbstractRestrictionConditionsBuilder<Target, Restriction> {
    return super.inBiome(biome) as AbstractRestrictionConditionsBuilder<Target, Restriction>
  }

  @ZenCodeType.Method
  override fun notInBiome(biome: String): AbstractRestrictionConditionsBuilder<Target, Restriction> {
    return super.notInBiome(biome) as AbstractRestrictionConditionsBuilder<Target, Restriction>
  }
}
