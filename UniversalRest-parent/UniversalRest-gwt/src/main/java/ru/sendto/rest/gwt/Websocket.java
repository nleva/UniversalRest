package ru.sendto.rest.gwt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.function.Consumer;

import org.fusesource.restygwt.client.JsonEncoderDecoder;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.typedarrays.shared.ArrayBuffer;

import ru.sendto.dto.Dto;
import ru.sendto.dto.RequestInfo;
import ru.sendto.gwt.client.html.Log;
import ru.sendto.gwt.client.util.Bus;
import ru.sendto.rest.api.ResponseDto;

/**
 * Класс реализующий вэбсокет.
 */
public class Websocket {
	
	static List<Consumer<Dto>> filters = new ArrayList<>();

	
	
	/**
	 * Интерфейс для обратного вызова прихода текстовых сообщений.
	 */
	@FunctionalInterface
	public static interface IDtoMessageCallback<T extends Dto> {
		public void onMessage(T dto);
	}

	/**
	 * Интерфейс для обратного вызова прихода бинарных сообщений.
	 */
	public static interface IBynaryMessageCallback {
		default public void onMessage(ArrayBuffer b) {
		};
	}

	public static class ConnectedEvent{};
	public static class DisconnectedEvent{};

	/**
	 * Очередь сообщений, которые ожидают отправки
	 */
	static LinkedList<Dto> queueToSend = new LinkedList<>();

	// Объект вэбсокета.
	static JavaScriptObject ws;
	static boolean connected = false;

	// Для преобразования данных dto.
	static interface Codec extends JsonEncoderDecoder<Dto> {
	};

	static Codec codec = null;
	// Список обработчиков dto.
	static Map<String, List<IDtoMessageCallback>> callbackDtoList = new HashMap();
	// Список обработчиков бинарных данных.
	static List<IBynaryMessageCallback> callbackBynaryList = new ArrayList<IBynaryMessageCallback>();

	/**
	 * Инициализация подключения.
	 * 
	 * @param url
	 *            - строка подключения.
	 */
	public static void init(String url, Consumer<Dto>...filters) {
		
		Websocket.filters.addAll(Arrays.asList(filters));
		codec = GWT.create(Codec.class);
		url = url.replaceFirst("http", "ws");
		connect(url);
	}

	/**
	 * Добавление обработчиков событий.
	 * 
	 * use Bus.get().listen(...) instead
	 * 
	 * @param type
	 *            - тип dto входных данных.
	 * @param callback
	 *            - обработчик данных.
	 */
	@Deprecated
	public static <T extends Dto> void addMessageListener(Class<T> type, IDtoMessageCallback<T> callback) {
//		Bus.get().listen(type, t->callback.onMessage(t));
		List<IDtoMessageCallback> list = callbackDtoList.get(type.getName());
		if (list == null) {
			list = new ArrayList<>();
			callbackDtoList.put(type.getName(), list);
		}
		list.add(callback);
	}

	/**
	 * Добавление обработчиков событий (бинарные данные).
	 * 
	 * @param callback
	 *            - обработчик данных.
	 */
	public static void addMessageListener(IBynaryMessageCallback callback) {
		callbackBynaryList.add(callback);
	}

	/**
	 * Отправка данных.
	 * 
	 * @param dto
	 *            - отправляемые данные.
	 */
	public static void send(Dto dto) {
		filters.forEach(c->c.accept(dto));
		if (connected) {
			send(codec.encode(dto).toString());
		} else {
			queueToSend.add(dto);
		}
	}
	/**
	 * Отправка текстовых данных.
	 * 
	 * @param msg
	 *            - текстовые данные.
	 */
	private static native void send(String msg)/*-{
		@ru.sendto.rest.gwt.Websocket::ws.send(msg);
	}-*/;

