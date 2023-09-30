package net.impleri.playerskills.api.skills

import net.impleri.playerskills.utils.SkillResourceLocation
import net.minecraft.resources.ResourceLocation

trait SkillData[T] {
  val name: ResourceLocation = SkillResourceLocation.of("empty").get
  val skillType: ResourceLocation = SkillResourceLocation.of("empty").get
  val value: Option[T] = None
  val description: Option[String] = None
  val teamMode: TeamMode = TeamMode.Off()
}
