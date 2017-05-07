package ru.sendto.rest.gwt;


import org.fusesource.restygwt.client.Defaults;
import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.REST;

import com.google.gwt.core.client.GWT;

import ru.sendto.gwt.client.html.Log;
import ru.sendto.rest.api.DirectUniversalRestApi;

/**
 * Содержит ссылки на rest сервисы.
 */
public class URest<T,S extends DirectRestService> implements RestResponseHandler<T, S>, RestErrorHandler<S>{

	S service;
	CallbackHandler<T> callbackHandler;
	ExcptionHandler excptionHandler;
	
	private URest(S service) {
		super();
		this.service = service;
	}

	private URest<T, S> setService(S service) {
		this.service = service;
		return this;
	}
	
	@Override
	public RestErrorHandler<S> onSuccess(CallbackHandler<T> callbackHandler) {
		this.callbackHandler = callbackHandler;
		return this;
	}
	
	@Override
	public S onSuccessOnly(CallbackHandler<T> callbackHandler) {
		this.callbackHandler = callbackHandler;
		return REST.withCallback(new MethodCallback<T>(){
			@Override
			public void onSuccess(Method method, T response) {
				if(callbackHandler!=null) callbackHandler.run(response);
			}
			@Override
			public void onFailure(Method method, Throwable exception) {
			}
		}).call(service);
	}
	@Override
	public S requestOnly() {
		return REST.withCallback(new MethodCallback<T>(){
			@Override public void onSuccess(Method method, T response) {}
			@Override public void onFailure(Method method, Throwable exception) {}
		}).call(service);
	}
	
	@Override
	public S onFailureOnly(ExcptionHandler excptionHandler) {
		return onFailure(excptionHandler);
	}
	@Override
	public S onFailure(ExcptionHandler excptionHandler) {
		this.excptionHandler = excptionHandler;
		
		return REST.withCallback(new MethodCallback<T>(){
			@Override
			public void onSuccess(Method method, T response) {
				if(callbackHandler!=null) callbackHandler.run(response);
			}
			@Override
			public void onFailure(Method method, Throwable exception) {
				if(excptionHandler!=null) excptionHandler.run(exception);
			}
		}).call(service);
	}
	@Override
	public S logOnFailure() {
		return onFailure(e->Log.console(e));
	}
	
	public static void init() {
		init("/UniversalRest/");
	}
	public static void init(String uniRestModileUrl) {
		Defaults.setServiceRoot(uniRestModileUrl);
	}

	public static interface SimpleMethodCallback<T> extends MethodCallback<T>{
		@Override
		default void onFailure(Method method, Throwable exception) {
			Log.console(exception.getMessage());
		}
		@Override
		default void onSuccess(Method method, T response) {
			run(response);
		}
		void run(T result);
	}

	public static interface CallbackHandler<T> {
		void run(T result);
	}
	public static interface ExcptionHandler{
		void run(Throwable result);
	}

	
	public static <T extends DirectRestService> T call(T service){
		return callback(r->{}).call(service);
	}
	
	public static <T> REST<T> callback(SimpleMethodCallback<T> smc){
		return REST.withCallback(smc);
	}
	
	private static DirectUniversalRestApi restService = null;
	public static DirectUniversalRestApi getUniRest() {
		if (restService == null)
			restService = GWT.create(DirectUniversalRestApi.class);
		return restService;
	}
	public static <T> RestResponseHandler<T, DirectUniversalRestApi> useUniRest() {
		return new URest<>(getUniRest());
	}

}
