package controllers

import models.EvaluationResult
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{mock, never, times, verify, when}
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test._
import services.SpeechEvaluationService

import scala.concurrent.Future

/**
 * SpeechEvaluationControllerSpec is responsible from testing of the SpeechEvaluationController and contains these tests:
 *
 * - BadRequest if there is no url
 * - correct result
 *
 * PS! -> It is mocking the service
 */
class SpeechEvaluationControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  // Create a mock instance of the external service
  val mockSpeechService: SpeechEvaluationService = mock[SpeechEvaluationService]

  "SpeechEvaluationController GET evaluation" should {

    "return BadRequest if there is no url" in {
      val controller = new SpeechEvaluationController(stubControllerComponents(), mockSpeechService)
      val response = controller.evaluate(Nil).apply(FakeRequest(GET, "/evaluatiuon"))

      status(response) mustBe BAD_REQUEST
      verify(mockSpeechService, never).evaluate(any())
    }

    "return correct result" in {
      val controller = new SpeechEvaluationController(stubControllerComponents(), mockSpeechService)
      val urls = List("url1", "url2")

      val result = EvaluationResult("Tolga Tuna", "Speaker Y", "Speaker X")
      when(mockSpeechService.evaluate(urls)).thenReturn(Future.successful(result))

      val response = controller.evaluate(urls).apply(FakeRequest(GET, "/evaluatiuon?url=url1&url=url2"))
      status(response) mustBe OK
      contentAsJson(response) mustBe Json.toJson(result)
      verify(mockSpeechService, times(1)).evaluate(any())

    }
  }
}
