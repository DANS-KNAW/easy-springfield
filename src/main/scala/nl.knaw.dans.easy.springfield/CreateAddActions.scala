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

import java.nio.file.{Files, Path, Paths}

import org.apache.commons.csv.{CSVFormat, CSVParser, CSVRecord}

import scala.collection.JavaConverters._
import scala.io.Source
import scala.util.{Failure, Success, Try}
import scala.xml.Elem

case class Video(srcVideo: Path, targetDomain: String, targetUser: String, targetCollection: String, targetPresentation: String, targetFileName: String, requireTicket: Boolean = true)

trait CreateAddActions {
  def parseCsv(file: Path): Try[Seq[Video]] = Try {
    val rawContent = Source.fromFile(file.toFile).mkString
    val format = CSVFormat.RFC4180
      .withRecordSeparator(',')
      .withHeader("SRC", "DOMAIN", "USER", "COLLECTION", "PRESENTATION", "FILE", "REQUIRE-TICKET")
      .withSkipHeaderRecord(true)
    val parser = CSVParser.parse(rawContent, format)
    parser.getRecords.asScala
      .map((row: CSVRecord) =>
        Video(
          srcVideo = Paths.get(row.get("SRC")),
          targetDomain = row.get("DOMAIN"),
          targetUser = row.get("USER"),
          targetCollection = row.get("COLLECTION"),
          targetPresentation = row.get("PRESENTATION"),
          targetFileName = row.get("FILE"),
          requireTicket = row.get("REQUIRE-TICKET").toBoolean))
    // TODO: validate input
  }

  def checkSourceVideosExist(videos: Seq[Video], srcFolder: Path): Try[Unit] = {
    val incorrect = videos.map(_.srcVideo).filterNot(v => !v.isAbsolute && Files.isRegularFile(srcFolder.resolve(v)))
    if (incorrect.isEmpty) Success(())
    else Failure(new Exception(s"Error in following items: [${incorrect.mkString(", ")}]. Possible errors: file not found (or a directory), path is absolute. " +
      s"Paths resolved against source folder: $srcFolder"))
  }

  def createAddActions(videos: Seq[Video]): Try[Elem] = Try {
    <actions>
      { createAddPresentations(videos) }
    </actions>
  }

  def createAddUser(name: String, targetDomain: String): Elem = {
    <add target={s"domain/$targetDomain"}>
      <user name={name}/>
    </add>
  }

  def createAddCollection(name: String, title: String, description: String, targetDomain: String, targetUser: String): Elem = {
    // @formatter:off
    <add target={s"domain/$targetDomain/user/$targetUser"}>
      <collection name={name}>
        <title>{title}</title>
        <description>{description}</description>
      </collection>
    </add>
    // @formatter:on
  }

  def createAddPresentations(videos: Seq[Video]): Seq[Elem] = {
    videos.groupBy(_.targetPresentation).values.map(createAddPresentation).toSeq
  }

  /**
   * Creates one add element for a presentation
   *
   * @param videos the videos in this presentation
   */
  def createAddPresentation(videos: Seq[Video]): Elem = {
    <add target={s"domain/${ videos.head.targetDomain }/user/${ videos.head.targetUser }/collection/${ videos.head.targetCollection }"}>
      <presentation name={videos.head.targetPresentation}>
        <video-playlist require-ticket={videos.head.requireTicket.toString}>
          {videos.sortBy(_.targetFileName).map(v => createAddVideo(v.srcVideo, v.targetFileName))}
        </video-playlist>
      </presentation>
    </add>
  }

  def createAddVideo(srcVideo: Path, fileName: String): Elem = {
      <video src={srcVideo.toString} target={fileName}/>
  }
}
