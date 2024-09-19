package controllers;

import com.feth.play.module.pa.PlayAuthenticate;
import models.User;
import play.i18n.MessagesApi;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.Http;
import service.UserService;
import views.html.restricted;

import javax.inject.Inject;

@Security.Authenticated(Secured.class)
public class Restricted extends Controller {

	private final PlayAuthenticate auth;

	private final UserService userService;
    protected final MessagesApi msg;

	@Inject
	public Restricted(final PlayAuthenticate auth, final UserService userService, final MessagesApi msg) {
		this.auth = auth;
		this.userService = userService;
		this.msg = msg;
	}

	public Result index(final Http.Request request) {
		final User localUser = this.userService.getLocalUser(this.auth.getUser(request.session()));
		return ok(restricted.render(this.auth, localUser, request, msg.preferred(request)));
	}
}
