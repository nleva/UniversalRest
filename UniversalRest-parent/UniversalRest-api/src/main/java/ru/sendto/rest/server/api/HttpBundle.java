package ru.sendto.rest.server.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.WebConnection;

import lombok.Data;

@Data
public class HttpBundle {
	HttpServletRequest request;
	HttpServletResponse response;
	HttpSession httpSession;
//	WebConnection webConnection;
}
