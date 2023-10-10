package models

import play.api.libs.json.{Json, OFormat}


case class EvaluationResult(mostSpeeches: String, mostSecurity: String, leastWordy: String)

object EvaluationResult {
  implicit val evaluationResultJson: OFormat[EvaluationResult] = Json.format[EvaluationResult]
}
