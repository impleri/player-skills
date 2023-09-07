package net.impleri.playerskills.integrations.crafttweaker

import com.blamejared.crafttweaker.api.annotation.ZenRegister
import net.impleri.playerskills.integrations.crafttweaker.restrictions.MobConditionsBuilder
import net.impleri.playerskills.restrictions.mobs.MobRestrictionBuilder
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.world.entity.EntityType
import org.openzen.zencode.java.ZenCodeType
import net.impleri.playerskills.api.MobRestrictions as MobRestrictionsApi

@ZenRegister
@ZenCodeType.Name("mods.playerskills.MobRestrictions")
object MobRestrictions {
  @ZenCodeType.Method
  @JvmStatic
  fun create(name: String): MobConditionsBuilder {
    return MobConditionsBuilder {
      PlayerSkillsLogger.MOBS.debug("Registering restriction for $name")
      MobRestrictionBuilder.register(name, it)
    }
  }

  @ZenCodeType.Method
  @JvmStatic
  fun create(entityType: EntityType<*>): MobConditionsBuilder {
    return create(MobRestrictionsApi.getName(entityType).toString())
  }
}
