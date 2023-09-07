package net.impleri.playerskills.integrations.crafttweaker

import com.blamejared.crafttweaker.api.annotation.ZenRegister
import net.impleri.playerskills.integrations.crafttweaker.restrictions.ItemConditionsBuilder
import net.impleri.playerskills.restrictions.items.ItemRestrictionBuilder
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.world.item.Item
import org.openzen.zencode.java.ZenCodeType
import net.impleri.playerskills.api.ItemRestrictions as ItemRestrictionsApi

@ZenRegister
@ZenCodeType.Name("mods.playerskills.ItemRestrictions")
object ItemRestrictions {
  @ZenCodeType.Method
  @JvmStatic
  fun create(name: String): ItemConditionsBuilder {
    return ItemConditionsBuilder {
      PlayerSkillsLogger.MOBS.debug("Registering restriction for $name")
      ItemRestrictionBuilder.register(name, it)
    }
  }

  @ZenCodeType.Method
  @JvmStatic
  fun create(item: Item): ItemConditionsBuilder {
    return create(ItemRestrictionsApi.getName(item).toString())
  }
}
