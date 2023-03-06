package suprsend;

public class BulkSubscribersFactory {
    private final Suprsend config;

    BulkSubscribersFactory(Suprsend config) {
        this.config = config;
    }

    public BulkSubscribers newInstance() {
        return new BulkSubscribers(this.config);
    }

}
