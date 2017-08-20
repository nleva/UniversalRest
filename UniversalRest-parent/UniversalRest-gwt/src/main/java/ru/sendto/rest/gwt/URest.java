package ru.sendto.rest.gwt;


import java.util.List;

import org.fusesource.restygwt.client.Defaults;
import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.REST;

import com.google.gwt.core.client.GWT;

import ru.sendto.dto.Dto;
import ru.sendto.dto.ErrorDto;
import ru.sendto.gwt.client.util.Bus;
import ru.sendto.rest.api.DirectUniversalRestApi;

/**
 * Universal rest service api
 * .setServiceRoot(url) to set UniversalRest war location root
 * .send(Dto) to send message
 * Bus.get().listen to set listener
 * 
 * @author Lev Nadeinsky
 */
public class URest<T,S extends DirectRestService> {

	private static class Callback implements MethodCallback<List<Dto>> {
		
		Dto request;
		
		public Callback setRequest(Dto request) {
			this.request = request;
			return this;
		}
		
		@Override
		public void onFailure(Method method, Throwable exception) {
			Bus.get(request.getClass().getName())
					.fire(new ErrorDto().setError(exception.getMessage()));
			Bus.get()
					.fire(new ErrorDto().setError(exception.getMessage()));
		}

		@Override
		public void onSuccess(Method method, List<Dto> list) {
			final Bus bus = Bus.get(request.getClass().getName());
			list.forEach(bus::fire);
			list.forEach(Bus.get()::fire);
		}
	}
	static {
		setServiceRoot("/UniversalRest/");
	}
	
	private URest() {
	}
	
	public static void send(Dto dto){
		REST.withCallback(new Callback().setRequest(dto)).call(getUniRest()).doPost(dto);
	}
	
	public static void setServiceRoot(String serviceRootUrl) {
		Defaults.setServiceRoot(serviceRootUrl);
	}
	
	private static DirectUniversalRestApi restService = null;
	private static DirectUniversalRestApi getUniRest() {
		if (restService == null)
			restService = GWT.create(DirectUniversalRestApi.class);
		return restService;
	}

}
