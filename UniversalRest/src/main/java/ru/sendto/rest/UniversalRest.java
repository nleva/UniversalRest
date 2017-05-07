package ru.sendto.rest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import lombok.extern.java.Log;
import ru.sendto.dto.Dto;
import ru.sendto.ejb.EventResultsBean;
import ru.sendto.ejb.dto.ErrorDto;

@Stateless
@Path("/")
@Log
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class UniversalRest {

	@Inject
	Event<Dto> bus;

	@Inject
	EventResultsBean ctx;

	@Resource
	SessionContext sctx;
	
	boolean init = false;

	@POST
	@PUT
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public List<Dto> doPost(Dto dto) {
		try {
			init = true;
			bus.fire(dto);
			final Map<Dto, List<Dto>> data = ctx.getData();
			final List<Dto> list = data.get(dto);
			return list;
		} catch (Exception e) {
			sctx.setRollbackOnly();
			log.throwing(UniversalRest.class.getName(), "doPost", e);
			return Arrays.asList(new ErrorDto().setError(e.getMessage()));
		}
	}

	@GET
	@HEAD
	@DELETE
	@OPTIONS
	public List<Dto> onOtherMethods() {
		return Arrays.asList(new ErrorDto().setError("Only POST and PUT methods are permitted"));
	}

	public void afterComplition(@Observes(during = TransactionPhase.AFTER_COMPLETION) Dto dto) {
		if (init) {
			ctx.clear(dto);
		}
		init = false;
	}

}
