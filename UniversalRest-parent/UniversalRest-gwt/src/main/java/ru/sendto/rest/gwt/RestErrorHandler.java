package ru.sendto.rest.gwt;


import org.fusesource.restygwt.client.DirectRestService;

public interface RestErrorHandler<S extends DirectRestService>{
	S onFailure(URest.ExcptionHandler excptionHandler);
	S logOnFailure();
}