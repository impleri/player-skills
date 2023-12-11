package net.impleri.playerskills.server.integrations.ftbquests.rewards

import dev.ftb.mods.ftblibrary.icon.Icon
import dev.ftb.mods.ftbquests.quest.Quest
import dev.ftb.mods.ftbquests.quest.reward.RewardType
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.server.api.{Player => PlayerOps}
import net.impleri.playerskills.server.PlayerSkillsServer
import net.impleri.playerskills.utils.SkillResourceLocation
import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.server.integrations.ftbquests.helpers.StringValueHandling
import net.impleri.playerskills.skills.tiered.TieredSkillType
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

case class TieredSkillReward(
  q: Quest,
  override val playerOps: PlayerOps,
  override val skillOps: SkillOps,
  override val skillTypeOps: SkillTypeOps,
)
  extends MinMaxSkillReward[String](q, playerOps, skillOps, skillTypeOps) with StringValueHandling {
  override val skillType: ResourceLocation = TieredSkillType.NAME

  override def getType: RewardType = TieredSkillReward.REWARD_TYPE
}

object TieredSkillReward {
  val REWARD_TYPE: RewardType = RewardTypes.register(
    SkillResourceLocation.of("tiered_skill_reward").get,
    apply,
    () => Icon.getIcon("minecraft:item/golden_hoe"),
  )

  REWARD_TYPE.setDisplayName(Component.translatable("playerskills.quests.tiered_skill"))

  def apply(quest: Quest): TieredSkillReward = {
    new TieredSkillReward(
      quest,
      PlayerSkillsServer.STATE.PLAYER_OPS,
      PlayerSkills.STATE.getSkillOps,
      PlayerSkills.STATE.getSkillTypeOps,
    )
  }
}
