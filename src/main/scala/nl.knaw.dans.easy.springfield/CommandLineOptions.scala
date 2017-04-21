/**
 * Copyright (C) 2017 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.easy.springfield

import java.nio.file.{Path, Paths}

import org.apache.commons.configuration.PropertiesConfiguration
import org.rogach.scallop.{ScallopConf, ScallopOption, Subcommand, singleArgConverter}

class CommandLineOptions(args: Array[String], properties: PropertiesConfiguration) extends ScallopConf(args) {
  appendDefaultToDescription = true
  editBuilder(_.setHelpWidth(110))

  printedName = "easy-springfield"
  private val _________ = " " * printedName.length
  private val SUBCOMMAND_SEPARATOR = "---\n"
  version(s"$printedName v${Version()}")
  banner(s"""
            |Manage Springfield Web TV
            |
            |Usage:
            |
            |$printedName [-u, --user <user>] status
            |$printedName ls <path>
            |$printedName rm <path>
            |$printedName add ...
            |
            |Options:
            |""".stripMargin)


  private implicit val fileConverter = singleArgConverter[Path](s => Paths.get(resolveTildeToHomeDir(s)))
  private def resolveTildeToHomeDir(s: String): String = if (s.startsWith("~")) s.replaceFirst("~", System.getProperty("user.home")) else s

  val status = new Subcommand("status") {
    descr("Retrieve the status of content offered for ingestion into Springfield")
    val user: ScallopOption[String] = opt[String](name = "user",
      descr = "limit to videos owned by this user")
    footer(SUBCOMMAND_SEPARATOR)
  }
  addSubcommand(status)
  val listUsers = new Subcommand("list-users") {
    descr("List users in a given domain")
    val domain: ScallopOption[String] = trailArg[String](name = "domain",
      descr = "the domain of which to list the users",
      default = Some("dans"))
    footer(SUBCOMMAND_SEPARATOR)
  }
  addSubcommand(listUsers)
  val rm = new Subcommand("rm") {
    descr("Output Springfield actions to remove the item at <path>. Typically you would redirect the output to a text file with the extension .xml," +
      "which would then be placed in the Springfield inbox to effectuate the removal.")
    footer(SUBCOMMAND_SEPARATOR)
  }
  addSubcommand(rm)
  footer("")
}

object CommandLineOptions {
  def apply(args: Array[String], properties: PropertiesConfiguration): CommandLineOptions = new CommandLineOptions(args, properties)
}
