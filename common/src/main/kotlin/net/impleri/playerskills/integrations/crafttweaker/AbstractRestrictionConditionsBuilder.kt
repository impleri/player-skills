package net.impleri.playerskills.integrations.crafttweaker

import com.blamejared.crafttweaker.api.annotation.ZenRegister
import net.impleri.playerskills.restrictions.AbstractRestriction
import net.impleri.playerskills.restrictions.RestrictionConditionsBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.entity.player.Player
import org.openzen.zencode.java.ZenCodeType
import java.util.function.Predicate

@ZenRegister
@ZenCodeType.Name("mods.playerskills.restrictions.AbstractRestrictionConditionsBuilder")
abstract class AbstractRestrictionConditionsBuilder<Target, Restriction : AbstractRestriction<Target>>(
  override val server: MinecraftServer,
) : RestrictionConditionsBuilder<Target, Player, Restriction> {
  override val includeBiomes: MutableList<ResourceLocation> = ArrayList()
  override val excludeBiomes: MutableList<ResourceLocation> = ArrayList()
  override val includeDimensions: MutableList<ResourceLocation> = ArrayList()
  override val excludeDimensions: MutableList<ResourceLocation> = ArrayList()
  override var rawCondition: Predicate<Player> = Predicate { _ -> true }
  override val actualCondition: (Player) -> Boolean
    get() = { rawCondition.test(it) }

  @ZenCodeType.Method
  override fun condition(predicate: Predicate<Player>): AbstractRestrictionConditionsBuilder<Target, Restriction> {
    super.condition(predicate)

    return this
  }

  @ZenCodeType.Method
  override fun unless(predicate: Predicate<Player>): AbstractRestrictionConditionsBuilder<Target, Restriction> {
    super.unless(predicate)

    return this
  }

  @ZenCodeType.Method
  override fun inDimension(dimension: String): AbstractRestrictionConditionsBuilder<Target, Restriction> {
    super.inDimension(dimension)

    return this
  }

  @ZenCodeType.Method
  override fun notInDimension(dimension: String): AbstractRestrictionConditionsBuilder<Target, Restriction> {
    super.notInDimension(dimension)

    return this
  }

  @ZenCodeType.Method
  override fun inBiome(biome: String): AbstractRestrictionConditionsBuilder<Target, Restriction> {
    super.inBiome(biome)

    return this
  }

  @ZenCodeType.Method
  override fun notInBiome(biome: String): AbstractRestrictionConditionsBuilder<Target, Restriction> {
    super.notInBiome(biome)

    return this
  }
}
