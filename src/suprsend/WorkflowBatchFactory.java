package suprsend;

public class WorkflowBatchFactory {
	Suprsend config;
	
	public WorkflowBatchFactory(Suprsend config) {
		this.config = config;
	}
	
	public WorkflowBatch newWorkflowBatch() {
		return new WorkflowBatch(this.config);
	}
}
