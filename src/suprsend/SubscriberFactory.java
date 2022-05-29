package suprsend;

public class SubscriberFactory {
	Suprsend config;

	SubscriberFactory(Suprsend config) {
		this.config = config;
	}

	public Subscriber getInstance(String distinctID) throws SuprsendException {
		if (distinctID == null || distinctID.trim().isEmpty()) {
			throw new SuprsendException("distinct_id must be passed");
		}
		distinctID = distinctID.trim();
		Subscriber subscriber = new Subscriber(this.config, distinctID);
		return subscriber;
	}
}
