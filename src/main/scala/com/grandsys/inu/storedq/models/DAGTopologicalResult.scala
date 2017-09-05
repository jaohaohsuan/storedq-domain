package com.grandsys.inu.storedq.models

import DAGTopologicalResult.{Vertex, IncomingEdges}
import scala.annotation.tailrec
import scala.util.Try

object DAGTopologicalResult {
  type IncomingEdges = Set[String]
  type Vertex = String

  def withAnyEdges(kv : (Vertex, IncomingEdges)) = { kv._2.isEmpty }

  @tailrec
  final def sort(vertex_map: Map[Vertex, IncomingEdges], result: Iterable[Vertex]): Iterable[Vertex] = {

    if (result.nonEmpty)
      println(s"Legal vertices (${result.mkString(",")}) found\n")

    if (vertex_map.isEmpty)
      return result

    val (no_incoming_edge_vertices, with_incoming_edge_vertices) = vertex_map.partition(withAnyEdges)

    val with_incoming_edge_vertices_info = s"${with_incoming_edge_vertices.map{ case (vertex, edges) => s"$vertex's incoming edges (${edges.mkString(",")})" } mkString "\n  "}"

    if (no_incoming_edge_vertices.isEmpty && with_incoming_edge_vertices.nonEmpty)
      sys.error(s"cycle graph\n  $with_incoming_edge_vertices_info")

    if (no_incoming_edge_vertices.nonEmpty)
      println(s"\nNo incoming edges vertices (${no_incoming_edge_vertices.keys.mkString(",")})")

    if (with_incoming_edge_vertices.nonEmpty)
      println(s"With incoming edges vertices\n  $with_incoming_edge_vertices_info")

    // use no incoming edges vertices to eliminate vertices with incoming edges
    // then add no incoming edges vertices to result
    sort(with_incoming_edge_vertices.mapValues(_ -- no_incoming_edge_vertices.keys), result ++ no_incoming_edge_vertices.keys)
  }

  /*
  def collectPaths(currentDot: String,
                      result: Set[Set[Edge]] = Set.empty[Set[Edge]],
                      currentPath: Set[Edge] = Set.empty[Edge])
                     (implicit vertices: Map[Vertex, Set[Vertex]])
  : Set[Set[Edge]] = {
    vertices get currentDot match {
      case None => result + currentPath
      case Some(set) if set.isEmpty => result + currentPath
      case Some(set) =>
        set.foldLeft(result) { (acc, e) => { collectPaths(e, acc, currentPath + Edge(currentDot,e)) } }
    }
  }
  */

}


case class DAGTopologicalResult(incoming_edges_map: Map[Vertex, IncomingEdges]) {

  lazy val hasCycle = Try(DAGTopologicalResult.sort(incoming_edges_map, Seq())).isFailure

  def add(edge: com.grandsys.inu.algorithm.DAG.Edge) = {

    // consumer permutation
    val p1: (String, Set[String]) = edge.start -> incoming_edges_map.getOrElse(edge.start, Set.empty)

    // provider permutation
    val p2: (String, Set[String]) = edge.end -> (incoming_edges_map.getOrElse(edge.end, Set.empty) + edge.start)

    copy(incoming_edges_map = incoming_edges_map + p1 + p2)
  }

  def remove(consumer: String, provider: String) = {

    // provider permutation
    val p = provider -> (incoming_edges_map.getOrElse(provider, Set.empty) - consumer)

    copy( incoming_edges_map = incoming_edges_map + p)
  }

}


