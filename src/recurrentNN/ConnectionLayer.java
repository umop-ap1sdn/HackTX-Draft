package recurrentNN;

import recurrentNN.matrix.*;

public class ConnectionLayer {
	
	private Matrix layer;
	private Layer input;
	private Layer output;
	
	protected ConnectionLayer(Layer input, Layer output) {
		this.input = input;
		this.output = output;
		
		this.initialize();
	}
	
	protected void initialize() {
		layer = new Matrix(output.layerSize, input.trueSize, Matrix.FILL_RANDOM);
		
		if(input instanceof InputLayer) {
			((InputLayer)input).setOutputs(this);
		} else if(input instanceof HiddenLayer) {
			((HiddenLayer)input).setOutputs(this);
		}
		
		
		if(output instanceof HiddenLayer) {
			((HiddenLayer)output).setInputs(this);
		} else if(output instanceof OutputLayer) {
			((OutputLayer)output).setInputs(this);
		}
		
	}
	
	protected void passForward() {
		
		try {
			Matrix result = Matrix.multiply(layer, input.getRecentTrueValues());
			output.putValues(Matrix.getAsVector(result));
		} catch (Exception e) { }
	}
	
	protected Matrix getMatrix() {
		return this.layer;
	}
	
	protected void adjustWeights(Matrix gradients) {
		try {
			this.layer = Matrix.add(gradients, this.layer);
		} catch (Exception e) { }
		
	}
	
	protected void adjustWeights(double[][] gradients) {
		this.adjustWeights(new Matrix(gradients));
	}
	
	protected void setMatrix(Matrix layer) {
		this.layer = layer;
	}
	
	protected Layer getOutput() {
		return this.output;
	}
	
	protected Layer getInput() {
		return this.input;
	}
}
