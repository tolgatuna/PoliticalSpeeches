package controllers

import play.api.libs.json.Json
import play.api.mvc._
import services.SpeechEvaluationService

import javax.inject._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * This controller handles the requests for evaluation of speeches.
 */
@Singleton
class SpeechEvaluationController @Inject()(val controllerComponents: ControllerComponents, val speechEvaluationService: SpeechEvaluationService) extends BaseController {

  def evaluate(url: List[String]): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    if (url.isEmpty) {
      Future(BadRequest("Url not exist! Please provide a url."))
    } else {
      speechEvaluationService.evaluate(url).map { data =>
        Ok(Json.toJson(data))
      }.recover {
        case e: Exception =>
          InternalServerError(s"Exception occurred: $e")
      }
    }
  }
}
