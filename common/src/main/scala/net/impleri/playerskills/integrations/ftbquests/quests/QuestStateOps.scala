package net.impleri.playerskills.integrations.ftbquests.quests

import dev.ftb.mods.ftblibrary.config.ConfigGroup
import dev.ftb.mods.ftblibrary.config.ConfigValue
import dev.ftb.mods.ftblibrary.config.NameMap
import dev.ftb.mods.ftblibrary.icon.Icon
import dev.ftb.mods.ftbquests.quest.reward.RewardType
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes
import dev.ftb.mods.ftbquests.quest.Quest
import dev.ftb.mods.ftbquests.quest.reward.Reward
import dev.ftb.mods.ftbquests.quest.task.Task
import dev.ftb.mods.ftbquests.quest.task.TaskType
import dev.ftb.mods.ftbquests.quest.task.TaskTypes
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.server.api.{Player => PlayerOps}
import net.impleri.playerskills.PlayerSkills
import net.impleri.slab.chat.Message
import net.impleri.slab.chat.StaticText
import net.impleri.slab.chat.TranslatableText
import net.impleri.slab.logging.Logger
import net.impleri.slab.nbt.NbtContents
import net.impleri.slab.network.FriendlyBuffer
import net.impleri.slab.resources.ResourceLocation

import scala.jdk.CollectionConverters._
import scala.util.chaining.scalaUtilChainingOps

trait QuestStateOps[T] {
  protected var data: QuestState[T] = QuestState(
    ResourceLocation(PlayerSkills.MOD_ID, "none").get,
  )

  protected def noneValue: T

  protected def skillOps: SkillOps

  protected def playerOps: PlayerOps

  protected def logger: Logger

  protected def upsert(next: QuestState[T]): Unit = {
    data = next
  }

  // NBT Handling

  protected def writeSkillTag(nbt: NbtContents): NbtContents =
    nbt.putString(QuestStateOps.SKILL_TAG_KEY, data.skillAsString)

  protected def readSkillTag(nbt: NbtContents): Unit =
    nbt
      .getResourceLocation(QuestStateOps.SKILL_TAG_KEY)
      .pipe(s => data.copy(skill = s))
      .pipe(upsert)

  // Implemented by type-specific traits
  protected def writeValueToTag(
    nbt: NbtContents,
    key: String,
    value: Option[T],
  ): NbtContents

  protected def writeValueTag(nbt: NbtContents): NbtContents =
    writeValueToTag(nbt, QuestStateOps.VALUE_TAG_KEY, data.value)

  // Implemented by type-specific traits
  protected def readValueFromTag(nbt: NbtContents, key: String): Option[T]

  protected def readValueTag(nbt: NbtContents): Unit =
    readValueFromTag(nbt, QuestStateOps.VALUE_TAG_KEY)
      .pipe(v => data.copy(value = v))
      .pipe(upsert)

  // Buffer handling

  protected def writeSkillBuffer(buffer: FriendlyBuffer): FriendlyBuffer =
    buffer.writeString(data.skillAsString)

  protected def readSkillBuffer(buffer: FriendlyBuffer): Unit =
    buffer
      .readResourceLocation()
      .pipe(s => data.copy(skill = s))
      .pipe(upsert)

  // Implemented by type-specific traits
  protected def writeValueToBuffer(
    buffer: FriendlyBuffer,
    value: Option[T],
  ): FriendlyBuffer

  protected def writeValueBuffer(buffer: FriendlyBuffer): FriendlyBuffer =
    writeValueToBuffer(buffer, data.value)

  // Implemented by type-specific traits
  protected def readValueFromBuffer(buffer: FriendlyBuffer): Option[T]

  protected def readValueBuffer(buffer: FriendlyBuffer): Unit =
    readValueFromBuffer(buffer)
      .pipe(v => data.copy(value = v))
      .pipe(upsert)

  // Config handling

