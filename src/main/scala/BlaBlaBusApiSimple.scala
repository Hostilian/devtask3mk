package com.example

// This file intentionally left blank. All types and implementations are now in BlaBlaBusApi.scala.
// If you need a mock/test implementation, create it in a new uniquely named object and only use types from BlaBlaBusApi.scala.

object BlaBlaBusApiSimple {
  // Simple/mock API client for easy use in tests or demos
  val layer                      = BlaBlaBusApiClient.mockLayer
  def client: BlaBlaBusApiClient = new MockBlaBlaBusApiClient()
}
