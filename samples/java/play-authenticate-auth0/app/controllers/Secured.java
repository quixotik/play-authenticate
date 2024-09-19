package controllers;

import play.mvc.Http.RequestHeader;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.Http;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;

import java.util.Optional;

import javax.inject.Inject;

public class Secured extends Security.Authenticator {

	private final PlayAuthenticate auth;

	@Inject
	public Secured(final PlayAuthenticate auth) {
		this.auth = auth;
	}

	public Optional<String> getUsername(final Http.Request request) {
		final AuthUser u = this.auth.getUser(request.session());

		if (u != null) {
			return Optional.of(u.getId());
		} else {
			return null;
		}
	}

	public Result onUnauthorized(final Http.Request request) {
		return redirect(routes.Application.index()).
			flashing(Application.FLASH_MESSAGE_KEY, "Nice try, but you need to log in first!");
	}
}