  protected def addSkillToConfig(config: ConfigGroup): Unit = {
    val skills = skillOps
      .all()
      .filter(_.skillType == data.skillType)
      .map(_.name)

    val firstOption = skills.headOption.fold("")(_.toString)

    config
      .addEnum(
        QuestStateOps.SKILL_TAG_KEY,
        data.skillAsString,
        (s: String) =>
          Option(s)
            .filter(_.nonEmpty)
            .flatMap(ResourceLocation(_))
            .pipe(v => data.copy(skill = v))
            .pipe(upsert),
        NameMap
          .of(firstOption, skills.map(_.toString).asJava)
          .create(),
        firstOption,
      )
      .setNameKey(QuestStateOps.uiKey(QuestStateOps.SKILL_TAG_KEY))
  }

  // Implemented by type-specific traits
  protected def addValueConfig(
    config: ConfigGroup,
    key: String,
    value: T,
    options: NameMap[T],
    defaultValue: T,
  ): ConfigValue[_]

  private def addValueOptionsConfig(
    config: ConfigGroup,
    key: String,
    value: T,
    options: NameMap[T],
    defaultValue: T,
  ): ConfigValue[_] = {
    config.addEnum(
      key,
      value,
      (v: T) => data.copy(value = Option(v)).pipe(upsert),
      options,
      defaultValue,
    )
  }

  protected def addValueToConfig(config: ConfigGroup): Unit = {
    val actualSkill = data.skill.flatMap(skillOps.get[T])
    val options = actualSkill.toList.flatMap(_.options)

    val allOptions = List(noneValue) ++ options

    val optionsMap = NameMap
      .of(
        options.headOption.getOrElse(noneValue),
        allOptions.asJava,
      )
      .create()

    val value = Option(data.value)
      .filter(v => actualSkill.exists(_.isAllowedValue(v)))
      .flatten
      .orElse(options.headOption)
      .getOrElse(noneValue)

    val callback: QuestStateOps.VALUE_CALLBACK[T] =
      if (options.isEmpty) addValueConfig else addValueOptionsConfig

    callback(
      config,
      QuestStateOps.VALUE_TAG_KEY,
      value,
      optionsMap,
      noneValue,
    ).setNameKey(QuestStateOps.uiKey(QuestStateOps.VALUE_TAG_KEY))
  }

  protected def getSkillTitle: Message[_] = {
    TranslatableText(QuestStateOps.uiKey(QuestStateOps.SKILL_TAG_KEY))
      .append(": ")
      .append(StaticText(data.skillAsString).yellow())
  }
}

object QuestStateOps {
  private final val SKILL_TAG_KEY = "skill"

  final val BASIC_SKILL: String = "basic_skill"
  final val NUMERIC_SKILL: String = "numeric_skill"
  final val SPECIALIZED_SKILL: String = "specialized_skill"
  final val TIERED_SKILL: String = "tiered_skill"

  private type VALUE_CALLBACK[T] =
    (ConfigGroup, String, T, NameMap[T], T) => ConfigValue[_]
  private final val VALUE_TAG_KEY = "value"

  private def localeKey(name: String): String = s"playerskills.quests.$name"

  def uiKey(name: String): String = localeKey(s"ui.$name")

  def rewardKey(name: String): String = localeKey(s"reward.$name")

  def createRewardType(
    name: String,
    icon: String,
    f: Quest => Reward,
  ): RewardType = RewardTypes
    .register(
      ResourceLocation(s"${name}_reward").get.value,
      q => f(q),
      () => Icon.getIcon(icon),
    )
    .setDisplayName(TranslatableText(QuestStateOps.localeKey(name)).output)

  def createTaskType(name: String, icon: String, f: Quest => Task): TaskType =
    TaskTypes
      .register(
        ResourceLocation(s"${name}_task").get.value,
        q => f(q),
        () => Icon.getIcon(icon),
      )
      .setDisplayName(TranslatableText(QuestStateOps.localeKey(name)).output)
}
