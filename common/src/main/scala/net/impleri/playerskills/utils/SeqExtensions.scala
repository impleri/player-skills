package net.impleri.playerskills.utils

import scala.language.implicitConversions

object SeqExtensions {
  implicit class EnhancedDiff[T](val as: Seq[T]) {
    // We use b.forall instead of checking if a contains all of b so that we short-circuit immediately on a false
    def containsList(b: Seq[T]): Boolean =
      b.forall(as.contains)

    def containsListWith[U](b: Seq[T], f: T => Option[U]): Boolean =
      as.flatMap(f)
        .containsList(b.flatMap(f))

    // Returns entries in A that are not in B using f
    def diffWith[U](bs: Seq[T], f: T => Option[U]): Seq[T] =
      as.filterNot(f(_).exists(bs.flatMap(f).contains))
  }

  implicit class EveryFn[T](val as: Seq[T]) {
    def every(f: T => Boolean): Boolean = as.nonEmpty && as.forall(f)
  }
}
