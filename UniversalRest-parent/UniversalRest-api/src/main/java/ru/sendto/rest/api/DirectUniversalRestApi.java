package ru.sendto.rest.api;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.fusesource.restygwt.client.DirectRestService;

import ru.sendto.dto.Dto;

@Path("/")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
public interface DirectUniversalRestApi  extends DirectRestService {

	@POST
	@PUT
	List<Dto> doPost(Dto dto);

}