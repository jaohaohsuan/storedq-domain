Feature: TopologicalSort
  In order to avoid making mistakes
  As a dummy
  I want create a cycle graph

  Scenario: sort the non cycle graph
    Given "a" use "b", "c" and "d"
    Given "e" use "c" and "a"
    When sort
    Then I get "a,b,c,d,e"

  Scenario: cycle graph
    Given "a" use "b" then "b" use "a"
    Then I get cycle graph
