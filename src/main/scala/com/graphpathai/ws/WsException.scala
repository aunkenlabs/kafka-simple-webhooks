package com.graphpathai.ws

import play.api.libs.ws.StandaloneWSResponse

case class WsException(response: StandaloneWSResponse) extends RuntimeException(
  s"${response.status} ${response.statusText}, body: '${response.body}'")
