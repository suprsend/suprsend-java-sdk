package suprsend;

class BulkEventsFactory {
    private Suprsend config;

    public BulkEventsFactory(Suprsend config) {
        this.config = config;
    }

    public BulkEvents getInstance() {
        return new BulkEvents(config);
    }
}

