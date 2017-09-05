package com.grandsys.inu.storedq.models

import com.grandsys.inu.storedq.commands.AddClause

object AddClauseExtensions {
  implicit class covertToEdge(cmd :AddClause) {
    import cmd._
    import AddClause.Clause.Reference

    def getEdge(): Option[DirectedEdge] = {
      cmd.clause match {
        case Reference(ReferencedClause(id, _)) => Some(DirectedEdge(start = storedQueryId, end = id))
        case _ => None
      }
    }
  }
}
