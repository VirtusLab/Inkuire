package org.virtuslab.inkuire.js.html

import monix.eval.Task
import monix.reactive.Observable

trait BaseInput {
  def inputChanges: Observable[String]

  def notifyEngineReady: Task[Unit]
}
