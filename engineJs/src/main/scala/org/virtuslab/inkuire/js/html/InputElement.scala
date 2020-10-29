package org.virtuslab.inkuire.js.html

import org.scalajs.dom._
import org.scalajs.dom.experimental._

class InputElement(input: html.Input) {
  def registerOnInputChange(func: (String) => Unit): Unit = {
    // TODO: Better API would be great
    input.oninput = (event: Event) => func(event.target.asInstanceOf[html.Input].value)
  }
}

object InputElement {
  def apply(): InputElement = {
    // TODO: ID should be obtained from some config
    val parent = document.getElementById("main")
    val input  = document.createElement("input")
    parent.appendChild(input)
    new InputElement(input.asInstanceOf[html.Input])
  }
}