	/**
	 * Подключение соекта к заданному url.
	 * 
	 * @param url
	 *            - куда подключаемся.
	 */
	private static native void connect(String url) /*-{
		
		
		@ru.sendto.rest.gwt.Websocket::ws = new WebSocket(url);
		@ru.sendto.rest.gwt.Websocket::ws.binaryType = "arraybuffer";
		@ru.sendto.rest.gwt.Websocket::ws.onmessage = function(e) {
			console.log("--->");
			if (typeof e.data === "string"){
				console.log("--->"+e.data);
				@ru.sendto.rest.gwt.Websocket::receiv(*)(e.data);
			}else{
				@ru.sendto.rest.gwt.Websocket::receivBin(*)(e.data);
			}
		}
		@ru.sendto.rest.gwt.Websocket::ws.onopen = function() {
			console.log("connected...");
			@ru.sendto.rest.gwt.Websocket::connected = true;
			@ru.sendto.rest.gwt.Websocket::reconnectCount = 0;
			@ru.sendto.rest.gwt.Websocket::onOpen()();
		}
		@ru.sendto.rest.gwt.Websocket::ws.onclose = function() {
			console.log("clodsed");
			if (@ru.sendto.rest.gwt.Websocket::connected)
				@ru.sendto.rest.gwt.Websocket::reconnect(Ljava/lang/String;I)(url,5000);
			@ru.sendto.rest.gwt.Websocket::connected = false;

		}
		@ru.sendto.rest.gwt.Websocket::ws.onerror = function(e) {
			console.log("error:" + e.data);
			//			if(@ru.sendto.rest.gwt.Websocket::connected)
			@ru.sendto.rest.gwt.Websocket::reconnect(Ljava/lang/String;I)(url,5000);
			@ru.sendto.rest.gwt.Websocket::connected = false;

		}
	}-*/;

	/**
	 * Вызывается автоматически при открытии соединения Отправляет сообщения,
	 * которые ожидали отправки.
	 */
	static void onOpen() {
		Bus.get().fire(new ConnectedEvent());
		ListIterator<Dto> iter = queueToSend.listIterator();
		for (; iter.hasNext();) {
			send(iter.next());
			iter.remove();
		}
	}

	private static int reconnectCount = 0;
	static elemental.html.Notification reconnectNotification;

	private static void reconnect(String url, int delay) {
		reconnectCount++;

		Bus.get().fire(new DisconnectedEvent());
		Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {

			@Override
			public boolean execute() {
				connect(url);
				return false;
			}
		}, delay/* +(reconnectCount>3?10_000:1_000) */);
	}

	/**
	 * Обработка текстовых данных.
	 * 
	 * @param self
	 *            - ссылка на данный класс.
	 * @param m
	 *            - строка сообщения.
	 */
	private static void receiv(String m) {
		Log.console(m);
		Dto dto = codec.decode(m);
		Log.console(dto.getClass().getName());
		if(dto instanceof ResponseDto) {

			Log.console("1");
			ResponseDto respDto = (ResponseDto)dto;
			final List<Dto> list = respDto.getList();
			final Bus bus = Bus.get(respDto.getRequest());
			Log.console(respDto.getRequest().getClass().getName());
			list.forEach(bus::fire);
		}else if(dto instanceof RequestInfo) {

			Log.console("2");
			Bus.get(((RequestInfo)dto).getRequest().getClass().getName()).fire(dto);
		}

		Log.console("3");
		Bus.get().fire(dto);

		Log.console("4");
		invokeOldCalbacks(dto);

		Log.console("5");
	}

	@Deprecated
	private static void invokeOldCalbacks(Dto dto) {
		List<IDtoMessageCallback> callList = callbackDtoList.get(dto.getClass().getName());
		Log.console("old listeners size " + callList.size());
		if(callList!=null){
			for (IDtoMessageCallback call : callList) {
				call.onMessage(dto);
			}
		}
	}

	/**
	 * Обработка бинарных данных.
	 * 
	 * @param self
	 *            - ссылка на данный класс.
	 * @param b
	 *            - массив байт.
	 */
	private static void receivBin(ArrayBuffer b) {
		for (IBynaryMessageCallback c : callbackBynaryList)
			c.onMessage(b);
	}

}
