package models;

import be.objectify.deadbolt.java.models.Permission;
import io.ebean.Finder;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Initial version based on work by Steve Chaloner (steve@objectify.be) for
 * Deadbolt2
 */
@Entity
public class UserPermission extends AppModel implements Permission {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	public Long id;

	public String value;

	public static final Finder<Long, UserPermission> find = new Finder<>(UserPermission.class);

	public String getValue() {
		return value;
	}

	public static UserPermission findByValue(String value) {
		return find.query().where().eq("value", value).findOne();
	}
}
