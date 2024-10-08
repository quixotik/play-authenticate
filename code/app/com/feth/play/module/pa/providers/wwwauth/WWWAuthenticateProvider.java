/*
 * Copyright © 2014 Florian Hars, nMIT Solutions GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.feth.play.module.pa.providers.wwwauth;

import java.util.Optional;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.AuthProvider;
import com.feth.play.module.pa.user.AuthUser;
import play.inject.ApplicationLifecycle;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.twirl.api.Content;

/** A base class for browser based authentication using the WWW-Authenticate header.
 *
 * This does not fully implement the usual mechanism where a whole
 * site or directory is protected by one of these authentication
 * mechanisms. The intended use case is that it protects just a single
 * URL so that it can be used as one of play-authenticate's mechanisms.
 *
 * Unlike other mechanisms, it returns a formatted page on authentication
 * failure, which could for example be a login form for one or more of
 * the other mechanisms supported.
 */
public abstract class WWWAuthenticateProvider extends AuthProvider {

	public WWWAuthenticateProvider(final PlayAuthenticate auth, final ApplicationLifecycle lifecycle) {
		super(auth, lifecycle);
	}

	/** The name of the authentication scheme
	 *
	 * @return The name of the authentication scheme, like Basic or Negotiate
	 */
	protected abstract String authScheme();

	/** The challenge to provide to an unauthenticated client.
	 *
	 * @param requestHeader The current request requestHeader
	 * @return The challenge string to return (without the scheme name), or null
	 */
	protected abstract String challenge(Http.RequestHeader requestHeader);

	/** Try to authenticate the incoming Request.
	 *
	 * @param response The response to the challenge (without the scheme name)
	 * @return An AuthUser or null if authentication failed
	 * @throws AuthException
	 */
	protected abstract AuthUser authenticateResponse(String response) throws AuthException;

	/** The 401 page to return to the browser if authentication failed.
	 *
	 * This could for example be a login form that submits to another
	 * authentication method.
	 *
	 * @param requestHeader The current request requestHeader
	 * @return The formatted unauthorized page
	 */
	protected Content unauthorized(Http.RequestHeader requestHeader) {
		return new Content() {

			@Override
			public String body() {
				return "Go away, you don't exit.";
			}

			@Override
			public String contentType() {
				return "text/plain";
			}};
	}

	private Result deny(Http.RequestHeader requestHeader) {
		String authChallenge = challenge(requestHeader);
		if (authChallenge == null) {
			authChallenge = authScheme();
		} else {
			authChallenge = authScheme()+" "+authChallenge;
		}

		Result result = Controller.unauthorized(unauthorized(requestHeader));
		result.withHeader("WWW-Authenticate", authChallenge);
		return result;
	}

	@Override
	public Object authenticate(Http.Request request, Object payload)	throws AuthException {
		Optional<String> authHeader = request.header("Authorization");

		if (!authHeader.isPresent()) {
			return deny(request);
		}
		String auth = authHeader.get();
		int ix = auth.indexOf(32);
		if (ix == -1 || !authScheme().equalsIgnoreCase(auth.substring(0,ix))) {
			return deny(request);
		}
		AuthUser user = authenticateResponse(auth.substring(ix+1));
		if (user == null) {
			return deny(request);
		} else {
			return user;
		}
	}

	@Override
	public boolean isExternal() {
		return false;
	}

}
