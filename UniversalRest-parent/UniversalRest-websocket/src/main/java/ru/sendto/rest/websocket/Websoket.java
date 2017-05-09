package ru.sendto.rest.websocket;

import javax.ejb.LocalBean;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.websocket.server.ServerEndpoint;

import ru.sendto.websocket.WebsoketEventService;

@Singleton
@LocalBean
@Lock(LockType.READ)
@ServerEndpoint(
		value = "/", 
		encoders = { DtoCodec.class }, 
		decoders = { DtoCodec.class },
		configurator=WebsocketHandShaker.class)
public class Websoket extends WebsoketEventService {

}
