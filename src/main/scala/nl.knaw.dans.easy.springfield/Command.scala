/**
 * Copyright (C) 2017 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
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

import scala.util.{Success, Try}
import scala.xml.Elem

object Command extends App  {
  import scala.language.reflectiveCalls
  type FeedBackMessage = String
  val properties = new PropertiesConfiguration(Paths.get(System.getProperty("app.home")).resolve("cfg/application.properties").toFile)

  val opts = CommandLineOptions(args, properties)
  opts.verify()

  val result: Try[FeedBackMessage] = opts.subcommand match {
    case Some(cmd @ opts.status) => Success("")
    case Some(cmd @ opts.ls) => Success("")
    case Some(cmd @ opts.rm) => Success("")
    case _ => throw new IllegalArgumentException(s"Unknown command: ${opts.subcommand}")
      Try { "Unknown command" }
  }

  result.map(msg => println(s"OK: $msg"))
    .onError(e => println(s"FAILED: ${e.getMessage}"))


  def ls(path: Path): Try[String] = {



    ???
  }

  def getUsers(parent: Elem): Seq[String] = {

    ???
  }

  def getCollections(parent: Elem): Seq[String] = {


    ???
  }

}
