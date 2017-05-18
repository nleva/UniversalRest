package ru.sendto.rest.api;

import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.HandshakeResponse;
import javax.websocket.Session;
import javax.websocket.server.HandshakeRequest;

import lombok.Data;

public class WSBundle {
	@Data
	static class OnOpen {
		Session session;
		EndpointConfig config;
		HandshakeRequest handshakeRequest;
		HandshakeResponse handshakeResponse;
		
	}

	@Data
	static class OnMessage {
		Session session;
	}

	@Data
	static class OnClose {
		Session session;
		CloseReason closeReason;
	}

	@Data
	static class OnError {
		Session session;
		Throwable throwable;
	}
}
