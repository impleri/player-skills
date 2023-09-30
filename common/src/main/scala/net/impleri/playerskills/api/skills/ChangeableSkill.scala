package net.impleri.playerskills.api.skills

trait ChangeableSkill[T] extends SkillData[T] {
  val options: List[T] = List()

  val changesAllowed: Int = ChangeableSkill.UNLIMITED_CHANGES

  def areChangesAllowed(): Boolean = changesAllowed != 0

  def isAllowedValue(nextValue: Option[T]): Boolean = options.isEmpty || nextValue.map(options.contains(_)).getOrElse(nextValue.isEmpty)
}

trait ChangeableSkillOps[T, S <: ChangeableSkill[T]] extends ChangeableSkill[T] {
  def mutate(newValue: Option[T] = None): S = mutate(newValue, changesAllowed - 1)

  protected[playerskills] def mutate(value: Option[T], changesAllowed: Int): S
}

object ChangeableSkill {
  val UNLIMITED_CHANGES: Int = -1
}
