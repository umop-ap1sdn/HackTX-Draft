package recurrentNN;

import recurrentNN.matrix.*;

public class Network {
	
	public static final int TANH = 1;
	public static final int SIGMOID = 2;
	public static final int RELU = 3;
	public static final int LINEAR = 4;
	
	public static final int INPUT_LAYER = 5;
	public static final int HIDDEN_LAYER = 6;
	public static final int RECURRENT_LAYER = 7;
	public static final int OUTPUT_LAYER = 8;
	
	private int batchSize, trainSize;
	private int nLayersSize, cLayersSize, rConnectsSize;
	private int trainIndex;
	private int outputIndex, inputIndex = 0;
	
	private double LEARNING_RATE = 0.0001;
	
	private double loss, recentLoss;
	
	private Layer[] nLayers;
	private ConnectionLayer[] cLayers;
	private RConnectionLayer[] rConnects;
	
	private double[][][] dataset;
	private int datasetSize;
	
	protected Network(Layer[] nLayers, ConnectionLayer[] cLayers, RConnectionLayer[] rConnects, int batchSize, int trainSize) {
		this.nLayers = nLayers;
		this.cLayers = cLayers;
		this.rConnects = rConnects;
		this.batchSize = batchSize;
		this.trainSize = trainSize;
		
		nLayersSize = nLayers.length;
		cLayersSize = cLayers.length;
		rConnectsSize = rConnects.length;
		
		loss = 0;
		recentLoss = 0;
		trainIndex = 0;
		
		outputIndex = nLayersSize - 1;
	}
	
	public void uploadDataset(double[][][] dataset) {
		this.dataset = dataset;
		datasetSize = dataset.length;
	}
	
	public void setLearningRate(double lr) {
		this.LEARNING_RATE = lr;
	}
	
	public void algorithmicLR(double maximum, double target, double bottom, double bias) {
		this.LEARNING_RATE = (maximum * Math.tanh(Math.max(this.getNodeLoss() - target, bottom))) + bias;
	}
	
	public double[][][] train(boolean backProp) {
		double[][][] ret = new double[trainSize][2][nLayers[outputIndex].layerSize];
		
		for(int count = 0; count < trainSize; count++) {
			ret[count][0] = testInputs(dataset[trainIndex][0]);
			ret[count][1] = determineOutputErrors(dataset[trainIndex][1]);
			trainIndex++;
			
			trainIndex %= datasetSize;
		}
		
		calculateTotalLoss();
		calculateRecentLoss();
		
		if(backProp) {
			multiLayerError();
			adjustWeights();
		}
		
		return ret;
	}
	
	private double[] determineOutputErrors(double[] targets) {
		try {
			Vector outputErrors = new Vector(nLayers[outputIndex].getRecentValues());
			Vector targetInv = Vector.getAsVector(Vector.scale(new Vector(targets), -1));
			
			//System.out.println(targetInv + "\n");
			
			//System.out.println(outputErrors);
			//System.out.println(outputInv);
			
			
			outputErrors = Vector.getAsVector(Vector.add(outputErrors, targetInv));
			double[] ret = outputErrors.getTArray();
			//System.out.println(outputErrors + "\n");
			
			Vector derivs = nLayers[outputIndex].getRecentDerivs();
			
			for(int index = 0; index < outputErrors.getColumns(); index++) {
				double newVal = derivs.getValue(index) * outputErrors.getValue(index);
				outputErrors.setValue(index, newVal);
			}
			
			nLayers[outputIndex].setErrors(outputErrors);
			
			return ret;
		} catch (Exception e) { 
			return null;
		}
		
		
	}
	
