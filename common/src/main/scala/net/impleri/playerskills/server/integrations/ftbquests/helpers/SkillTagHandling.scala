package net.impleri.playerskills.server.integrations.ftbquests.helpers

import dev.ftb.mods.ftblibrary.config.ConfigGroup
import dev.ftb.mods.ftblibrary.config.NameMap
import net.fabricmc.api.Environment
import net.fabricmc.api.EnvType
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.utils.SkillResourceLocation
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.ChatFormatting

import scala.jdk.javaapi.CollectionConverters

trait SkillTagHandling {
  protected def skillOps: SkillOps

  protected def skillType: ResourceLocation

  private var skill: Option[ResourceLocation] = None

  protected def getSkill: Option[ResourceLocation] = skill

  private def setSkill(next: Option[ResourceLocation]): Unit = {
    skill = next
  }

  private def getSkillText: String = skill.fold("")(_.toString)

  protected def writeSkillTag(nbt: CompoundTag): Unit = {
    nbt.putString("skill", getSkillText)
  }

  protected def readSkillTag(nbt: CompoundTag): Unit = {
    setSkill(SkillResourceLocation.of(nbt.getString("skill")))
  }

  protected def writeSkillBuffer(buffer: FriendlyByteBuf): Unit = {
    buffer.writeUtf(getSkillText, Int.MaxValue)
  }

  protected def readSkillBuffer(buffer: FriendlyByteBuf): Unit = {
    setSkill(SkillResourceLocation.of(buffer.readUtf(Int.MaxValue)))
  }

  private def getSkills(skillType: ResourceLocation): List[ResourceLocation] = {
    skillOps.all()
      .filter(_.skillType == skillType)
      .map(_.name)
  }

  protected def getActualSkill[T]: Option[Skill[T]] = {
    skill.flatMap(skillOps.get)
  }

  protected def getActualOptions[T]: List[T] = {
    getActualSkill[T].toList.flatMap(_.options)
  }

  protected def getOptionsEnum[T](noneValue: T): NameMap[T] = {
    val options: List[T] = List(noneValue) ++ getActualOptions

    NameMap.of(
      options.headOption.getOrElse(noneValue),
      CollectionConverters.asJava(options),
    ).create()
  }


  @Environment(EnvType.CLIENT)
  protected def addSkillToConfig(config: ConfigGroup): Unit = {
    val skills = getSkills(skillType)
    val firstSkill = skills.headOption

    if (skill.isEmpty) {
      skill = firstSkill
    }

    config.addEnum(
      "skill",
      getSkillText,
      (s: String) => setSkill(if (s.isEmpty) None else SkillResourceLocation.of(s)),
      NameMap.of(firstSkill.fold("")(_.toString), CollectionConverters.asJava(skills.map(_.toString))).create(),
      firstSkill.fold("")(_.toString),
    ).setNameKey("playerskills.quests.ui.skill")
  }

  @Environment(EnvType.CLIENT)
  protected def getSkillTitle: MutableComponent = {
    Component.translatable("playerskills.quests.ui.skill")
      .append(": ")
      .append(
        Component.literal(getSkillText)
          .withStyle(ChatFormatting.YELLOW),
      )
  }
}
