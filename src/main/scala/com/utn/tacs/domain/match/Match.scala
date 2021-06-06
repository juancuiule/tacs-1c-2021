package com.utn.tacs.domain.`match`

import com.utn.tacs.domain.`match`.Match.MatchStep


sealed trait MatchAction

object MatchAction {
  case class Withdraw(loser: Long) extends MatchAction

  case class Battle(cardAttribute: String) extends MatchAction

  case object NoOp extends MatchAction

  case object InitMatch extends MatchAction

  case object DealCards extends MatchAction
}

sealed trait MatchState


object MatchState {
  case class BattleResult(cardsInDeck: List[Int], player1Cards: List[Int], player2Cards: List[Int], nextToPlay: Long)
    extends MatchState

  case class PreBattle(cardsInDeck: List[Int], player1Cards: List[Int], player2Cards: List[Int], player1Card: Int, player2Card: Int, nextToPlay: Long)
    extends MatchState

  case class Finished(winner: Long)
    extends MatchState

  case class Draw(cardsInDeck: List[Int], player1Cards: List[Int], player2Cards: List[Int])
    extends MatchState
}


final case class Match(
  matchId: String,
  deck: Int,
  player1: Long,
  player2: Long,
  steps: List[MatchStep]
) {
  type MatchStep = (MatchAction, MatchState)

  def hasPlayer(player: Long): Boolean = {
    player1 == player || player2 == player
  }

  def currentState: MatchState = {
    steps.last._2
  }
}

object Match {
  type MatchStep = (MatchAction, MatchState)

  def apply(
    matchId: String,
    deck: Int,
    player1: Long,
    player2: Long
  ): Match = {
    this (matchId, deck, player1, player2, List())
  }
}
