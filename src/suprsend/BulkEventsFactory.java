package suprsend;

public class BulkEventsFactory {
	private final Suprsend config;

	BulkEventsFactory(Suprsend config) {
		this.config = config;
	}

	public BulkEvents newInstance() {
		return new BulkEvents(this.config);
	}
}
