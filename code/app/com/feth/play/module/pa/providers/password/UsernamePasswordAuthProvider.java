package com.feth.play.module.pa.providers.password;

import org.apache.pekko.actor.Cancellable;

import com.feth.play.module.mail.IMailer;
import com.feth.play.module.mail.Mailer;
import com.feth.play.module.mail.Mailer.Mail;
import com.feth.play.module.mail.Mailer.Mail.Body;
import com.feth.play.module.mail.Mailer.MailerFactory;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.exceptions.AuthException;
import com.feth.play.module.pa.providers.AuthProvider;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.NameIdentity;

import play.inject.ApplicationLifecycle;
import play.mvc.Call;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.RequestHeader;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

public abstract class UsernamePasswordAuthProvider<R, UL extends UsernamePasswordAuthUser, US extends UsernamePasswordAuthUser, L extends UsernamePasswordAuthProvider.UsernamePassword, S extends UsernamePasswordAuthProvider.UsernamePassword>
		extends AuthProvider {

	public static final String PROVIDER_KEY = "password";

	protected static final String SETTING_KEY_MAIL = "mail";

	private static final String SETTING_KEY_MAIL_FROM_EMAIL = Mailer.SettingKeys.FROM_EMAIL;

	private static final String SETTING_KEY_MAIL_DELAY = Mailer.SettingKeys.DELAY;

	private static final String SETTING_KEY_MAIL_FROM = Mailer.SettingKeys.FROM;

	@Override
	protected List<String> neededSettingKeys() {
		return Arrays.asList(SETTING_KEY_MAIL + "." + SETTING_KEY_MAIL_DELAY,
				SETTING_KEY_MAIL + "." + SETTING_KEY_MAIL_FROM + "."
						+ SETTING_KEY_MAIL_FROM_EMAIL);
	}

	protected IMailer mailer;
	protected MailerFactory mailerFactory;

	private enum Case {
		SIGNUP, LOGIN
	}

	protected enum SignupResult {
		USER_EXISTS, USER_CREATED_UNVERIFIED, USER_CREATED, USER_EXISTS_UNVERIFIED
	}

	protected enum LoginResult {
		USER_UNVERIFIED, USER_LOGGED_IN, NOT_FOUND, WRONG_PASSWORD
	}

	public static interface UsernamePassword {

		public String getEmail();

		public String getPassword();
	}

	@Inject
	public UsernamePasswordAuthProvider(final PlayAuthenticate auth, final ApplicationLifecycle lifecycle, final MailerFactory mailerFactory) {
		super(auth, lifecycle);
		this.mailerFactory = mailerFactory;
		mailer = mailerFactory.create(getConfiguration().getConfig(
				SETTING_KEY_MAIL));
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	public String getKey() {
		return PROVIDER_KEY;
	}

	@Override
	public Object authenticate(final Http.Request request, final Object payload)
			throws AuthException {

		if (payload == Case.SIGNUP) {
			final S signup = getSignup(request);
			final US authUser = buildSignupAuthUser(signup, request);
			final SignupResult r = signupUser(authUser);

			switch (r) {
			case USER_EXISTS:
				// The user exists already
				return userExists(authUser).url();
			case USER_EXISTS_UNVERIFIED:
			case USER_CREATED_UNVERIFIED:
				// User got created as unverified
				// Send validation email
				sendVerifyEmailMailing(request, authUser);
				return userUnverified(authUser).url();
			case USER_CREATED:
				// continue to login...
				return transformAuthUser(authUser, request);
			default:
				throw new AuthException("Something in signup went wrong");
			}
		} else if (payload == Case.LOGIN) {
			final L login = getLogin(request);
			final UL authUser = buildLoginAuthUser(login, request);
			final LoginResult r = loginUser(authUser);
			switch (r) {
			case USER_UNVERIFIED:
				// The email of the user is not verified, yet - we won't allow
				// him to log in
				return userUnverified(authUser).url();
			case USER_LOGGED_IN:
				// The user exists and the given password was correct
				return authUser;
			case WRONG_PASSWORD:
				// don't expose this - it might harm users privacy if anyone
				// knows they signed up for our service
			case NOT_FOUND:
				// forward to login page
				return onLoginUserNotFound(request);
			default:
				throw new AuthException("Something in login went wrong");
			}
		} else {
			return this.auth.getResolver().login().url();
		}
	}

	protected Result onLoginUserNotFound(Http.Request request) {
		return Controller.redirect(this.auth.getResolver().login());
	}

	public Result handleLogin(final Http.Request request) {
		return this.auth.handleAuthentication(PROVIDER_KEY, request,
				Case.LOGIN);
	}

	@Override
	public AuthUser getSessionAuthUser(final String id, final long expires) {
		return new SessionUsernamePasswordAuthUser(getKey(), id, expires);
	}

	public Result handleSignup(final Http.Request request) {
		return this.auth.handleAuthentication(PROVIDER_KEY, request, Case.SIGNUP);
	}

	/**
	 * You might overwrite this to provide your own recipient format
	 * implementation,
	 * however the default should be fine for most cases
	 *
	 * @param user
	 * @return
	 */
	protected String getEmailName(final US user) {
		String name = null;
		if (user instanceof NameIdentity) {
			name = ((NameIdentity) user).getName();
		}

		return getEmailName(user.getEmail(), name);
	}

	protected String getEmailName(final String email, final String name) {
		return Mailer.getEmailName(email, name);
	}

	protected abstract R generateVerificationRecord(final US user);

	protected void sendVerifyEmailMailing(final Http.Request request, final US user) {
		final String subject = getVerifyEmailMailingSubject(user, request);
		final R record = generateVerificationRecord(user);
		final Body body = getVerifyEmailMailingBody(record, user, request);
		sendMail(subject, body, getEmailName(user));
	}

	/**
	 * Called to send mails. You might want to override this in order to
	 * customize mail sending e.g. by using a different mailer service
	 * implementation.
	 *
	 * @param subject
	 *            The mail's subject.
	 * @param body
	 *            The mail's body.
	 * @param recipient
	 *            The (formatted) recipient.
	 * @return The {@link org.apache.pekko.actor.Cancellable} that can be used to cancel the
	 *         action.
	 */
	protected Cancellable sendMail(final String subject, final Body body,
			final String recipient) {
		return sendMail(new Mail(subject, body, recipient));
	}

	/**
	 * Send a pre-assembled mail.
	 *
	 * @param mail
	 *            The mail to be sent.
	 * @return The {@link org.apache.pekko.actor.Cancellable} that can be used to cancel the
	 *         action.
	 */
	protected Cancellable sendMail(final Mail mail) {
		return mailer.sendMail(mail);
	}

	@Override
	public boolean isExternal() {
		return false;
	}

	protected abstract String getVerifyEmailMailingSubject(final US user,
			final Http.Request request);

	protected abstract Body getVerifyEmailMailingBody(
			final R verificationRecord, final US user, final Http.Request request);

	protected abstract UL buildLoginAuthUser(final L login, final Http.Request request);

	/**
	 * This gets called when the user shall be logged in directly after signing up
	 *
	 * @param authUser
	 * @param requestHeader
	 * @return
	 */
	protected abstract UL transformAuthUser(final US authUser, final Http.Request request);

	protected abstract US buildSignupAuthUser(final S signup, final Http.Request request);

	protected abstract LoginResult loginUser(final UL authUser);

	protected abstract SignupResult signupUser(final US user);

	protected abstract S getSignup(final Http.Request request);

	protected abstract L getLogin(final Http.Request request);

	protected abstract Call userExists(final UsernamePasswordAuthUser authUser);

	protected abstract Call userUnverified(
			final UsernamePasswordAuthUser authUser);

}
