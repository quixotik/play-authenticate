package models;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;

import play.data.validation.Constraints;

import io.ebean.DB;
import io.ebean.ExpressionList;
import io.ebean.Finder;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.feth.play.module.pa.user.EmailIdentity;
import com.feth.play.module.pa.user.NameIdentity;

@Entity
@Table(name = "users")
public class User extends AppModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	public Long id;

	@Constraints.Email
	// if you make this unique, keep in mind that users *must* merge/link their
	// accounts then on signup with additional providers
	// @Column(unique = true)
	public String email;

	public String name;

	public boolean active;

	public boolean emailValidated;

	@OneToMany(cascade = CascadeType.ALL)
	public List<LinkedAccount> linkedAccounts;

	public static final Finder<Long, User> find = new Finder<Long, User>(User.class);

	public static boolean existsByAuthUserIdentity(
			final AuthUserIdentity identity) {
		final ExpressionList<User> exp = getAuthUserFind(identity);
		return exp.findCount() > 0;
	}

	private static ExpressionList<User> getAuthUserFind(
			final AuthUserIdentity identity) {
		return find.query().where().eq("active", true)
				.eq("linkedAccounts.providerUserId", identity.getId())
				.eq("linkedAccounts.providerKey", identity.getProvider());
	}

	public static User findByAuthUserIdentity(final AuthUserIdentity identity) {
		if (identity == null) {
			return null;
		}
		return getAuthUserFind(identity).findOne();
	}

	public void merge(final User otherUser) {
		for (final LinkedAccount acc : otherUser.linkedAccounts) {
			this.linkedAccounts.add(LinkedAccount.create(acc));
		}
		// do all other merging stuff here - like resources, etc.

		// deactivate the merged user that got added to this one
		otherUser.active = false;
		DB.save(Arrays.asList(new User[] { otherUser, this }));
	}

	public static User create(final AuthUser authUser) {
		final User user = new User();
		user.active = true;
		user.linkedAccounts = Collections.singletonList(LinkedAccount
				.create(authUser));

		if (authUser instanceof EmailIdentity) {
			final EmailIdentity identity = (EmailIdentity) authUser;
			// Remember, even when getting them from FB & Co., emails should be
			// verified within the application as a security breach there might
			// break your security as well!
			user.email = identity.getEmail();
			user.emailValidated = false;
		}

		if (authUser instanceof NameIdentity) {
			final NameIdentity identity = (NameIdentity) authUser;
			final String name = identity.getName();
			if (name != null) {
				user.name = name;
			}
		}

		user.save();
		return user;
	}

	public static void merge(final AuthUser oldUser, final AuthUser newUser) {
		User.findByAuthUserIdentity(oldUser).merge(
				User.findByAuthUserIdentity(newUser));
	}

	public Set<String> getProviders() {
		final Set<String> providerKeys = new HashSet<String>(
				linkedAccounts.size());
		for (final LinkedAccount acc : linkedAccounts) {
			providerKeys.add(acc.providerKey);
		}
		return providerKeys;
	}

	public static void addLinkedAccount(final AuthUser oldUser,
			final AuthUser newUser) {
		final User u = User.findByAuthUserIdentity(oldUser);
		u.linkedAccounts.add(LinkedAccount.create(newUser));
		u.save();
	}
	
	public static User findByEmail(final String email) {
		return getEmailUserFind(email).findOne();
	}

	private static ExpressionList<User> getEmailUserFind(final String email) {
		return find.query().where().eq("active", true).eq("email", email);
	}

	public LinkedAccount getAccountByProvider(final String providerKey) {
		return LinkedAccount.findByProviderKey(this, providerKey);
	}

}
