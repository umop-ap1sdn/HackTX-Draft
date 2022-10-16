package recurrentNN;


public class HiddenLayer extends Layer {
	
	private ConnectionLayer inputs;
	private ConnectionLayer outputs;
	
	protected HiddenLayer(int layerSize, int batchSize, int activationCode, boolean bias) {
		super(layerSize, batchSize, activationCode, bias);
	}
	
	protected void setInputs(ConnectionLayer inputs) {
		this.inputs = inputs;
	}
	
	protected ConnectionLayer getInputs() {
		return this.inputs;
	}
	
	protected void setOutputs(ConnectionLayer outputs) {
		this.outputs = outputs;
	}
	
	protected ConnectionLayer getOutputs() {
		return this.outputs;
	}
}
