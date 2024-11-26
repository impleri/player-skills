package net.impleri.playerskills.integrations.ftbquests.tasks

import dev.ftb.mods.ftbquests.quest.Quest
import dev.ftb.mods.ftbquests.quest.task.TaskType
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.server.api.{Player => PlayerOps}
import net.impleri.playerskills.server.PlayerSkillsServer
import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.integrations.ftbquests.quests.{BooleanQuest, QuestState, QuestStateOps}
import net.impleri.playerskills.skills.basic.BasicSkillType

case class BasicSkillTask(
  q: Quest,
  override val playerOps: PlayerOps,
  override val skillOps: SkillOps,
) extends SkillTask[Boolean](q, playerOps, skillOps)
    with BooleanQuest {
  data = QuestState(BasicSkillType.NAME)

  override def getType: TaskType = BasicSkillTask.TASK_TYPE
}

object BasicSkillTask {
  final val TASK_TYPE: TaskType = QuestStateOps
    .createTaskType(
      QuestStateOps.BASIC_SKILL,
      "minecraft:item/wooden_shovel",
      apply,
    )

  def apply(quest: Quest): BasicSkillTask =
    new BasicSkillTask(
      quest,
      PlayerSkillsServer.STATE.PLAYER_OPS,
      PlayerSkills.STATE.SKILL_OPS,
    )
}
