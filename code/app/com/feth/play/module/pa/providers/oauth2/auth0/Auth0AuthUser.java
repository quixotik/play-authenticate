package com.feth.play.module.pa.providers.oauth2.auth0;

import java.util.Locale;

import com.fasterxml.jackson.databind.JsonNode;

import com.feth.play.module.pa.providers.oauth2.BasicOAuth2AuthUser;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.ExtendedIdentity;
import com.feth.play.module.pa.user.LocaleIdentity;
import com.feth.play.module.pa.user.PicturedIdentity;
import com.feth.play.module.pa.user.ProfiledIdentity;

public class Auth0AuthUser extends BasicOAuth2AuthUser implements
		ExtendedIdentity, PicturedIdentity, ProfiledIdentity, LocaleIdentity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * From https://auth0.com/docs/api/authentication#get-user-info
	 */
	private static class Constants {
		public static final String ID = "sub";
		public static final String EMAIL = "email";
		public static final String EMAIL_IS_VERIFIED = "email_verified";
		public static final String NAME = "name";
		public static final String FIRST_NAME = "given_name";
		public static final String LAST_NAME = "family_name";
		public static final String PICTURE = "picture";
		public static final String GENDER = "gender";
		public static final String LOCALE = "locale";
		public static final String LINK = "profile";
	}

	private String email;
	private boolean emailIsVerified = false;
	private String name;
	private String firstName;
	private String lastName;
	private String picture;
	private String gender;
	private String locale;
	private String link;

	public Auth0AuthUser(final JsonNode n, final Auth0AuthInfo info,
			final String state) {
		super(n.get(Constants.ID).asText(), info, state);

		if (n.has(Constants.EMAIL)) {
			this.email = n.get(Constants.EMAIL).asText();
		}
		if (n.has(Constants.EMAIL_IS_VERIFIED)) {
			this.emailIsVerified = n.get(Constants.EMAIL_IS_VERIFIED)
					.asBoolean();
		}

		if (n.has(Constants.NAME)) {
			this.name = n.get(Constants.NAME).asText();
		}

		if (n.has(Constants.FIRST_NAME)) {
			this.firstName = n.get(Constants.FIRST_NAME).asText();
		}
		if (n.has(Constants.LAST_NAME)) {
			this.lastName = n.get(Constants.LAST_NAME).asText();
		}
		if (n.has(Constants.PICTURE)) {
			this.picture = n.get(Constants.PICTURE).asText();
		}
		if (n.has(Constants.GENDER)) {
			this.gender = n.get(Constants.GENDER).asText();
		}
		if (n.has(Constants.LOCALE)) {
			this.locale = n.get(Constants.LOCALE).asText();
		}
		if (n.has(Constants.LINK)) {
			this.link = n.get(Constants.LINK).asText();
		}
	}

	public Auth0AuthInfo getAuthInfo() {
		return (Auth0AuthInfo) getOAuth2AuthInfo();
	}

	@Override
	public String getProvider() {
		return Auth0AuthProvider.PROVIDER_KEY;
	}

	public String getEmail() {
		return email;
	}

	public boolean isEmailVerified() {
		return emailIsVerified;
	}

	public String getName() {
		return name;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getPicture() {
		return picture;
	}

	public String getGender() {
		return gender;
	}

	public String getProfileLink() {
		return link;
	}

	public Locale getLocale() {
		return AuthUser.getLocaleFromString(locale);
	}
}