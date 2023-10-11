package net.impleri.playerskills.utils

import cats.data.State

trait StatefulRegistry[S] {
  protected var state: S
  /**
   * Helper method to run a state operation and update the internal state before returning the op response
   */
  protected def maintainState[T](op: State[S, T]): T = op.run(state).map(r => {
    state = r._1
    r._2
  }).value

  def getState: S = state
}
