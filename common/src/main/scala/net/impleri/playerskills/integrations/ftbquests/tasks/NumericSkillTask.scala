package net.impleri.playerskills.integrations.ftbquests.tasks

import dev.ftb.mods.ftblibrary.icon.Icon
import dev.ftb.mods.ftbquests.quest.Quest
import dev.ftb.mods.ftbquests.quest.task.TaskType
import dev.ftb.mods.ftbquests.quest.task.TaskTypes
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.server.api.Player
import net.impleri.playerskills.server.PlayerSkillsServer
import net.impleri.playerskills.utils.SkillResourceLocation
import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.integrations.ftbquests.helpers.DoubleValueHandling
import net.impleri.playerskills.skills.numeric.NumericSkillType
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

case class NumericSkillTask(
  q: Quest,
  override val playerOps: Player,
  override val skillOps: SkillOps,
  override val skillTypeOps: SkillTypeOps,
) extends SkillTask[Double](q, playerOps, skillOps) with DoubleValueHandling {
  override val skillType: ResourceLocation = NumericSkillType.NAME

  override def getType: TaskType = NumericSkillTask.TASK_TYPE

  override def getMaxProgress: Long = value.fold(0L)(_.toLong)
}

object NumericSkillTask {
  val TASK_TYPE: TaskType = TaskTypes.register(
    SkillResourceLocation.of("numeric_skill_task").get,
    apply,
    () => Icon.getIcon("minecraft:item/iron_shovel"),
  )

  TASK_TYPE.setDisplayName(Component.translatable("playerskills.quests.numeric_skill"))

  def apply(quest: Quest): NumericSkillTask = {
    new NumericSkillTask(
      quest,
      PlayerSkillsServer.STATE.PLAYER_OPS,
      PlayerSkills.STATE.getSkillOps,
      PlayerSkills.STATE.getSkillTypeOps,
    )
  }
}