	private void multiLayerError() {
		
		for(int batch = 0; batch < batchSize; batch++) {
			int rConnectsIndex = rConnectsSize;
			for(int layer = outputIndex - 1; layer > inputIndex; layer--) {
				double[] errorVec = new double[nLayers[layer].layerSize];
				double[] futureErrorVec = new double[nLayers[layer].layerSize];
				
				for(int index = 0; index < errorVec.length; index++) {
					errorVec[index] = calcHiddenError(batch, layer, index);
					if(batch < batchSize - 1) futureErrorVec[index] = calcHiddenError(batch + 1, layer, index);
				}
				
				if(nLayers[layer] instanceof RecurrentLayer && batch < (batchSize - 1)) {
					Vector linError = new Vector(errorVec);
					rConnectsIndex--;
					double[] timeVec = new double[nLayers[layer].layerSize];
					
					for(int index = 0; index < errorVec.length; index++) {
						timeVec[index] = calcRecurrentError(batch, layer, index, rConnectsIndex, futureErrorVec);
					}
					
					Vector timeError = new Vector(timeVec);
					
					try {
						Vector totalError = Vector.getAsVector(Vector.add(linError, timeError));
						nLayers[layer].setErrors(totalError, batch);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				} else {
					nLayers[layer].setErrors(errorVec, batch);
				}
			}
		}
	}
	
	private double calcHiddenError(int batch, int layer, int index) {
		Vector errors;
		errors = nLayers[layer + 1].getErrors().get(batch);
		
		double sum = 0;
		
		Matrix connections = ((HiddenLayer) nLayers[layer]).getOutputs().getMatrix();
		
		try {
			for(int ind = 0; ind < errors.getColumns(); ind++) {
				sum += errors.getValue(ind) * connections.getVal(ind, index);
			}
			
			return sum * nLayers[layer].getDerivValues().get(batch).getValue(index);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	private double calcRecurrentError(int batch, int layer, int index, int rConnectsLayer, double[] futureError) {
		
		Vector errors = new Vector(futureError);
		double sum = 0;
		
		try {
		
			for(int ind = 0; ind < errors.getColumns(); ind++) {
				sum += errors.getValue(ind) * rConnects[rConnectsLayer].getMatrix().getVal(ind, index);
			}
			
			return sum * nLayers[layer].getDerivValues().get(batch).getValue(index);
			
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public double[] testInputs(double... inputs) {
		nLayers[0].putValues(inputs);
		nLayers[0].activate();
		
		forwardPropagate();
		
		return nLayers[nLayersSize - 1].getRecentValues().getTArray();
	}
	
	private void forwardPropagate() {
		
		int recurrentIndex = 0;
		
		for(int index = 0; index < cLayersSize; index++) {
			cLayers[index].passForward();
			
			if(nLayers[index + 1] instanceof RecurrentLayer) {
				rConnects[recurrentIndex].passForward();
				recurrentIndex++;
			}
			
			nLayers[index + 1].activate();
		}
	}
	
	private void adjustWeights() {
		for(int batch = batchSize - 1; batch >= 0; batch--) {
			
			for(int index = cLayersSize - 1; index >= 0; index--) {
				
				Vector layerOut = cLayers[index].getInput().getTrueValues(batch);
				Vector layerError = cLayers[index].getOutput().getErrors().get(batch);
				
				double[][] gradients = fillGradients(layerOut, layerError);
				cLayers[index].adjustWeights(gradients);
			}
			
			if(batch > batchSize - 2) continue;
			
			for(int index = rConnectsSize - 1; index >= 0; index--) {
				
				Vector layerOut = rConnects[index].getInput().getTrueValues(batch);
				Vector layerError = rConnects[index].getOutput().getErrors().get(batch + 1);
				
				double[][] gradients = fillGradients(layerOut, layerError);
				rConnects[index].adjustWeights(gradients);
			}
		}
	}
	
	private double[][] fillGradients(Vector layerOut, Vector layerError){
		
		double[][] gradients = new double[layerError.getColumns()][layerOut.getColumns()];
		
		for(int row = 0; row < layerError.getColumns(); row++) {
			for(int col = 0; col < layerOut.getColumns(); col++) {
				try {
					gradients[row][col] = -LEARNING_RATE * layerError.getValue(row) * layerOut.getValue(col);
				} catch (Exception e) { }
			}
		}
		
		return gradients;
	}
	
	public void calculateTotalLoss() {
		Vector lossVector;
		double sum = 0;
		for(int index = 0; index < batchSize; index++) {
			lossVector = nLayers[outputIndex].getErrors().get(index);
			for(int indey = 0; indey < lossVector.getColumns(); indey++) {
				try {
					sum += lossFunction(lossVector.getValue(indey));
				} catch (Exception e) { }
			}
		}
		
		this.loss = sum;
	}
	
	public void calculateRecentLoss() {
		Vector loss = nLayers[outputIndex].getRecentErrors();
		double sum = 0;
		for(int index = 0; index < loss.getColumns(); index++) {
			try {
				sum += lossFunction(loss.getValue(index));
			} catch (Exception e) { }
		}
		
		this.recentLoss = sum;
	}
	
	public double getTotalLoss() {
		return this.loss;
	}
	
	public double getNodeLoss() {
		return getTotalLoss() / nLayers[outputIndex].layerSize;
	}
	
	public double getRecentLoss() {
		return this.recentLoss;
	}
	
	private double lossFunction(double e) {
		return Math.pow(e, 2) / 2;
	}
	
	protected String[] getLayerData() {
		String[] ret = new String[nLayersSize];
		
		for(int index = 0; index < nLayersSize; index++) {
			Layer n = nLayers[index];
			int size = n.layerSize;
			int activation = n.activationCode;
			int bias = n.bias ? 1 : 0;
			int layerType = layerType(n);
			
			
			String lyr = String.format("%d,%d,%d,%d", size, activation, layerType, bias);
			ret[index] = lyr;
		}
		
		return ret;
	}
	
	protected String getConnectionData(int index, boolean recurrent) {
		if(recurrent) return rConnects[index].getMatrix().dataString();
		else return cLayers[index].getMatrix().dataString();
	}
	
	private int layerType(Layer n) {
		if(n instanceof InputLayer) return Network.INPUT_LAYER;
		if(n instanceof RecurrentLayer) return Network.RECURRENT_LAYER;
		if(n instanceof HiddenLayer) return Network.HIDDEN_LAYER;
		if(n instanceof OutputLayer) return Network.OUTPUT_LAYER;
		else return 0;
	}
	
	protected void writeConnection(double[][] arr, int index, boolean recurrent) {
		Matrix mat = new Matrix(arr);
		if(recurrent) rConnects[index].setMatrix(mat);
		else cLayers[index].setMatrix(mat);
	}
	
	public static final int NLAYERS_INDEX = 0;
	public static final int CLAYERS_INDEX = 1;
	public static final int RCONNECTS_INDEX = 2;
	
	private final int SIZE = 3;
	
	protected int[] getSizes() {
		int[] ret = new int[SIZE];
		ret[NLAYERS_INDEX] = nLayersSize;
		ret[CLAYERS_INDEX] = cLayersSize;
		ret[RCONNECTS_INDEX] = rConnectsSize;
		
		return ret;
	}
	
	public void clearData() {
		for(Layer n: nLayers) {
			n.clearData();
		}
	}
}
