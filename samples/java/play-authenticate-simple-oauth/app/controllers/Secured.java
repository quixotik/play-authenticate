package controllers;

import play.mvc.Http.RequestHeader;
import play.mvc.Result;
import play.mvc.Security;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;

import javax.inject.Inject;

public class Secured extends Security.Authenticator {

	private final PlayAuthenticate auth;

	@Inject
	public Secured(final PlayAuthenticate auth) {
		this.auth = auth;
	}

	@Override
	public String getUsername(final Http.RequestHeader ctx) {
		final AuthUser u = this.auth.getUser(ctx.session());

		if (u != null) {
			return u.getId();
		} else {
			return null;
		}
	}

	@Override
	public Result onUnauthorized(final Http.RequestHeader ctx) {
		ctx.flash().put(Application.FLASH_MESSAGE_KEY, "Nice try, but you need to log in first!");
		return redirect(routes.Application.index());
	}
}