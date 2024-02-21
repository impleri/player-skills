package net.impleri.playerskills

package object skills {
  type ErrorOr[T] = Either[Throwable, T]
}
