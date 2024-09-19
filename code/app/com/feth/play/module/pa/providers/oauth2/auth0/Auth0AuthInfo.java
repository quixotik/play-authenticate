package com.feth.play.module.pa.providers.oauth2.auth0;

import java.util.Date;

import com.fasterxml.jackson.databind.JsonNode;

import com.feth.play.module.pa.providers.oauth2.OAuth2AuthInfo;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider.Constants;
import com.feth.play.module.pa.providers.oauth2.auth0.Auth0AuthProvider.Auth0Constants;

public class Auth0AuthInfo extends OAuth2AuthInfo {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String bearer;
	private String idToken;

	public Auth0AuthInfo(final JsonNode node) {
		super(	node.get(Constants.ACCESS_TOKEN) != null ? node.get(Constants.ACCESS_TOKEN).asText() : null,
				node.get(Constants.EXPIRES_IN) != null ? new Date().getTime() + node.get(Constants.EXPIRES_IN).asLong() * 1000 : -1,
				node.get(Constants.REFRESH_TOKEN) != null ? node.get(Constants.REFRESH_TOKEN).asText() : null);

		bearer = node.get(Constants.TOKEN_TYPE).asText();
		idToken = node.get(Auth0Constants.ID_TOKEN).asText();
	}

	public String getBearer() {
		return bearer;
	}

	public String getIdToken() {
		return idToken;
	}
}
