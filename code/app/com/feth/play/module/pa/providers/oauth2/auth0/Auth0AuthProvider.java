package com.feth.play.module.pa.providers.oauth2.auth0;

import java.security.AuthProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.exceptions.AccessTokenException;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.exceptions.ResolverMissingException;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider;
import com.feth.play.module.pa.providers.oauth2.google.GoogleAuthInfo;
import com.feth.play.module.pa.providers.oauth2.google.GoogleAuthUser;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.typesafe.config.Config;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import play.Logger;
import play.i18n.MessagesApi;
import play.inject.ApplicationLifecycle;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Http;

import javax.inject.Inject;
import javax.inject.Singleton;

public class Auth0AuthProvider extends OAuth2AuthProvider<Auth0AuthUser, Auth0AuthInfo> {

    public static final String PROVIDER_KEY = "auth0";

    public static abstract class Auth0SettingKeys extends
            OAuth2AuthProvider.SettingKeys {
		public static final String LOGOUT_URL = "logoutUrl";
		public static final String USER_INFO_URL = "userInfoUrl";
		public static final String ORGANIZATION_ID = "organization";
    }

    public static abstract class Auth0Constants extends OAuth2AuthProvider.Constants {
		public static final String ID_TOKEN = "id_token";
		public static final String ID_TOKEN_HINT = "id_token_hint";
		public static final String NONCE = "nonce";
        public static final String ORGANIZATION_ID = "organization";
    }

	@Inject
	public Auth0AuthProvider(final PlayAuthenticate auth, final ApplicationLifecycle lifecycle, final WSClient wsClient, final MessagesApi messagesApi) {
		super(auth, lifecycle, wsClient, messagesApi);
	}

    @Override
    public String getKey() {
		return PROVIDER_KEY;
    }

    @Override
    protected Auth0AuthInfo buildInfo(final WSResponse r)
            throws AccessTokenException {
		final JsonNode n = r.asJson();
		Logger.debug(n.toString());

		if (n.get(OAuth2AuthProvider.Constants.ERROR) != null) {
			throw new AccessTokenException(n.get(
					OAuth2AuthProvider.Constants.ERROR).asText());
		} else {
			return new Auth0AuthInfo(n);
		}
    }

    @Override
    protected AuthUserIdentity transform(final Auth0AuthInfo info, final String state) throws AuthException {
		final String url = getConfiguration().getString(Auth0SettingKeys.USER_INFO_URL);

		final WSResponse r = fetchAuthResponse(url,
				new QueryParam(OAuth2AuthProvider.Constants.ACCESS_TOKEN, info.getAccessToken())
		);

		final JsonNode result = r.asJson();
		if (result.get(OAuth2AuthProvider.Constants.ERROR) != null) {
			throw new AuthException(result.get(
					OAuth2AuthProvider.Constants.ERROR).asText());
		} else {
			Logger.debug(result.toString());
			return new Auth0AuthUser(result, info, state);
		}
    }

	protected List<NameValuePair> getParams(final Http.RequestHeader requestHeader,
			final Config c) throws ResolverMissingException {
		final List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.addAll(super.getParams(requestHeader, c));
		final String organizationId = c.getString(Auth0SettingKeys.ORGANIZATION_ID);
		if (organizationId != null) {
			params.add(new BasicNameValuePair(
				Auth0Constants.ORGANIZATION_ID,
				organizationId));	
		}
		return params;
	}

	public void logout(AuthUser authUser) throws AuthException {
		if (!getConfiguration().hasPath(Auth0SettingKeys.LOGOUT_URL)) {
			return;
		}
		final String url = getConfiguration().getString(Auth0SettingKeys.LOGOUT_URL);
		final Auth0AuthUser auth0AuthUser = (Auth0AuthUser) authUser;
		final WSResponse r = fetchAuthResponse(url,
				new QueryParam(Auth0Constants.ID_TOKEN_HINT, auth0AuthUser.getAuthInfo().getIdToken())
		);

		final JsonNode result = r.asJson();
		if (result.get(OAuth2AuthProvider.Constants.ERROR) != null) {
			throw new AuthException(result.get(
					OAuth2AuthProvider.Constants.ERROR).asText());
		}		
	}	

}