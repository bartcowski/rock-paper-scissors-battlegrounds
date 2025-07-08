package com.github.bartcowski.rps_battlegrounds.infra

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.bartcowski.rps_battlegrounds.app.GameStateBroadcaster
import com.github.bartcowski.rps_battlegrounds.model.GameState
import org.springframework.stereotype.Component
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap

@Component
class EchoWebSocketHandler : TextWebSocketHandler(), GameStateBroadcaster {

    private val gameSessions: MutableMap<String, MutableList<WebSocketSession>> = ConcurrentHashMap()
    private val objectMapper = jacksonObjectMapper().registerKotlinModule()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val uri = session.uri?.toString()
        val gameId = uri!!.substringAfterLast("/")
        gameSessions.computeIfAbsent(gameId) { mutableListOf() }.add(session)
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        println("Received: ${message.payload}")
        session.sendMessage(TextMessage("Echo: ${message.payload}"))
    }

    override fun broadcast(gameId: String, gameState: GameState) {
        val json = objectMapper.writeValueAsString(gameState)
        //TODO: if game activated too quickly, socket connection is probably not yet established and there is nothing in gameSessions map, NPE is thrown and the whole coroutine loop is broken
        // --> after changing it from "!!" to "?" it seems ok, the loop is already running, it will start the game as soon as the socket session is added to gameSessions
        gameSessions[gameId]?.forEach { session ->
            if (session.isOpen) {
                session.sendMessage(TextMessage(json))
            } else {
                //TODO: better handling needed, at least close remaining sessions for this game ID (also no throwing here either cuz loop stops)
                throw RuntimeException("some sessions are not open for game $gameId")
            }
        }
    }
}
