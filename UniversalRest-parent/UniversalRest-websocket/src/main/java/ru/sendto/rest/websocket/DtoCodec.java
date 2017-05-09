/**
 * 
 */
package ru.sendto.rest.websocket;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.java.Log;
import ru.sendto.dto.Dto;

@Log
public class DtoCodec implements Encoder.Text<Dto>, Decoder.Text<Dto> {

	@Inject
	ObjectMapper mapper;

	@Override
	public void destroy() {
	}

	@Override
	public void init(EndpointConfig arg0) {
		// mapper = new ObjectMapper();
	}

	@Override
	public String encode(Dto o) throws EncodeException {
		try {
			return mapper.writeValueAsString(o);
		} catch (JsonProcessingException e) {
			throw new EncodeException(e, e.getMessage());
		}
	}

	@Override
	public Dto decode(String msg) throws DecodeException {
		Dto version = null;
		try {
			version = mapper.readValue(msg, Dto.class);
		} catch (IOException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
			throw new DecodeException(msg, e.getMessage());
		}
		return version;
	}

	@Override
	public boolean willDecode(String arg0) {
		boolean will = arg0.getBytes()[0] == -1 ? false : true;
		return will;
	}

}