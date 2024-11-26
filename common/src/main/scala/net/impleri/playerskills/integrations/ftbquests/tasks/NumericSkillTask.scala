package net.impleri.playerskills.integrations.ftbquests.tasks

import dev.ftb.mods.ftbquests.quest.Quest
import dev.ftb.mods.ftbquests.quest.task.TaskType
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.server.api.Player
import net.impleri.playerskills.server.PlayerSkillsServer
import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.integrations.ftbquests.quests.{DoubleQuest, QuestState, QuestStateOps}
import net.impleri.playerskills.skills.numeric.NumericSkillType

case class NumericSkillTask(
  q: Quest,
  override val playerOps: Player,
  override val skillOps: SkillOps,
) extends SkillTask[Double](q, playerOps, skillOps)
    with DoubleQuest {
  data = QuestState(NumericSkillType.NAME)

  override def getType: TaskType = NumericSkillTask.TASK_TYPE

  override def getMaxProgress: Long = data.value.fold(0L)(_.toLong)
}

object NumericSkillTask {
  final val TASK_TYPE: TaskType = QuestStateOps
    .createTaskType(
      QuestStateOps.NUMERIC_SKILL,
      "minecraft:item/iron_shovel",
      apply,
    )

  def apply(quest: Quest): NumericSkillTask =
    new NumericSkillTask(
      quest,
      PlayerSkillsServer.STATE.PLAYER_OPS,
      PlayerSkills.STATE.SKILL_OPS,
    )
}
