package controllers;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.Http;

import javax.inject.Inject;

public class JavaController extends Controller {

	public static final String FLASH_ERROR_KEY = "error";

	private PlayAuthenticate auth;

	@Inject
	public JavaController(PlayAuthenticate auth) {
		this.auth = auth;
	}

	@Security.Authenticated(JavaSecured.class)
	public Result index(Http.Request request) {
		AuthUser user = this.auth.getUser(request);
		return ok(user.getProvider() + ": " + user.getId());
	}

}
