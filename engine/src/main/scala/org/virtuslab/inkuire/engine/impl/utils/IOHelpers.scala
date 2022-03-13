package org.virtuslab.inkuire.engine.impl.utils

object IOHelpers {
  val ANSI_RESET = "\u001B[0m";
  val ANSI_RED   = "\u001B[31m";
  val ANSI_BLUE  = "\u001B[34m";
  val ANSI_GREEN = "\u001B[32m";
  def red(str: String):   String = ANSI_RED + str + ANSI_RESET
  def blue(str: String):  String = ANSI_BLUE + str + ANSI_RESET
  def green(str: String): String = ANSI_GREEN + str + ANSI_RESET
}
