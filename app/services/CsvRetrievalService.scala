package services

import com.google.inject.Singleton
import models.{EvaluationResult, Speech}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.io.{Codec, Source}

/**
 * This service is responsible for fetching speeches from given url.
 */
@Singleton
class CsvRetrievalService @Inject()(implicit ec: ExecutionContext) {

  import Speech.fromStringToDate
  implicit val utf8: Codec = scala.io.Codec.UTF8

  private val CsvDelimiter: String = ","

  def fetchSpeechesFromUrl(url: String): Future[List[Speech]] = {
    Future {
      val bufferedSource = Source.fromURL(url)
      try {
        bufferedSource.getLines().drop(1).map { line =>
          val values = line.split(CsvDelimiter).map(_.trim)
          Speech(values(0), values(1), values(2), values(3).toInt)
        }.toList
      } finally {
        bufferedSource.close()
      }
    }
  }
}