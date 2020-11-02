package org.virtuslab.inkuire.js.html

import monix.reactive.Observable

trait BaseInput {
  def inputChanges: Observable[String]
}
