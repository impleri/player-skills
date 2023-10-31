package net.impleri.playerskills.api.skills

import org.jetbrains.annotations.VisibleForTesting

import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps

sealed trait TeamMode {
  def getLimit[T](skill: Skill[T], count: Int = 0): Int = count
}

object TeamMode {
  case class Off() extends TeamMode

  case class Shared() extends TeamMode

  case class SplitEvenly() extends TeamMode {
    override def getLimit[T](skill: Skill[T], count: Int): Int = {
      math.max(skill.options.size, 1)
        .pipe(count.toDouble / _)
        .pipe(_.ceil.toInt)
    }
  }

  case class Pyramid() extends TeamMode {
    @VisibleForTesting
    private[api] def getOptionLimits(optionsCount: Int): Map[Int, Int] = {
      Range.inclusive(1, optionsCount)
        .map(idx => (idx, optionsCount - idx))
        .toMap
        .view
        .mapValues(scala.math.pow(2, _))
        .mapValues(_.toInt)
        .toMap
    }

    @VisibleForTesting
    private[api] def getOptionLimit(skill: Skill[_])(index: Int): Option[Int] = {
      getOptionLimits(skill.options.size)
        .pipe(ls => Try(ls.apply(index)))
        .toOption
    }

    override def getLimit[T](skill: Skill[T], count: Int): Int = {
      skill.value
        .map(skill.options.indexOf)
        .flatMap {
          case -1 => Option(0)
          case 0 => Option(count)
          case i => getOptionLimit(skill)(i)
        }
        .getOrElse(0)
    }
  }

  case class Proportional(percentage: Double) extends TeamMode {
    override def getLimit[T](skill: Skill[T], count: Int): Int = {
      (percentage / 100)
        .pipe(_ * count)
        .pipe(_.ceil.toInt)
    }
  }

  case class Limited(amount: Int) extends TeamMode {
    override def getLimit[T](skill: Skill[T], count: Int): Int = amount
  }
}
