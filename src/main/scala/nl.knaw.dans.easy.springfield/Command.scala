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

import nl.knaw.dans.lib.error._
import nl.knaw.dans.lib.logging.DebugEnhancedLogging

import scala.io.StdIn
import scala.util.{ Failure, Success, Try }
import scala.xml.PrettyPrinter

object Command extends App
  with DebugEnhancedLogging
  with EasySpringfieldApp
  with Smithers2
  with ListUsers
  with GetStatus
  with CreateAddActions {

  import scala.language.reflectiveCalls

  type FeedBackMessage = String

  val opts = CommandLineOptions(args, properties)
  opts.verify()

  val result: Try[FeedBackMessage] = opts.subcommand match {
    case Some(cmd @ opts.status) =>
      val TABS = "%-35s %-40s %-10s\n"
      val maybeList =
        if (cmd.user.toOption.isDefined) {
          getStatusSummaries(cmd.domain(), cmd.user())
            .map(_.map(s => TABS format(s.user, s.filename, s.status.toUpperCase)).mkString)
        }
        else {
          getUserList(cmd.domain())
            .map {
              _.map {
                user =>
                  getStatusSummaries(cmd.domain(), user)
                    .map(_.map(s => TABS format(s.user, s.filename, s.status.toUpperCase)).mkString)
                    .recover { case _ => TABS format(user, "*** COULD NOT RETRIEVE DATA ***", "") }.get
              }.mkString
            }
        }

      maybeList.map {
        list =>
          "\n" +
            (TABS format("USER", "FILE", "STATUS")) +
            (TABS format("=" * "USER".length, "=" * "FILE".length, "=" * "STATUS".length)) +
            list
      }
    case Some(cmd @ opts.listUsers) =>
      debug("Calling list-users")
      getUserList(cmd.domain()).map(_.mkString(", "))
    case Some(cmd @ opts.delete) =>
      for {
        list <- if (cmd.withReferencedItems()) getReferencedPaths(cmd.path()).map(_ :+ cmd.path())
                else Success(Seq(cmd.path()))
        _ <- approveDeletion(list)
        _ <- list.map(deletePath).collectResults
      } yield "Items deleted"
    case Some(cmd @ opts.createAddActions) =>
      val result = for {
        videos <- parseCsv(cmd.videosCsv())
        _ <- if (cmd.skipSourceExistsCheck()) Success(())
             else checkSourceVideosExist(videos, cmd.srcFolder())
        // If check parent items -> doe controle
        actions <- createAddActions(videos)
      } yield new PrettyPrinter(160, 2).format(actions)
      result.map { s =>
        println(s)
        "XML generated." + (if (cmd.skipSourceExistsCheck()) " (Existence of files NOT checked!)"
                            else "")
      }
    case Some(cmd @ opts.createUser) =>
      createUser(cmd.user(), cmd.targetDomain()).map(_ => s"User created: ${cmd.user()}")
    case Some(cmd @ opts.createCollection) =>
      createCollection(cmd.collection(), cmd.title(), cmd.description(), cmd.user(), cmd.targetDomain()).map(_ => s"Collection created: ${cmd.collection()}")
    case _ => throw new IllegalArgumentException(s"Unknown command: ${ opts.subcommand }")
      Try { "Unknown command" }
  }

  result.map(msg => Console.err.println(s"OK: $msg"))
    .onError(e => Console.err.println(s"FAILED: ${ e.getMessage }"))

  private def getUserList(domain: String): Try[Seq[String]] = {
    for {
      xml <- getXmlFromPath(Paths.get("domain", domain, "user"))
      users <- Try { listUsers(xml) }
    } yield users
  }

  private def getStatusSummaries(domain: String, user: String): Try[Seq[VideoStatusSummary]] = {
    for {
      xml <- getXmlFromPath(Paths.get("domain", domain, "user", user, "video"))
      summaries <- Try { getStatus(user, xml) }
      _ = debug(s"Retrieved status summaries: $summaries")
    } yield summaries
  }

  private def approveDeletion(list: Seq[Path]): Try[Seq[Path]] = {
    println("The following items will be deleted:")
    list.foreach(println)
    print("OK? (y/n): ")
    if (StdIn.readLine().toLowerCase == "y") Success(list)
    else Failure(new Exception("User aborted delete"))
  }
}
