/*
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

import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.nio.file.{ Path, Paths }
import java.util.UUID

import nl.knaw.dans.easy.springfield.AvType._
import nl.knaw.dans.easy.springfield.Playmode.Playmode
import nl.knaw.dans.lib.error._
import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import org.apache.commons.lang.BooleanUtils
import resource.managed

import scala.io.StdIn
import scala.util.{ Failure, Success, Try }
import scala.xml.{ PrettyPrinter, XML }

object Command extends App
  with DebugEnhancedLogging
  with EasySpringfieldApp
  with Smithers2
  with AddSubtitles
  with ListUsers
  with ListCollections
  with ListFiles
  with ListPresentations
  with GetStatus
  with GetProgressOfCurrentJobs
  with CreateSpringfieldActions
  with Ticket
  with SetTitle
  with SetPlaymode {

  import scala.language.reflectiveCalls

  type FeedBackMessage = String

  private val avNames = Set("audio", "video")
  private val opts = new CommandLineOptions(args, config)
  opts.verify()

  val result: Try[FeedBackMessage] = opts.subcommand match {
    case Some(cmd @ opts.listPresentations) =>
      debug("Calling list-presentations")
      getPresentationList(cmd.user()).map(_.mkString(", "))
    case Some(cmd @ opts.listFiles) =>
      debug("Calling list-files")
      getFileList(cmd.user(), cmd.presentationId()).map(_.mkString(", "))
    case Some(cmd @ opts.listUsers) =>
      debug("Calling list-users")
      getUserList(cmd.domain()).map(_.mkString(", "))
    case Some(cmd @ opts.listCollections) =>
      getCollectionList(cmd.domain(), cmd.user()).map(_.mkString(", "))
    case Some(cmd @ opts.createUser) =>
      createUser(cmd.user(), cmd.domain()).map(_ => s"User created: ${ cmd.user() }")
    case Some(cmd @ opts.createCollection) =>
      createCollection(cmd.collection(), cmd.title(), cmd.description(), cmd.user(), cmd.domain()).map(_ => s"Collection created: ${ cmd.collection() }")
    case Some(cmd @ opts.createPresentation) =>
      createPresentation(cmd.title(), cmd.description(), cmd.requireTicket(), cmd.user(), cmd.domain()).map(referid => s"Presentation created: $referid")
    case Some(cmd @ opts.createSpringfieldActions) =>
      val result = for {
        videos <- parseCsv(cmd.videosCsv())
        _ <- if (cmd.videosFolder.isSupplied) checkSourceVideosExist(videos, cmd.videosFolder())
             else Success(())
        parentsToCreate <- if (cmd.checkParentItems())
                             getParentPaths(videos)
                               .map(checkPathExists)
                               .collectResults
                               .map(_.filterNot(_._2).map(_._1))
                           else Success(Set[Path]())
        actions <- createSpringfieldActions(videos)
      } yield (XML.loadString(new PrettyPrinter(160, 2).format(actions)), parentsToCreate)
      result.map { case (s, ps) =>
        managed(new OutputStreamWriter(Console.out)).acquireAndGet(XML.write(_, s, StandardCharsets.UTF_8.name, xmlDecl = true, null))
        "XML generated." + (if (!cmd.videosFolder.isSupplied) " (Existence of files has NOT been checked!)"
                            else "") +
          (if (cmd.checkParentItems()) {
            "\nParent items have been checked: " +
              (if (ps.isEmpty) "OK"
               else "not existing yet:\n" + ps.mkString("\n"))
          }
           else "\nParent items have been NOT been checked.")
      }
    case Some(cmd @ opts.status) =>
      val TABS = "%-35s %-40s %-7s %-10s %8s\n"
      for {
        allProgress <- getAllProgress(cmd.domain())
        formatSummary = (s: AvStatusSummary) => TABS format(s.user, s.filename, s.requireTicket, s.status.toUpperCase, getProgressOfJob(allProgress, s.jobRef, s.status))
        list <- cmd.user.toOption
          .map(user => getStatusSummaries(cmd.domain(), user).map(_.map(formatSummary).mkString))
          .getOrElse {
            getUserList(cmd.domain()).map(_.map { user =>
              getStatusSummaries(cmd.domain(), user)
                .map(_.map(formatSummary).mkString)
                .getOrRecover { _ => TABS format(user, "*** COULD NOT RETRIEVE DATA ***", "") }
            }.mkString)
          }
      } yield "\n" +
        (TABS format("USER", "A/V FILE", "PRIVATE", "STATUS", "PROGRESS")) +
        (TABS format("=" * "USER".length, "=" * "A/V FILE".length, "=" * "PRIVATE".length, "=" * "STATUS".length, "=" * "PROGRESS".length)) +
        list
    case Some(cmd @ opts.setRequireTicket) =>
      for {
        avFiles <- getReferencedPaths(cmd.path()).map(_.filter(p => p.getNameCount > 1 && avNames.contains(p.getName(p.getNameCount - 2).toString)))
        _ <- approveAction(avFiles,
          s"""
             |WARNING: THIS ACTION COULD EXPOSE AUDIO/VIDEO FILES TO UNAUTHORIZED VIEWERS/LISTENERS.
             |These audio/video files will be set to require-ticket = ${ cmd.requireTicket() }
             |
             |(Note that you may have to clear your browser cache after making audio/video files private to effectively test the result.)
           """.stripMargin)
        avFilesSetRequireTicketPath = avFiles.map(_.resolve("properties").resolve("private"))
        _ <- avFilesSetRequireTicketPath.map(setProperty(_, BooleanUtils.toBoolean(cmd.requireTicket()).toString)).collectResults
      } yield s"Video(s) set to require-ticket = ${ cmd.requireTicket() }"
    case Some(cmd @ opts.setTitle) =>
      for {
        _ <- checkPathIsRelative(cmd.presentation())
        completePath = getCompletePath(cmd.presentation())
        presentationRefId <- getPresentationReferIdPath(completePath)
        _ <- setTitle(cmd.videoNumber(), cmd.title(), presentationRefId)
      } yield "Title set"
    case Some(cmd @ opts.setPlayMode) =>
      for {
        _ <- checkPathIsRelative(cmd.path())
        completePath = getCompletePath(cmd.path())
        presentationReferId <- getPresentationReferIdPath(completePath)
        playmode <- toPlayMode(cmd.mode())
        _ <- setPlayModeForPresentation(presentationReferId, playmode)
      } yield "Play mode added or changed."

    case Some(cmd @ opts.createTicket) =>
      for {
        _ <- checkPathIsRelative(cmd.path())
        completePath = getCompletePath(cmd.path())
        ticket = cmd.ticket.toOption.getOrElse(UUID.randomUUID.toString)
        _ <- createTicket(completePath, ticket, cmd.expiresAfterSeconds())
        _ = println(ticket)
      } yield "Ticket created"
    case Some(cmd @ opts.deleteTicket) =>
      deleteTicket(cmd.ticket()).map(_ => "Ticket deleted.")
    case Some(cmd @ opts.delete) =>
      for {
        _ <- checkPathIsRelative(cmd.path())
        list <- if (cmd.withReferencedItems()) getReferencedPaths(cmd.path()).map(_ :+ getCompletePath(cmd.path()))
                else Success(Seq(cmd.path()))
        _ <- approveAction(list, """These items will be deleted.""")
        _ <- list.map(deletePath).collectResults
      } yield "Items deleted"
    case Some(cmd @ opts.`addVideoRefToPresentation`) =>
      for {
        _ <- checkPathIsRelative(cmd.video())
        _ <- addVideoRefToPresentation(getCompletePath(cmd.video()), cmd.name(), cmd.presentation())
      } yield "Video reference added."
    case Some(cmd @ opts.`addPresentationRefToCollection`) =>
      for {
        _ <- checkPathIsRelative(cmd.presentation())
        _ <- addPresentationRefToCollection(getCompletePath(cmd.presentation()), cmd.name(), cmd.collection())
      } yield "Presentation reference added."
    case Some(cmd @ opts.addSubtitlesToVideo) =>
      for {
        _ <- checkPathIsRelative(cmd.video())
        videoRefId = getCompletePath(cmd.video())
        _ <- addSubtitlesToVideo(cmd.subtitles(), videoRefId, cmd.languageCode())
      } yield "Subtitles added to video."
    case Some(cmd @ opts.addSubtitlesToPresentation) =>
      for {
        _ <- checkPathIsRelative(cmd.presentation())
        completePath = getCompletePath(cmd.presentation())
        presentationRefId <- getPresentationReferIdPath(completePath)
        videos <- getVideoIdsForPresentation(presentationRefId)
        _ <- validateNumberOfVideosInPresentationIsEqualToNumberOfSubtitles(videos, cmd.subtitles())
        videoPathsWithIds = zipVideoPathsWithIds(cmd.subtitles(), videos)
        _ <- addSubtitlesToPresentation(cmd.languageCode(), presentationRefId, videoPathsWithIds)
      } yield "Subtitles added to presentation"
    case Some(cmd @ opts.showAvailableLanguageCodes) =>
      println(config.languages.mkString("\n"))
      Success("Finished printing supported language codes.")
    case _ => Failure(new IllegalArgumentException("Enter a valid subcommand"))
  }

  private def toPlayMode(mode: String): Try[Playmode] = Try {
    Playmode.
      values
      .find(_.toString == mode)
      .getOrElse(throw new IllegalArgumentException(s"playmode `$mode` not one of ${ Playmode.values }"))
  }

  result.map(msg => Console.err.println(s"OK: $msg"))
    .doIfFailure { case e => Console.err.println(s"FAILED: ${ e.getMessage }") }

  private def checkPathIsRelative(path: Path): Try[Unit] =
    Try { require(!path.isAbsolute, "Path MUST NOT start with a slash") }

  private def getUserList(domain: String): Try[Seq[String]] = {
    for {
      xml <- getXmlFromPath(Paths.get("domain", domain, "user"))
      users <- Try { listUsers(xml) }
    } yield users
  }

  private def getCollectionList(domain: String, user: String): Try[Seq[String]] = {
    for {
      xml <- getXmlFromPath(Paths.get("domain", domain, "user", user, "collection"))
      collections <- Try { listCollections(xml) }
    } yield collections
  }

  private def getPresentationList(user: String): Try[Seq[(String,String)]] = {
    for {
      xml <- getXmlFromPath(Paths.get("user", user, "collection"))
      presentations <- Try { listPresentations(xml) }
    } yield presentations
  }

  private def getFileList(user: String, presentationId: String): Try[Seq[(Int, String,String, Boolean)]] = {
    for {
      xml <- getXmlFromPath(Paths.get("user", user))
      files <- Try { listFiles(xml, presentationId.toInt) }
    } yield files
  }

  private def getAllProgress(domain: String): Try[Map[JobRef, Progress]] = {
    for {
      videoQueue <- getXmlFromPath(Paths.get("domain", domain, "service", "momar", "queue", "high"))
      videoProgress = getProgressOfCurrentJobs(videoQueue, video)
      _ = debug(s"Video progress: $videoProgress")
      audioQueue <- getXmlFromPath(Paths.get("domain", domain, "service", "willie", "queue", "high"))
      audioProgress = getProgressOfCurrentJobs(audioQueue, audio)
      _ = debug(s"Audio progress: $audioProgress")
    } yield videoProgress ++ audioProgress
  }

  private def getProgressOfJob(allProgress: Map[JobRef, Progress], jobRef: JobRef, status: String): String = {
    if (status equalsIgnoreCase "DONE") "100%"
    else if (status equalsIgnoreCase "STILLS") "~100%"
    else allProgress.get(jobRef).map(p => s"$p%").getOrElse("n/a")
  }

  private def getStatusSummaries(domain: String, user: String): Try[Seq[AvStatusSummary]] = {
    for {
      videosXml <- getXmlFromPath(Paths.get("domain", domain, "user", user, "video"))
      videosSummary <- Try { getStatus(user, "video", videosXml) }
      audiosXml <- getXmlFromPath(Paths.get("domain", domain, "user", user, "audio"))
      audiosSummary <- Try { getStatus(user, "audio", audiosXml) }
      _ = debug(s"Retrieved status summaries, video: $videosSummary, audio $audiosSummary")
    } yield videosSummary ++ audiosSummary
  }

  private def approveAction(list: Seq[Path], msg: String): Try[Seq[Path]] = {
    println("The following items will be processed:")
    list.foreach(println)
    println(msg)
    print("OK? (y/n): ")
    if (StdIn.readLine().toLowerCase == "y") Success(list)
    else Failure(new Exception("User aborted action"))
  }
}
