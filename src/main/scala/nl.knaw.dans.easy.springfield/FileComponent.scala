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

import java.nio.file.Path

import org.apache.commons.io.FileUtils

import scala.util.Try

trait FileComponent {

  def moveSubtitlesToDir(relativeDestination: Path, subtitles: Path, adjustedFileName: String, springFieldBaseDir: Path): Try[Unit] = Try {
    val resolvedDestination = springFieldBaseDir.resolve(relativeDestination) //TODO make configurable?
    println(s"copying sub titles '${ subtitles.getFileName }' to destination '${ resolvedDestination.resolve(adjustedFileName) }'")
    FileUtils.copyFile(subtitles.toFile, resolvedDestination.resolve(adjustedFileName).toFile)
  }

  def createLanguageAdjustedFileName(subTitlesPath: Path, language: String): String = {
    s"${ language }_${ subTitlesPath.getFileName }" // sub.vtt => nl_sub.vtt)
  }
}
