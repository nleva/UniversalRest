package ru.sendto.rest.websocket;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.ejb.LocalBean;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import lombok.extern.java.Log;
import ru.sendto.dto.Dto;
import ru.sendto.dto.ErrorDto;
import ru.sendto.ejb.EventResultsBean;
import ru.sendto.ejb.SingleRequestEventResultsBean;
import ru.sendto.rest.api.ResponseDto;
import ru.sendto.websocket.WebsocketEventService;

@Singleton
@LocalBean
@Lock(LockType.READ)
@ServerEndpoint(value = "/", encoders = { DtoCodec.class }, decoders = {
		DtoCodec.class }, configurator = WebsocketHandShaker.class)
@Log
public class Websocket extends WebsocketEventService {

	@Inject
	SingleRequestEventResultsBean ctx;

	ThreadLocal<Session> local = new ThreadLocal<>();
	
	@Produces
	public Session getLocalSession() {
		return local.get();
	}
	
	@Override
	public void onMessage(Dto msg, Session session) {
		local.set(session);
		ResponseDto rdto = new ResponseDto();
		try {
			rdto.setRequest(msg.getClass().newInstance());
		} catch (InstantiationException | IllegalAccessException e2) {
			log.throwing(Websocket.class.getName(), "dto.getClass().newInstance() failed", e2);
		}
		try {
			messageBus.fire(session);
			messagePayloadBus.fire(msg);

			final List<Dto> list = ctx.get();
			if (list == null)
				return;
			try {
				session.getBasicRemote().sendObject(rdto.setList(list));
			} catch (Exception e) {
				log.throwing(Websocket.class.getName(), "failed to send result to client", e);
			}
		} catch (Exception e) {
			try {
				session.getBasicRemote().sendObject(rdto.setList(
						Arrays.asList(new ErrorDto().setError(e.getMessage()))));
			} catch (IOException | EncodeException e1) {
				log.throwing(Websocket.class.getName(), "failed to send exception to client", e);
			}
			log.log(Level.SEVERE, e.getMessage(), e);
		}finally {
			ctx.clear();
			local.set(null);
		}
	}
}
