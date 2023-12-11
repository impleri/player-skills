package net.impleri.playerskills.server.integrations.ftbquests

import dev.ftb.mods.ftbquests.quest.reward.RewardType
import dev.ftb.mods.ftbquests.quest.task.TaskType
import net.impleri.playerskills.server.integrations.ftbquests.rewards.BasicSkillReward
import net.impleri.playerskills.server.integrations.ftbquests.rewards.NumericSkillReward
import net.impleri.playerskills.server.integrations.ftbquests.rewards.SpecializedSkillReward
import net.impleri.playerskills.server.integrations.ftbquests.rewards.TieredSkillReward
import net.impleri.playerskills.server.integrations.ftbquests.tasks.BasicSkillTask
import net.impleri.playerskills.server.integrations.ftbquests.tasks.NumericSkillTask
import net.impleri.playerskills.server.integrations.ftbquests.tasks.SpecializedSkillTask
import net.impleri.playerskills.server.integrations.ftbquests.tasks.TieredSkillTask

class FtbQuestsIntegration(
  BASIC_TASK: TaskType,
  NUMERIC_TASK: TaskType,
  TIERED_TASK: TaskType,
  SPECIALIZED_TASK: TaskType,
  BASIC_REWARD: RewardType,
  NUMERIC_REWARD: RewardType,
  TIERED_REWARD: RewardType,
  SPECIALIZED_REWARD: RewardType,
)

object FtbQuestsIntegration {
  val STATE: FtbQuestsIntegration = init()

  private def init(): FtbQuestsIntegration = {
    new FtbQuestsIntegration(
      BasicSkillTask.TASK_TYPE,
      NumericSkillTask.TASK_TYPE,
      TieredSkillTask.TASK_TYPE,
      SpecializedSkillTask.TASK_TYPE,
      BasicSkillReward.REWARD_TYPE,
      NumericSkillReward.REWARD_TYPE,
      TieredSkillReward.REWARD_TYPE,
      SpecializedSkillReward.REWARD_TYPE,
    )
  }

  def apply(): FtbQuestsIntegration = {
    STATE
  }
}
