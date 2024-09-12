package com.feth.play.module.pa.controllers;

import com.feth.play.module.pa.PlayAuthenticate;
import play.mvc.Result;
import play.mvc.Http;

import javax.inject.Inject;

public class Authenticate extends AuthenticateBase {

	private PlayAuthenticate auth;

	@Inject
	public Authenticate(PlayAuthenticate auth) {
		this.auth = auth;
	}

	public Result authenticate(Http.Request request, final String provider) {
		final String payload = request.getQueryString(PAYLOAD_KEY);
		return noCache(this.auth.handleAuthentication(provider, request, payload));
	}

	public Result logout(Http.Request request) {
		return noCache(this.auth.logout(request.session()));
	}
}
