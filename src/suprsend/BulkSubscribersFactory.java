package suprsend;

public class BulkSubscribersFactory {
    private final Suprsend config;

    public BulkSubscribersFactory(Suprsend config) {
        this.config = config;
    }

    public BulkSubscribers getInstance() {
        return new BulkSubscribers(config);
    }

}