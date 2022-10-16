package recurrentNN;

import java.util.LinkedList;

import recurrentNN.matrix.Matrix;
import recurrentNN.matrix.Vector;

public class Layer {
	
	private LinkedList<Vector> histValues;
	private LinkedList<Vector> histErrors;
	private LinkedList<Vector> histDerivs;
	private Vector progressValues;
	protected int layerSize, trueSize, batchSize, activationCode;
	protected boolean bias;
	
	protected Layer(int layerSize, int batchSize, int activationCode, boolean bias) {
		this.layerSize = layerSize;
		this.trueSize = layerSize;
		this.batchSize = batchSize;
		this.activationCode = activationCode;
		this.bias = bias;
		
		if(bias) this.trueSize++;
		
		this.initialize();
	}
	
	private void initialize() {
		
		progressValues = new Vector(layerSize, Matrix.FILL_ZERO);
		histValues = new LinkedList<>();
		histErrors = new LinkedList<>();
		histDerivs = new LinkedList<>();
		
		for(int index = 0; index < batchSize; index++) {
			histValues.addLast(new Vector(layerSize, Matrix.FILL_ZERO));
			histErrors.addLast(new Vector(layerSize, Matrix.FILL_ZERO));
			histDerivs.addLast(new Vector(layerSize, Matrix.FILL_ZERO));
		}
	}
	
	protected void clearData() {
		this.initialize();
	}
	
	protected void putValues(Vector values) {
		try{
			this.progressValues = Matrix.getAsVector(Matrix.add(values, progressValues));
		} catch (Exception e) { }
		
	}
	
	protected void putValues(double[] arr) {
		Vector values = new Vector(arr);
		this.putValues(values);
	}
	
	protected void activate() {
		Vector tempVec0 = new Vector(progressValues);
		Vector tempVec1 = new Vector(progressValues);
		
		switch(activationCode) {
		case Network.LINEAR:
			tempVec0 = linear(tempVec0);
			tempVec1 = linearDeriv(tempVec1);
			break;
		case Network.RELU:
			tempVec0 = relu(tempVec0);
			tempVec1 = reluDeriv(tempVec1);
			break;
		case Network.SIGMOID:
			tempVec0 = sigmoid(tempVec0);
			tempVec1 = sigmoidDeriv(tempVec1);
			break;
		case Network.TANH:
			tempVec0 = tanh(tempVec0);
			tempVec1 = tanhDeriv(tempVec1);
			break;
		default:
			tempVec0 = linear(tempVec0);
			tempVec1 = linearDeriv(tempVec1);
			break;	
		}
		
		this.setValues(tempVec0);
		this.setDerivs(tempVec1);
		
		progressValues = new Vector(layerSize, Matrix.FILL_ZERO);
	}
	
	private Vector linear(Vector active) {
		
		for(int index = 0; index < active.getColumns(); index++) {
			
			try {
				double val = active.getValue(index);
				active.setValue(index, Functions.linear(val));
				
			} catch (Exception e) { }
		}
		
		return active;

	}
	
	private Vector relu(Vector active) {
		
		for(int index = 0; index < active.getColumns(); index++) {
			
			try {
				double val = active.getValue(index);
				active.setValue(index, Functions.relu(val));
			} catch (Exception e) { }
		}
		
		return active;
	}
	
	private Vector sigmoid(Vector active) {
		
		for(int index = 0; index < active.getColumns(); index++) {
			
			try {
				double val = active.getValue(index);
				active.setValue(index, Functions.sigmoid(val));
				
			} catch (Exception e) {
				System.out.println("Error");
				e.printStackTrace();
			}
		}
		
		return active;
	}
	
	private Vector tanh(Vector active) {
		
		for(int index = 0; index < active.getColumns(); index++) {
			
			try {
				double val = active.getValue(index);
				active.setValue(index, Functions.tanh(val));
				
			} catch (Exception e) { }
		}
		
		return active;

	}
	
	private Vector linearDeriv(Vector active) {
		
		for(int index = 0; index < active.getColumns(); index++) {
			
			try {
				double val = active.getValue(index);
				active.setValue(index, Functions.linearDeriv(val));
				
			} catch (Exception e) { }
		}
		
		return active;

	}
	
	private Vector reluDeriv(Vector active) {
		
		for(int index = 0; index < active.getColumns(); index++) {
			
			try {
				double val = active.getValue(index);
				active.setValue(index, Functions.reluDeriv(val));
			} catch (Exception e) { }
		}
		
		return active;
	}
	
	private Vector sigmoidDeriv(Vector active) {
		
		for(int index = 0; index < active.getColumns(); index++) {
			
			try {
				double val = active.getValue(index);
				active.setValue(index, Functions.sigmoidDeriv(val));
				
			} catch (Exception e) { }
		}
		
		return active;
	}
	
	private Vector tanhDeriv(Vector active) {
		
		for(int index = 0; index < active.getColumns(); index++) {
			
			try {
				double val = active.getValue(index);
				active.setValue(index, Functions.tanhDeriv(val));
				
			} catch (Exception e) { }
		}
		
		return active;

	}
	
	private void setValues(Vector values) {
		histValues.addLast(values);
		if(histValues.size() > batchSize) {
			histValues.pollFirst();
		}
	}
	
	private void setDerivs(Vector derivs) {
		histDerivs.addLast(derivs);
		if(histDerivs.size() > batchSize) {
			histDerivs.pollFirst();
		}
	}
	
	protected void setErrors(Vector errors) {
		histErrors.addLast(errors);
		if(histErrors.size() > batchSize) {
			histErrors.pollFirst();
		}
		
	}
	
	protected void setErrors(double[] arr) {
		Vector errors = new Vector(arr);
		this.setErrors(errors);
	}
	
	protected void setErrors(double[] arr, int index) {
		Vector errors = new Vector(arr);
		
		this.setErrors(errors, index);
	}
	
	protected void setErrors(Vector errors, int index) {
		histErrors.set(index, errors);
	}
	
	protected Vector getRecentValues() {
		return histValues.getLast();
	}
	
	protected Vector getTrueValues(int batch) {
		if(!bias) return getValues().get(batch);
		double[] tArr = getValues().get(batch).getTArray();
		double[] ret = new double[tArr.length + 1];
		
		for(int index = 0; index < tArr.length; index++) {
			ret[index] = tArr[index];
			ret[index + 1] = 1;
		}
		
		return new Vector(ret);
	}
	
	protected Vector getRecentTrueValues() {
		return getTrueValues(batchSize - 1);
	}
	
	protected Vector getRecentDerivs() {
		return histDerivs.getLast();
	}
	
	protected Vector getRecentErrors() {
		return histErrors.getLast();
	}
	
	protected LinkedList<Vector> getValues(){
		return this.histValues;
	}
	
	protected LinkedList<Vector> getDerivValues(){
		return this.histDerivs;
	}
	
	protected LinkedList<Vector> getErrors(){
		return this.histErrors;
	}
}
