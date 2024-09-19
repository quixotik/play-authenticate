package controllers;

import com.feth.play.module.pa.PlayAuthenticate;
import play.i18n.MessagesApi;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.index;

import javax.inject.Inject;

public class Application extends Controller {

	public static final String FLASH_MESSAGE_KEY = "message";
	public static final String FLASH_ERROR_KEY = "error";

	private final PlayAuthenticate auth;
    protected final MessagesApi msg;

	@Inject
	public Application(final PlayAuthenticate auth, final MessagesApi msg) {
		this.auth = auth;
		this.msg = msg;
	}

	public static Result noCache(Result result) {
        result.withHeader(Http.HeaderNames.CACHE_CONTROL, "no-cache, no-store, must-revalidate");  // HTTP 1.1
        result.withHeader(Http.HeaderNames.PRAGMA, "no-cache");  // HTTP 1.0.
        result.withHeader(Http.HeaderNames.EXPIRES, "0");  // Proxies.
        return result;
    }

	public Result index(final Http.Request request) {
		return ok(index.render(this.auth, request, msg.preferred(request)));
	}

	public Result oAuthDenied(final String providerKey) {
		return noCache(redirect(routes.Application.index())
			.flashing(FLASH_ERROR_KEY,
				"You need to accept the OAuth connection in order to use this website!"));
	}
}