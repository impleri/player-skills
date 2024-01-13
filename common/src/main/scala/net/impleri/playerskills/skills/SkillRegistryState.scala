package net.impleri.playerskills.skills

import cats.data.State
import cats.data.StateT
import net.impleri.playerskills.api.skills.Skill
import net.minecraft.resources.ResourceLocation

case class SkillAlreadyExistsException(skill: Skill[_])
  extends RuntimeException(s"${skill.name.toString} cannot be added again.")

object SkillRegistryState {
  /**
   * Internal state representation and low-level operations
   */
  final case class Skills private (skills: List[Skill[_]]) {
    def entries: List[Skill[_]] = skills

    def get(key: ResourceLocation): Option[Skill[_]] = skills.find(_.name == key)

    def has(key: ResourceLocation): Boolean = skills.exists(_.name == key)

    def upsert(skill: Skill[_]): Skills = Skills(remove(skill.name).skills :+ skill)

    def add(skill: Skill[_]): Either[SkillAlreadyExistsException, Skills] = {
      if (has(skill.name)) {
        Left(
          SkillAlreadyExistsException(skill),
        )
      } else {
        Right(upsert(skill))
      }
    }

    def remove(key: ResourceLocation): Skills = Skills(skills.filter(_.name != key))
  }

  private def readOp[T](f: Skills => T): State[Skills, T] = State[Skills, T](s => (s, f(s)))

  val empty: Skills = Skills(List.empty)

  def resync(source: List[Skill[_]]): State[Skills, Unit] = State.set(Skills(source))

  def upsert(skill: Skill[_]): State[Skills, Unit] = State.modify(_.upsert(skill))

  def add(skill: Skill[_]): StateT[ErrorOr, Skills, Unit] = StateT.modifyF[ErrorOr, Skills](_.add(skill))

  def remove(key: ResourceLocation): State[Skills, Unit] = State.modify(_.remove(key))

  def entries(): State[Skills, List[Skill[_]]] = State[Skills, List[Skill[_]]](s => (s, s.entries))

  def find(key: ResourceLocation): State[Skills, Option[Skill[_]]] = readOp(_.get(key))

  def has(key: ResourceLocation): State[Skills, Boolean] = readOp(_.has(key))
}
