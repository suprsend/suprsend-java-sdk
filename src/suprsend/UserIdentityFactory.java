package suprsend;

public class UserIdentityFactory {
	Suprsend config;
	
	public UserIdentityFactory(Suprsend config) {
		this.config = config;
	}
	
	public UserIdentity newUserIdentity(String distinctID) throws SuprsendException {
		distinctID = distinctID.strip();
		if (distinctID == null) {
			throw new SuprsendException("distinct_id must be passed");
		}
		UserIdentity userIdentity = new UserIdentity(this.config, distinctID);
		return userIdentity;
	}
}
