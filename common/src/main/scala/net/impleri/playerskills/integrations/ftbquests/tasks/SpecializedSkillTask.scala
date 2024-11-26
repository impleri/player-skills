package net.impleri.playerskills.integrations.ftbquests.tasks

import dev.ftb.mods.ftbquests.quest.Quest
import dev.ftb.mods.ftbquests.quest.task.TaskType
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.server.api.Player
import net.impleri.playerskills.server.PlayerSkillsServer
import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.integrations.ftbquests.quests.QuestState
import net.impleri.playerskills.integrations.ftbquests.quests.QuestStateOps
import net.impleri.playerskills.integrations.ftbquests.quests.StringQuest
import net.impleri.playerskills.skills.specialized.SpecializedSkillType

case class SpecializedSkillTask(
  q: Quest,
  override val playerOps: Player,
  override val skillOps: SkillOps,
) extends SkillTask[String](q, playerOps, skillOps)
    with StringQuest {
  data = QuestState(SpecializedSkillType.NAME)

  override def getType: TaskType = SpecializedSkillTask.TASK_TYPE
}

object SpecializedSkillTask {
  final val TASK_TYPE: TaskType = QuestStateOps
    .createTaskType(
      QuestStateOps.SPECIALIZED_SKILL,
      "minecraft:item/diamond_shovel",
      apply,
    )

  def apply(quest: Quest): SpecializedSkillTask =
    new SpecializedSkillTask(
      quest,
      PlayerSkillsServer.STATE.PLAYER_OPS,
      PlayerSkills.STATE.SKILL_OPS,
    )
}
