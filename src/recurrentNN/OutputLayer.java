package recurrentNN;

import recurrentNN.matrix.*;

public class OutputLayer extends Layer {
	
	private ConnectionLayer inputs;
	
	protected OutputLayer(int layerSize, int batchSize, int activationCode) {
		super(layerSize, batchSize, activationCode, false);
	}
	
	protected Vector getOutputs() {
		return super.getRecentValues();
	}
	
	protected void setInputs(ConnectionLayer inputs) {
		this.inputs = inputs;
	}
	
	protected ConnectionLayer getInputs() {
		return this.inputs;
	}
}
