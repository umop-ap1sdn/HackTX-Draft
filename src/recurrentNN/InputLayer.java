package recurrentNN;


public class InputLayer extends Layer {
	
	private ConnectionLayer outputs;
	
	protected InputLayer(int layerSize, int batchSize, boolean bias) {
		super(layerSize, batchSize, Network.LINEAR, bias);
	}
	
	protected InputLayer(int layerSize, int batchSize, int activationCode, boolean bias) {
		super(layerSize, batchSize, activationCode, bias);
	}
	
	protected void setInputs(double[] inputs) {
		super.putValues(inputs);
		super.activate();
	}
	
	protected void setOutputs(ConnectionLayer outputs) {
		this.outputs = outputs;
	}
	
	protected ConnectionLayer getOutputs() {
		return this.outputs;
	}
}
