package providers;

import com.feth.play.module.mail.Mailer.Mail.Body;
import com.feth.play.module.mail.Mailer.MailerFactory;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import controllers.routes;
import models.LinkedAccount;
import models.TokenAction;
import models.TokenAction.Type;
import models.User;
import play.Application;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Required;
import play.i18n.Lang;
import play.inject.ApplicationLifecycle;
import play.mvc.Call;
import play.mvc.Http;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Singleton
public class MyUsernamePasswordAuthProvider
		extends
		UsernamePasswordAuthProvider<String, MyLoginUsernamePasswordAuthUser, MyUsernamePasswordAuthUser, MyUsernamePasswordAuthProvider.MyLogin, MyUsernamePasswordAuthProvider.MySignup> {

	private static final String SETTING_KEY_VERIFICATION_LINK_SECURE = SETTING_KEY_MAIL
			+ "." + "verificationLink.secure";
	private static final String SETTING_KEY_PASSWORD_RESET_LINK_SECURE = SETTING_KEY_MAIL
			+ "." + "passwordResetLink.secure";
	private static final String SETTING_KEY_LINK_LOGIN_AFTER_PASSWORD_RESET = "loginAfterPasswordReset";

	private static final String EMAIL_TEMPLATE_FALLBACK_LANGUAGE = "en";

	@Override
	protected List<String> neededSettingKeys() {
		final List<String> needed = new ArrayList<String>(
				super.neededSettingKeys());
		needed.add(SETTING_KEY_VERIFICATION_LINK_SECURE);
		needed.add(SETTING_KEY_PASSWORD_RESET_LINK_SECURE);
		needed.add(SETTING_KEY_LINK_LOGIN_AFTER_PASSWORD_RESET);
		return needed;
	}

	public static class MyIdentity {

		public MyIdentity() {
		}

		public MyIdentity(final String email) {
			this.email = email;
		}

		@Required
		@Email
		public String email;

	}

	public static class MyLogin extends MyIdentity
			implements
			com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider.UsernamePassword {

		@Required
		@MinLength(5)
		protected String password;

		@Override
		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		@Override
		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}
	}

	public static class MySignup extends MyLogin {

		@Required
		@MinLength(5)
		private String repeatPassword;

		@Required
		private String name;

		public String validate() {
			if (password == null || !password.equals(repeatPassword)) {
				return "playauthenticate.password.signup.error.passwords_not_same";
			}
			return null;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getRepeatPassword() {
			return repeatPassword;
		}

		public void setRepeatPassword(String repeatPassword) {
			this.repeatPassword = repeatPassword;
		}
	}

	private final Provider<Application> appProvider;
	private final Form<MySignup> SIGNUP_FORM;
	private final Form<MyLogin> LOGIN_FORM;

	@Inject
	public MyUsernamePasswordAuthProvider(final Provider<Application> appProvider, final PlayAuthenticate auth, final FormFactory formFactory,
										  final ApplicationLifecycle lifecycle, MailerFactory mailerFactory) {
		super(auth, lifecycle, mailerFactory);
		this.appProvider = appProvider;

		this.SIGNUP_FORM = formFactory.form(MySignup.class);
		this.LOGIN_FORM = formFactory.form(MyLogin.class);
	}

	public Form<MySignup> getSignupForm() {
		return SIGNUP_FORM;
	}

	public Form<MyLogin> getLoginForm() {
		return LOGIN_FORM;
	}

	@Override
	protected MySignup getSignup(final Http.RequestHeader ctx) {
		// TODO change to getSignupForm().bindFromRequest(request) after 2.1
		Http.RequestHeader.current.set(ctx);
		final Form<MySignup> filledForm = getSignupForm().bindFromRequest();
		return filledForm.get();
	}

	@Override
	protected MyLogin getLogin(final Http.RequestHeader ctx) {
		// TODO change to getLoginForm().bindFromRequest(request) after 2.1
		Http.RequestHeader.current.set(ctx);
		final Form<MyLogin> filledForm = getLoginForm().bindFromRequest();
		return filledForm.get();
	}

	@Override
	protected com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider.SignupResult signupUser(final MyUsernamePasswordAuthUser user) {
		final User u = User.findByUsernamePasswordIdentity(user);
		if (u != null) {
			if (u.emailValidated) {
				// This user exists, has its email validated and is active
				return SignupResult.USER_EXISTS;
			} else {
				// this user exists, is active but has not yet validated its
				// email
				return SignupResult.USER_EXISTS_UNVERIFIED;
			}
		}
		// The user either does not exist or is inactive - create a new one
		@SuppressWarnings("unused")
		final User newUser = User.create(user);
		// Usually the email should be verified before allowing login, however
		// if you return
		// return SignupResult.USER_CREATED;
		// then the user gets logged in directly
		return SignupResult.USER_CREATED_UNVERIFIED;
	}

	@Override
	protected com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider.LoginResult loginUser(
			final MyLoginUsernamePasswordAuthUser authUser) {
		final User u = User.findByUsernamePasswordIdentity(authUser);
		if (u == null) {
			return LoginResult.NOT_FOUND;
		} else {
			if (!u.emailValidated) {
				return LoginResult.USER_UNVERIFIED;
			} else {
				for (final LinkedAccount acc : u.linkedAccounts) {
					if (getKey().equals(acc.providerKey)) {
						if (authUser.checkPassword(acc.providerUserId,
								authUser.getPassword())) {
							// Password was correct
							return LoginResult.USER_LOGGED_IN;
						} else {
							// if you don't return here,
							// you would allow the user to have
							// multiple passwords defined
							// usually we don't want this
							return LoginResult.WRONG_PASSWORD;
						}
					}
				}
				return LoginResult.WRONG_PASSWORD;
			}
		}
	}

	@Override
	protected Call userExists(final UsernamePasswordAuthUser authUser) {
		return routes.Signup.exists();
	}

	@Override
	protected Call userUnverified(final UsernamePasswordAuthUser authUser) {
		return routes.Signup.unverified();
	}

	@Override
	protected MyUsernamePasswordAuthUser buildSignupAuthUser(
			final MySignup signup, final Http.RequestHeader ctx) {
		return new MyUsernamePasswordAuthUser(signup);
	}

	@Override
	protected MyLoginUsernamePasswordAuthUser buildLoginAuthUser(
			final MyLogin login, final Http.RequestHeader ctx) {
		return new MyLoginUsernamePasswordAuthUser(login.getPassword(),
				login.getEmail());
	}


	@Override
	protected MyLoginUsernamePasswordAuthUser transformAuthUser(final MyUsernamePasswordAuthUser authUser, final Http.RequestHeader requestHeader) {
		return new MyLoginUsernamePasswordAuthUser(authUser.getEmail());
	}

	@Override
	protected String getVerifyEmailMailingSubject(
			final MyUsernamePasswordAuthUser user, final Http.RequestHeader ctx) {
		return "playauthenticate.password.verify_signup.subject";
	}

	@Override
	protected String onLoginUserNotFound(final Http.RequestHeader requestHeader) {
		requestHeader.flash()
				.put(controllers.Application.FLASH_ERROR_KEY,
						"playauthenticate.password.login.unknown_user_or_pw");
		return super.onLoginUserNotFound(requestHeader);
	}

	@Override
	protected Body getVerifyEmailMailingBody(final String token,
			final MyUsernamePasswordAuthUser user, final Http.RequestHeader ctx) {

		final boolean isSecure = getConfiguration().getBoolean(
				SETTING_KEY_VERIFICATION_LINK_SECURE);
		final String url = routes.Signup.verify(token).absoluteURL(
				ctx.request(), isSecure);

		final Lang lang = Lang.preferred(appProvider.get(), ctx.request().acceptLanguages());
		final String langCode = lang.code();

		final String html = getEmailTemplate(
				"views.html.account.signup.email.verify_email", langCode, url,
				token, user.getName(), user.getEmail());
		final String text = getEmailTemplate(
				"views.txt.account.signup.email.verify_email", langCode, url,
				token, user.getName(), user.getEmail());

		return new Body(text, html);
	}

	private static String generateToken() {
		return UUID.randomUUID().toString();
	}

	@Override
	protected String generateVerificationRecord(
			final MyUsernamePasswordAuthUser user) {
		return generateVerificationRecord(User.findByAuthUserIdentity(user));
	}

	protected String generateVerificationRecord(final User user) {
		final String token = generateToken();
		// Do database actions, etc.
		TokenAction.create(Type.EMAIL_VERIFICATION, token, user);
		return token;
	}

	protected String generatePasswordResetRecord(final User u) {
		final String token = generateToken();
		TokenAction.create(Type.PASSWORD_RESET, token, u);
		return token;
	}

	protected String getPasswordResetMailingSubject(final User user,
			final Http.RequestHeader ctx) {
		return "playauthenticate.password.reset_email.subject";
	}

	protected Body getPasswordResetMailingBody(final String token,
			final User user, final Http.RequestHeader ctx) {

		final boolean isSecure = getConfiguration().getBoolean(
				SETTING_KEY_PASSWORD_RESET_LINK_SECURE);
		final String url = routes.Signup.resetPassword(token).absoluteURL(
				ctx.request(), isSecure);

		final Lang lang = Lang.preferred(appProvider.get(), ctx.request().acceptLanguages());
		final String langCode = lang.code();

		final String html = getEmailTemplate(
				"views.html.account.email.password_reset", langCode, url,
				token, user.name, user.email);
		final String text = getEmailTemplate(
				"views.txt.account.email.password_reset", langCode, url, token,
				user.name, user.email);

		return new Body(text, html);
	}

	public void sendPasswordResetMailing(final User user, final Http.RequestHeader ctx) {
		final String token = generatePasswordResetRecord(user);
		final String subject = getPasswordResetMailingSubject(user, ctx);
		final Body body = getPasswordResetMailingBody(token, user, ctx);
		sendMail(subject, body, getEmailName(user));
	}

	public boolean isLoginAfterPasswordReset() {
		return getConfiguration().getBoolean(
				SETTING_KEY_LINK_LOGIN_AFTER_PASSWORD_RESET);
	}

	protected String getVerifyEmailMailingSubjectAfterSignup(final User user,
			final Http.RequestHeader ctx) {
		return "playauthenticate.password.verify_email.subject";
	}

	protected String getEmailTemplate(final String template,
			final String langCode, final String url, final String token,
			final String name, final String email) {
		Class<?> cls = null;
		String ret = null;
		try {
			cls = Class.forName(template + "_" + langCode);
		} catch (ClassNotFoundException e) {
			Logger.warn("Template: '"
					+ template
					+ "_"
					+ langCode
					+ "' was not found! Trying to use English fallback template instead.");
		}
		if (cls == null) {
			try {
				cls = Class.forName(template + "_"
						+ EMAIL_TEMPLATE_FALLBACK_LANGUAGE);
			} catch (ClassNotFoundException e) {
				Logger.error("Fallback template: '" + template + "_"
						+ EMAIL_TEMPLATE_FALLBACK_LANGUAGE
						+ "' was not found either!");
			}
		}
		if (cls != null) {
			Method htmlRender = null;
			try {
				htmlRender = cls.getMethod("render", String.class,
						String.class, String.class, String.class);
				ret = htmlRender.invoke(null, url, token, name, email)
						.toString();

			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

	protected Body getVerifyEmailMailingBodyAfterSignup(final String token,
			final User user, final Http.RequestHeader ctx) {

		final boolean isSecure = getConfiguration().getBoolean(
				SETTING_KEY_VERIFICATION_LINK_SECURE);
		final String url = routes.Signup.verify(token).absoluteURL(
				ctx.request(), isSecure);

		final Lang lang = Lang.preferred(appProvider.get(), ctx.request().acceptLanguages());
		final String langCode = lang.code();

		final String html = getEmailTemplate(
				"views.html.account.email.verify_email", langCode, url, token,
				user.name, user.email);
		final String text = getEmailTemplate(
				"views.txt.account.email.verify_email", langCode, url, token,
				user.name, user.email);

		return new Body(text, html);
	}

	public void sendVerifyEmailMailingAfterSignup(final User user,
			final Http.Request request) {

		final String subject = getVerifyEmailMailingSubjectAfterSignup(user,
		request);
		final String token = generateVerificationRecord(user);
		final Body body = getVerifyEmailMailingBodyAfterSignup(token, user, request);
		sendMail(subject, body, getEmailName(user));
	}

	private String getEmailName(final User user) {
		return getEmailName(user.email, user.name);
	}
}
