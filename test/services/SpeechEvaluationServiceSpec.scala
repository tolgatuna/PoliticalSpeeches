package services

import models.{EvaluationResult, Speech}
import org.joda.time.DateTime
import org.mockito.MockitoSugar._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * SpeechEvaluationServiceSpec is responsible from testing of the SpeechEvaluationService and contains these tests:
 *
 * - evaluation result on speeches for a single URL
 * - evaluation result based on speeches from multiple URLs
 * - null for most speeches in 2013 if there is no one speaking in 2013
 * - null for most speeches for topic `Innere Sicherheit` if there is no one speaking about it
 * - null for each field if there is more than one speaker exist in same counts
 *
 * PS! -> It is mocking outside url and downloading csv files...
 */
class SpeechEvaluationServiceSpec extends AnyWordSpec with Matchers with ScalaFutures {

  // Create a mock instance of the external service
  val mockCsvFetchingService: CsvRetrievalService = mock[CsvRetrievalService]

  // Create an instance of SpeechEvaluationService with the mock external service
  val speechEvaluationService: SpeechEvaluationService = new SpeechEvaluationService(mockCsvFetchingService)(global)

  "SpeechEvaluationService" should {
    "return evaluation result on speeches for a single URL" in {

      // Speaker 1 - politician gave the most speeches in 2013
      // Speaker 3 - politician gave the most speeches on the topic "Innere Sicherheit"
      // Speaker 4 - politician used the fewest words?
      val speechesFromUrl = List(
        Speech("Speaker1", "Other Topic", DateTime.parse("2013-08-30"), 100),
        Speech("Speaker2", "Any Other Topic", DateTime.parse("2014-11-30"), 300),
        Speech("Speaker3", "Innere Sicherheit", DateTime.parse("2013-02-22"), 50),
        Speech("Speaker4", "Some Other Topic", DateTime.parse("2013-01-30"), 25),
        Speech("Speaker1", "Something Interesting", DateTime.parse("2013-10-30"), 500)
      )

      when(mockCsvFetchingService.fetchSpeechesFromUrl("url")).thenReturn(Future.successful(speechesFromUrl))

      val expectedResult = EvaluationResult("Speaker1", "Speaker3", "Speaker4")

      val resultFuture = speechEvaluationService.evaluate(List("url"))
      whenReady(resultFuture) { result =>
        result shouldBe expectedResult
      }
    }

    "return evaluation result based on speeches from multiple URLs" in {
      // Speaker 1 - politician gave the most speeches in 2013
      // Speaker 3 - politician gave the most speeches on the topic "Innere Sicherheit"
      // Speaker 4 - politician used the fewest words?
      val speechesFromUrl1 = List(
        Speech("Speaker1", "Other Topic", DateTime.parse("2013-08-30"), 100),
        Speech("Speaker2", "Any Other Topic", DateTime.parse("2014-11-30"), 300),
        Speech("Speaker3", "Innere Sicherheit", DateTime.parse("2013-02-22"), 50)
      )

      val speechesFromUrl2 = List(
        Speech("Speaker4", "Some Other Topic", DateTime.parse("2013-01-30"), 25),
        Speech("Speaker1", "Something Interesting", DateTime.parse("2013-10-30"), 500)
      )

      when(mockCsvFetchingService.fetchSpeechesFromUrl("url1")).thenReturn(Future.successful(speechesFromUrl1))
      when(mockCsvFetchingService.fetchSpeechesFromUrl("url2")).thenReturn(Future.successful(speechesFromUrl2))

      val expectedResult = EvaluationResult("Speaker1", "Speaker3", "Speaker4")

      val resultFuture = speechEvaluationService.evaluate(List("url1", "url2"))
      whenReady(resultFuture) { result =>
        result shouldBe expectedResult
      }
    }

    "return null for most speeches in 2013 if there is no one speaking in 2013" in {

      // NO One!   - politician gave the most speeches in 2013
      // Speaker 3 - politician gave the most speeches on the topic "Innere Sicherheit"
      // Speaker 4 - politician used the fewest words?
      val speechesFromUrl = List(
        Speech("Speaker1", "Other Topic", DateTime.parse("2014-08-30"), 100),
        Speech("Speaker2", "Any Other Topic", DateTime.parse("2014-11-30"), 300),
        Speech("Speaker3", "Innere Sicherheit", DateTime.parse("2015-02-22"), 50),
        Speech("Speaker4", "Some Other Topic", DateTime.parse("2016-01-30"), 25),
        Speech("Speaker1", "Something Interesting", DateTime.parse("2016-10-30"), 500)
      )

      when(mockCsvFetchingService.fetchSpeechesFromUrl("url")).thenReturn(Future.successful(speechesFromUrl))

      val expectedResult = EvaluationResult(null, "Speaker3", "Speaker4")

      val resultFuture = speechEvaluationService.evaluate(List("url"))
      whenReady(resultFuture) { result =>
        result shouldBe expectedResult
      }
    }

    "return null for most speeches for topic `Innere Sicherheit` if there is no one speaking about it" in {

      // No One!   - politician gave the most speeches in 2013
      // NO One!   - politician gave the most speeches on the topic "Innere Sicherheit"
      // Speaker 4 - politician used the fewest words?
      val speechesFromUrl = List(
        Speech("Speaker1", "Other Topic", DateTime.parse("2014-08-30"), 100),
        Speech("Speaker2", "Any Other Topic", DateTime.parse("2014-11-30"), 300),
        Speech("Speaker3", "Something else", DateTime.parse("2015-02-22"), 50),
        Speech("Speaker4", "Some Other Topic", DateTime.parse("2016-01-30"), 25),
        Speech("Speaker1", "Something Interesting", DateTime.parse("2016-10-30"), 500)
      )

      when(mockCsvFetchingService.fetchSpeechesFromUrl("url")).thenReturn(Future.successful(speechesFromUrl))

      val expectedResult = EvaluationResult(null, null, "Speaker4")

      val resultFuture = speechEvaluationService.evaluate(List("url"))
      whenReady(resultFuture) { result =>
        result shouldBe expectedResult
      }
    }

    "return null for each field if there is more than one speaker exist in same counts" in {

      // Speaker 1 & Speaker 2 -> both have 1 speech for 2013 - So there is an ambiguity and should return null
      // Speaker 5 & Speaker 6 -> both have 1 speech about `Innere Sicherheit` - So there is an ambiguity and should return null
      // Speaker 3 & Speaker 4 -> both have 25 words - So there is an ambiguity and should return null
      val speechesFromUrl = List(
        Speech("Speaker1", "Other Topic", DateTime.parse("2013-08-30"), 100),
        Speech("Speaker2", "Any Other Topic", DateTime.parse("2013-11-30"), 300),
        Speech("Speaker3", "Something else", DateTime.parse("2015-02-22"), 25),
        Speech("Speaker4", "Some Other Topic", DateTime.parse("2016-01-30"), 25),
        Speech("Speaker5", "Innere Sicherheit", DateTime.parse("2016-10-30"), 500),
        Speech("Speaker6", "Innere Sicherheit", DateTime.parse("2016-10-30"), 500)
      )

      when(mockCsvFetchingService.fetchSpeechesFromUrl("url")).thenReturn(Future.successful(speechesFromUrl))

      val expectedResult = EvaluationResult(null, null, null)

      val resultFuture = speechEvaluationService.evaluate(List("url"))
      whenReady(resultFuture) { result =>
        result shouldBe expectedResult
      }
    }
  }
}
