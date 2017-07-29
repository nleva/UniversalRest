package ru.sendto.rest.websocket;

import java.util.List;
import java.util.Map;

import javax.ejb.LocalBean;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import lombok.extern.java.Log;
import ru.sendto.dto.Dto;
import ru.sendto.ejb.EventResultsBean;
import ru.sendto.websocket.WebsocketEventService;

@Singleton
@LocalBean
@Lock(LockType.READ)
@ServerEndpoint(value = "/", encoders = { DtoCodec.class }, decoders = {
		DtoCodec.class }, configurator = WebsocketHandShaker.class)
@Log
public class Websocket extends WebsocketEventService {

	@Inject
	EventResultsBean ctx;

	@Override
	public void onMessage(Dto msg, Session session) {
		super.onMessage(msg, session);
		final Map<Dto, List<Dto>> data = ctx.getData();
		final List<Dto> list = data.get(msg);
		if (list == null)
			return;
		try {
			for (Dto d : list) {
				if (d != null) {
					session.getBasicRemote().sendObject(d);
				}
			}
		} catch (Exception e) {
			log.throwing(Websocket.class.getName(), "doPost", e);
		}
	}
}
