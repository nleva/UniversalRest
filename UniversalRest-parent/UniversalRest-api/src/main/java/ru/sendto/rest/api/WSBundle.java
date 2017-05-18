package ru.sendto.rest.api;

import javax.websocket.CloseReason;
import javax.websocket.HandshakeResponse;
import javax.websocket.Session;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

import lombok.Data;

public class WSBundle {

	@Data
	static class HandshakeBundle {
		ServerEndpointConfig	config;
		HandshakeRequest		handshakeRequest;
		HandshakeResponse		handshakeResponse;
	}

	@Data
	static class OnOpen {
		Session					session;
		ServerEndpointConfig	config;
	}

	@Data
	static class OnMessage {
		Session session;
	}

	@Data
	static class OnClose {
		Session		session;
		CloseReason	closeReason;
	}

	@Data
	static class OnError {
		Session		session;
		Throwable	throwable;
	}
}
