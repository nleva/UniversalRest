package ru.sendto.rest.gwt;


import org.fusesource.restygwt.client.DirectRestService;

public interface RestResponseHandler<T,S extends DirectRestService>{
	RestErrorHandler<S> onSuccess(URest.CallbackHandler<T> callbackHandler);
	S onSuccessOnly(URest.CallbackHandler<T> callbackHandler);
	S onFailureOnly(URest.ExcptionHandler excptionHandler);
	S requestOnly();
}