package ru.sendto.rest;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.core.Context;

import lombok.extern.java.Log;
import ru.sendto.dto.Dto;
import ru.sendto.dto.ErrorDto;
import ru.sendto.ejb.SingleRequestEventResultsBean;
import ru.sendto.rest.api.DirectUniversalRestApi;
import ru.sendto.rest.api.ResponseDto;
import ru.sendto.rest.server.api.HttpBundle;

@Log
@Stateless
@LocalBean
public class UniversalRest implements DirectUniversalRestApi {

	@Inject
	Event<Object> bus;

//	@Inject
//	EventResultsBean ctx;

	@Inject
	SingleRequestEventResultsBean ctx;
	
	@Resource
	SessionContext sctx;

	@Context
	HttpServletResponse resp;
	@Context
	HttpServletRequest req;
	@Context
	ServletContext servletCtx;
	
	boolean init = false;

	/* (non-Javadoc)
	 * @see ru.sendto.rest.DirectUniversalRestApi#doPost(ru.sendto.dto.Dto)
	 */
	@Override
	public List<Dto> doPost(Dto dto) {
		try {
			req.setAttribute("response", resp);
			init = true;
			bus.fire(new HttpBundle().setRequest(req).setResponse(resp));
			bus.fire(dto);
//			final Map<Dto, List<Dto>> data = ctx.getData();
			final List<Dto> list = ctx.get();
			return list;
		} catch (Exception e) {
			sctx.setRollbackOnly();
			log.throwing(UniversalRest.class.getName(), "doPost", e);
			return Arrays.asList(new ErrorDto().setError(e.getMessage()));
		}
	}
	
	@Override
	public ResponseDto doPostWraped(Dto dto) {
		ResponseDto rdto = new ResponseDto();
		try {
			rdto.setRequest(dto.getClass().newInstance());
		} catch (InstantiationException | IllegalAccessException e1) {
			log.throwing(UniversalRest.class.getName(), "dto.getClass().newInstance() failed", e1);
			
		}
		try {
			req.setAttribute("response", resp);
			init = true;
			bus.fire(new HttpBundle().setRequest(req).setResponse(resp));
			bus.fire(dto);
			final List<Dto> list = ctx.get();
			
			return rdto.setList(list);
			
		} catch (Exception e) {
			sctx.setRollbackOnly();
			log.throwing(UniversalRest.class.getName(), "doPost", e);
			return rdto.setList(Arrays.asList(new ErrorDto().setError(e.getMessage())));
		}
	}
	
	public void addFilter(@Observes Filter f){
		servletCtx.addFilter(f.getClass().getCanonicalName(), f);
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
			ctx.clear();
		}
		init = false;
	}

}
