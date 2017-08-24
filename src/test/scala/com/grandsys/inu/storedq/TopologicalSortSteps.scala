package com.grandsys.inu.storedq

import com.grandsys.inu.storedq.models.DAGTopologicalResult
import com.grandsys.inu.storedq.models.DAGTopologicalResult.Edge
import cucumber.api.scala.{EN, ScalaDsl}
import org.scalatest.Matchers
import scala.util.{Failure, Success, Try}

class TopologicalSortSteps extends ScalaDsl with EN with Matchers {
  var s0 = DAGTopologicalResult(incoming_edges_map = Map.empty)
  var s1 = DAGTopologicalResult(incoming_edges_map = Map.empty)
  var s1_result: Try[Iterable[String]] = null

  Given("""^"([^"]*)" use "([^"]*)" then "([^"]*)" use "([^"]*)"$"""){ (arg0:String, arg1:String, arg2:String, arg3:String) =>
    s0 = s0.add(Edge(arg0, arg1))
    s0 = s0.add(Edge(arg2, arg3))
  }
  Then("""^I get cycle graph$"""){ () =>
    Try(DAGTopologicalResult.sort(s0.incoming_edges_map, Seq())) match {
      case Failure(e) => println(e)
      case _ => fail("error should be occurred")
    }
    s0.hasCycle should be(true)
  }

  Given("""^"([^"]*)" use "([^"]*)", "([^"]*)" and "([^"]*)"$"""){ (arg0:String, arg1:String, arg2:String, arg3:String) =>
    s1 = s1.add(Edge(arg0, arg1))
    s1 = s1.add(Edge(arg0, arg2))
    s1 = s1.add(Edge(arg0, arg3))
  }

  Given("""^"([^"]*)" use "([^"]*)" and "([^"]*)"$"""){ (arg0:String, arg1:String, arg2:String) =>
    s1 = s1.add(Edge(arg0, arg1))
    s1 = s1.add(Edge(arg0, arg2))
  }
  When("""^sort$"""){ () =>
    s1_result = Try(DAGTopologicalResult.sort(s1.incoming_edges_map, Seq()))
  }

  Then("""^I get "([^"]*)"$"""){ (arg0:String) =>
    s1_result match {
      case Success(xs) => arg0.split(",") should contain theSameElementsAs(xs)
      case Failure(e) => fail(e)
    }
  }
}
