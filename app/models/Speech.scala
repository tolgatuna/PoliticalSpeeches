package models

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import scala.language.implicitConversions

case class Speech(speaker: String, title: String, dateOfSpeech: DateTime, wordCount: Int)

object Speech {
  private val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")

  implicit def fromStringToDate(dateString: String): DateTime = {
    formatter.parseDateTime(dateString)
  }
}