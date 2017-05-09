package ru.sendto.rest.websocket;

import java.util.Map;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

/**
 * Необходим для запоминания http сессии в момент подключения вэбсокета.
 */
@Singleton
@Lock(LockType.READ)
public class WebsocketHandShaker extends ServerEndpointConfig.Configurator {

	@Inject Event<ServerEndpointConfig> bus;
	
	@Override
	public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
		Map<String, Object> userProperties = sec.getUserProperties();
		userProperties.put("request", request);
		userProperties.put("response", response);
		bus.fire(sec);
	}
}
