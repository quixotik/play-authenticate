package security;

import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.ExecutionHttp.RequestHeaderProvider;
import be.objectify.deadbolt.java.cache.HandlerCache;
import com.feth.play.module.pa.PlayAuthenticate;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MyHandlerCache implements HandlerCache {

	private final DeadboltHandler defaultHandler;

	private final PlayAuthenticate auth;

	@Inject
	public MyHandlerCache(final PlayAuthenticate auth, final ExecutionHttp.RequestHeaderProvider execHttp.RequestHeaderProvider) {
		this.auth = auth;
		this.defaultHandler = new MyDeadboltHandler(auth, execHttp.RequestHeaderProvider);
	}

	@Override
	public DeadboltHandler apply(final String key) {
		return this.defaultHandler;
	}

	@Override
	public DeadboltHandler get() {
		return this.defaultHandler;
	}
}