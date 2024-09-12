package controllers;

import play.mvc.Http.RequestHeader;
import play.mvc.Result;
import play.mvc.Security;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;

import controllers.routes;

import javax.inject.Inject;

public class JavaSecured extends Security.Authenticator {

    public static final String FLASH_MESSAGE_KEY = "message";

    private PlayAuthenticate auth;

    @Inject
    public JavaSecured(PlayAuthenticate auth) {
        this.auth = auth;
    }

    @Override
    public String getUsername(final Http.RequestHeader ctx) {
	final AuthUser u = this.auth.getUser(ctx.session());
	return (u != null ? u.getId() : null);
    }

    @Override
    public Result onUnauthorized(final Http.RequestHeader ctx) {
	ctx.flash().put(FLASH_MESSAGE_KEY,
		"Nice try, but you need to log in first!");
	return redirect(routes.ApplicationController.index());
    }
}