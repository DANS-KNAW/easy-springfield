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

import java.nio.file.{ Path, Paths }

import org.apache.commons.configuration.PropertiesConfiguration
import org.rogach.scallop.{ ScallopConf, ScallopOption, Subcommand, singleArgConverter }

class CommandLineOptions(args: Array[String], properties: PropertiesConfiguration) extends ScallopConf(args) {
  appendDefaultToDescription = true
  editBuilder(_.setHelpWidth(110))

  printedName = "easy-springfield"
  private val _________ = " " * printedName.length
  private val SUBCOMMAND_SEPARATOR = "---\n"
  version(s"$printedName v${ Version() }")
  banner(
    s"""
       |Manage Springfield Web TV
       |
            |Usage:
       |
            |$printedName status [-d, --domain <domain>] [-u, --user <user>]
       |$printedName ls <path>
       |$printedName rm <path>
       |$printedName add ...
       |
            |Options:
       |""".stripMargin)


  private implicit val fileConverter = singleArgConverter[Path](s => Paths.get(resolveTildeToHomeDir(s)))

  private def resolveTildeToHomeDir(s: String): String =
    if (s.startsWith("~")) s.replaceFirst("~", System.getProperty("user.home"))
    else s

  val status = new Subcommand("status") {
    descr("Retrieve the status of content offered for ingestion into Springfield")
    val domain: ScallopOption[String] = opt(name = "domain",
      descr = "limit to videos within this domain",
      default = Some("dans"))
    val user: ScallopOption[String] = opt(name = "user",
      descr = "limit to videos owned by this user")

    footer(SUBCOMMAND_SEPARATOR)
  }
  addSubcommand(status)

  val listUsers = new Subcommand("list-users") {
    descr("List users in a given domain")
    val domain: ScallopOption[String] = trailArg(name = "domain",
      descr = "the domain of which to list the users",
      default = Some(properties.getString("springfield.default-domain")))
    footer(SUBCOMMAND_SEPARATOR)
  }
  addSubcommand(listUsers)

  val delete = new Subcommand("delete") {
    descr("Delete the item at the specified Springfield path")
    val path: ScallopOption[Path] = trailArg(name = "path",
      descr = "the path pointing item to remove")
    val withReferencedItems: ScallopOption[Boolean] = opt(name = "with-referenced-items", short = 'r',
      descr = "also remove items reference from <path>, recursively")
    footer(SUBCOMMAND_SEPARATOR)
  }
  addSubcommand(delete)

  val createAddActions = new Subcommand("create-add-actions") {
    descr(
      """Create Springfield Actions XML containing add-actions for A/V items specified in a CSV file
        |with lines describing videos with the following columns: SRC, DOMAIN, USER, COLLECTION, PRESENTATION, FILE,
        |REQUIRE-TICKET.
      """.stripMargin.stripLineEnd)
    val videosCsv: ScallopOption[Path] = trailArg(name = "video-csv",
      descr = "CSV file describing the videos",
      required = true)
    val srcFolder: ScallopOption[Path] = trailArg(name = "videos-folder",
      descr = "Folder relative to which to resolve the SRC column in the CSV",
      required = false, default = Some(Paths.get(".")))
    val checkParentItems: ScallopOption[Boolean] = opt(name = "check-parent-items", short = 'p',
      descr = "Check that parent items (domain, user, collection) exist")
    val skipSourceExistsCheck: ScallopOption[Boolean] = opt(name = "skip-source-exists-check", short = 's',
      descr = "Do NOT Check that the source videos exist in the expected location")
  }
  addSubcommand(createAddActions)

  val createUser = new Subcommand("create-user") {
    descr(
      """Create a new user in the Springfield database. This does NOT generate a springfield-actions XML but
        |instead creates the user in Springfield right away.
      """.stripMargin.stripLineEnd)
      val user: ScallopOption[String] = trailArg(name = "user", descr = "User name for the new user")
      val targetDomain: ScallopOption[String] = opt(name = "target-domain", short = 'd',
        descr = "The target domain in which to create the user",
        default = Some(properties.getString("springfield.default-domain")))
  }
  addSubcommand(createUser)

  val createCollection = new Subcommand("create-collection") {
    descr(
      """Create a new collection in the Springfield database. This does NOT generate a springfield-actions XML but
        |instead creates the collection in Springfield right away.
      """.stripMargin.stripLineEnd)
    val collection: ScallopOption[String] = trailArg(name = "collection", descr = "Name for the collection")
    val user: ScallopOption[String] = trailArg(name = "target-user", descr = "Existing user under which to store the collection")
    val targetDomain: ScallopOption[String] =  opt(name = "target-domain",
      descr = "The target domain in which to create the user",
      default = Some(properties.getString("springfield.default-domain")))
    val title: ScallopOption[String] = opt(name = "title", short='t',
      descr = "Title for the new collection", default = Some(""))
    val description: ScallopOption[String] = opt(name = "description", short = 'd',
      descr = "Description for the new collection", default = Some(""))
  }
  addSubcommand(createCollection)

  val setRequireTicket = new Subcommand("set-require-ticket") {
    descr("Sets or clears the 'require-ticket' flag for the specified presentation")
    val path: ScallopOption[Path] = trailArg(name = "springfield-path", descr = "The parent of items to change")
    val requireTicket: ScallopOption[String] = trailArg(name = "require-ticket", descr = "true|false")
  }
  addSubcommand(setRequireTicket)


  footer("")
}

object CommandLineOptions {
  def apply(args: Array[String], properties: PropertiesConfiguration): CommandLineOptions = new CommandLineOptions(args, properties)
}
