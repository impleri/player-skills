package net.impleri.playerskills.integrations.kubejs.api

import dev.latvian.mods.rhino.util.HideFromJS
import dev.latvian.mods.rhino.util.RemapForJS
import net.impleri.playerskills.restrictions.AbstractRestriction
import net.impleri.playerskills.restrictions.RestrictionConditionsBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.entity.player.Player
import java.util.function.Predicate

abstract class AbstractRestrictionConditionsBuilder<Target, Restriction : AbstractRestriction<Target>>(
  @HideFromJS override val server: Lazy<MinecraftServer>,
) : RestrictionConditionsBuilder<Target, PlayerDataJS, Restriction> {
  @HideFromJS
  override val name: ResourceLocation? = null

  @HideFromJS
  override var target: String? = null

  @HideFromJS
  override var rawCondition = Predicate { _: PlayerDataJS -> true }

  override val actualCondition: (Player) -> Boolean
    @HideFromJS
    get() = {
      rawCondition.test(PlayerDataJS(it))
    }

  @HideFromJS
  override val includeBiomes: MutableList<ResourceLocation> = ArrayList()

  @HideFromJS
  override val excludeBiomes: MutableList<ResourceLocation> = ArrayList()

  @HideFromJS
  override val includeDimensions: MutableList<ResourceLocation> = ArrayList()

  @HideFromJS
  override val excludeDimensions: MutableList<ResourceLocation> = ArrayList()

  @RemapForJS("if")
  override fun condition(predicate: Predicate<PlayerDataJS>): AbstractRestrictionConditionsBuilder<Target, Restriction> {
    super.condition(predicate)

    return this
  }

  override fun unless(predicate: Predicate<PlayerDataJS>): AbstractRestrictionConditionsBuilder<Target, Restriction> {
    super.condition(predicate)

    return this
  }
}
