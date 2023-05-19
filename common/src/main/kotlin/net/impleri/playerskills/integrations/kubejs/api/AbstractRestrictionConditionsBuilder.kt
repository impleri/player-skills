package net.impleri.playerskills.integrations.kubejs.api

import dev.latvian.mods.rhino.util.HideFromJS
import dev.latvian.mods.rhino.util.RemapForJS
import net.impleri.playerskills.restrictions.AbstractRestriction
import net.impleri.playerskills.restrictions.RestrictionConditionsBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.entity.player.Player

abstract class AbstractRestrictionConditionsBuilder<Target, Restriction : AbstractRestriction<Target>>(
  @HideFromJS override val server: MinecraftServer,
) : RestrictionConditionsBuilder<Target, PlayerDataJS, Restriction> {
  @HideFromJS
  override var rawCondition = { _: PlayerDataJS -> true }

  override val condition: (Player) -> Boolean
    @HideFromJS
    get() = {
      rawCondition(PlayerDataJS(it))
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
  override fun condition(predicate: (PlayerDataJS) -> Boolean): AbstractRestrictionConditionsBuilder<Target, Restriction> {
    @Suppress("UNCHECKED_CAST")
    return super.condition(predicate) as AbstractRestrictionConditionsBuilder<Target, Restriction>
  }

  override fun unless(predicate: (PlayerDataJS) -> Boolean): AbstractRestrictionConditionsBuilder<Target, Restriction> {
    @Suppress("UNCHECKED_CAST")
    return super.condition(predicate) as AbstractRestrictionConditionsBuilder<Target, Restriction>
  }
}
