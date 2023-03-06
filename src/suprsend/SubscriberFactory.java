package suprsend;

public class SubscriberFactory {
	private final Suprsend config;

	SubscriberFactory(Suprsend config) {
		this.config = config;
	}

	public Subscriber getInstance(String distinctId) throws SuprsendException {
		if (distinctId == null || distinctId.trim().isEmpty()) {
			throw new SuprsendException("distinct_id must be passed");
		}
		distinctId = distinctId.trim();
		return new Subscriber(this.config, distinctId);
	}
}
