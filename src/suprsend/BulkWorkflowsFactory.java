package suprsend;

public class BulkWorkflowsFactory {

    private Suprsend config;

    public BulkWorkflowsFactory(Suprsend config) {
        this.config = config;
    }

    public BulkWorkflows getInstance() {
        return new BulkWorkflows(config);
    }

}
