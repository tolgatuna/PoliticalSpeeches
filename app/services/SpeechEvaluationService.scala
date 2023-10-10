package services

import com.google.inject.Singleton
import models.{EvaluationResult, Speech}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

/**
 * This service is responsible from evaluation method of speeches.
 */
@Singleton
class SpeechEvaluationService @Inject()(val fetchingService: CsvRetrievalService)(implicit ec: ExecutionContext) {

  private val TargetResearchTopic: String = "Innere Sicherheit"
  private val TargetResearchYear: Int = 2013

  def evaluate(urls: List[String]): Future[EvaluationResult] = {
    val listOfFutureForEachUrl = urls.map(fetchingService.fetchSpeechesFromUrl)
    Future.sequence(listOfFutureForEachUrl).map { speechListForAllUrls =>
      val speeches = speechListForAllUrls.flatten
      EvaluationResult(
        findMostActivePoliticianInYear(speeches).orNull,
        findMostActivePoliticianInTopic(speeches).orNull,
        findLeastVerbosePolitician(speeches).orNull
      )
    }
  }

  private def findMostActivePoliticianInYear(speeches: List[Speech]): Option[String] = {
    val speakerAndSpeechCountMap = speeches.filter(_.dateOfSpeech.getYear == TargetResearchYear)
      .groupBy(_.speaker)
      .view
      .mapValues(_.size)
      .toList

    findSpeakerWithMaxSpeechCount(speakerAndSpeechCountMap)
  }

  private def findMostActivePoliticianInTopic(speeches: List[Speech]): Option[String] = {
    val speakerAndSpeechCountMap = speeches.filter(_.title == TargetResearchTopic)
      .groupBy(_.speaker)
      .view
      .mapValues(_.size)
      .toList

    findSpeakerWithMaxSpeechCount(speakerAndSpeechCountMap)
  }

  private def findLeastVerbosePolitician(speeches: List[Speech]): Option[String] = {
    val speakerAndWordCountMap = speeches
      .groupBy(_.speaker)
      .view
      .mapValues(_.map(_.wordCount).sum)
      .toList

    speakerAndWordCountMap match {
      case Nil => None
      case _ =>
        val minSpeakerAndWordCount = speakerAndWordCountMap.minBy(_._2)
        /*
         * It is a control for if multiple people have the same minimum word count.
         * If there is more than one person with minimum word count which means there is an ambiguity and will return None
         */
        if (speakerAndWordCountMap.count(_._2 == minSpeakerAndWordCount._2) > 1) None
        else Some(minSpeakerAndWordCount._1)
    }
  }

  private def findSpeakerWithMaxSpeechCount(speakerAndSpeechCountList: List[(String, Int)]): Option[String] = {
    speakerAndSpeechCountList match {
      case Nil => None
      case _ =>
        /*
         * Check if multiple people have the same maximum speech size.
         * If there is more than one person with maximum speech size which means there is an ambiguity and will return None
         */
        val maxSpeakerAndSpeechCount = speakerAndSpeechCountList.maxBy(_._2)
        if (speakerAndSpeechCountList.count(_._2 == maxSpeakerAndSpeechCount._2) > 1) None
        else Some(maxSpeakerAndSpeechCount._1)
    }
  }
}