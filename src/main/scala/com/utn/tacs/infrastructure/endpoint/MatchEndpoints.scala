package com.utn.tacs.infrastructure.endpoint

import cats.effect.Sync
import cats.implicits._
import com.utn.tacs.domain.`match`.{Match, MatchService}
import com.utn.tacs.infrastructure.repository.MatchNotFoundError
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes}


object MatchEndpoints {
  implicit def decoder[F[_] : Sync]: EntityDecoder[F, Match] = jsonOf[F, Match]

  def matchRoutes[F[_] : Sync](matchServiceImpl: MatchService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / matchId =>
        matchServiceImpl.getMatch(matchId)
          .map(m => Ok(m.asJson))
          .getOrElse(NotFound(s"match:${matchId} not found"))
      case req@POST -> Root =>
        for {
          payload <- req.as[Map[String, String]]
          newMatch <- matchServiceImpl.createMatch(payload("player1_id"), payload("player2_id"), payload("deck_id"))
          resp <- Created(newMatch.asJson)
        } yield resp
      case req@PUT -> Root / matchId / "withdraw" =>
        for {
          payload <- req.as[Map[String, String]]
          loserPlayer = payload("loser_player_id")
          withDrawResult = matchServiceImpl.withdraw(matchId, loserPlayer)
          resp <- withDrawResult match {
            case Left(MatchNotFoundError) => NotFound()
            case Right(m) => Accepted(m.asJson)
          }
        } yield resp
      case GET -> Root / matchId / "replay" =>
        for {
          resp <- Ok(Json.obj(
            ("plays", List(
              Json.obj(("id", Json.fromString("1"))),
              Json.obj(("id", Json.fromString("2"))),
              Json.obj(("id", Json.fromString("3")))
            ).asJson)
          ))
        } yield resp
    }
  }
}
