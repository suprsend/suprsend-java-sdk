package suprsend;

public class BulkWorkflowsFactory {

    private final Suprsend config;

    BulkWorkflowsFactory(Suprsend config) {
        this.config = config;
    }

    public BulkWorkflows newInstance() {
        return new BulkWorkflows(this.config);
    }

}
