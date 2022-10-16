package recurrentNN;

import java.io.File;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Scanner;

public class NetworkBuilder {
	
	private ArrayList<Layer> layers;
	private ArrayList<Integer> rLayers;
	
	private Layer[] nLayers;
	private ConnectionLayer[] cLayers;
	private RConnectionLayer[] rConnects;
	
	private int nLayerSize, cLayerSize, rConnectsSize;
	private int batchSize, trainSize;
	
	private boolean allowInputLayer, allowHiddenLayer, allowOutputLayer, allowFinalize;
	
	public NetworkBuilder(int batchSize) {
		layers = new ArrayList<>();
		rLayers = new ArrayList<>();
		
		nLayerSize = 0;
		cLayerSize = 0;
		rConnectsSize = 0;
		
		allowInputLayer = true;
		allowHiddenLayer = false;
		allowOutputLayer = false;
		allowFinalize = false;
		
		this.batchSize = batchSize;
	}
	
	public boolean putLayer(int layerSize, int activationCode, int layerType, boolean bias) {
		switch(layerType) {
		case Network.INPUT_LAYER:
			return putInputLayer(layerSize, activationCode, bias);
		case Network.HIDDEN_LAYER:
			return putHiddenLayer(layerSize, activationCode, bias);
		case Network.RECURRENT_LAYER:
			return putRecurrentLayer(layerSize, activationCode, bias);
		case Network.OUTPUT_LAYER:
			return putOutputLayer(layerSize, activationCode);
		default:
			return false;
		}
	}
	
	private boolean putInputLayer(int layerSize, int activationCode, boolean bias) {
		if(!allowInputLayer) return false;
		
		layers.add(new InputLayer(layerSize, batchSize, activationCode, bias));
		nLayerSize++;
		
		allowInputLayer = false;
		allowHiddenLayer = true;
		allowOutputLayer = true;
		
		return true;
	}
	
	private boolean putHiddenLayer(int layerSize, int activationCode, boolean bias) {
		if(!allowHiddenLayer) return false;
		
		layers.add(new HiddenLayer(layerSize, batchSize, activationCode, bias));
		nLayerSize++;
		cLayerSize++;
		
		return true;
	}
	
	private boolean putRecurrentLayer(int layerSize, int activationCode, boolean bias) {
		if(!allowHiddenLayer) return false;
		
		layers.add(new RecurrentLayer(layerSize, batchSize, activationCode, bias));
		rLayers.add(nLayerSize);
		nLayerSize++;
		cLayerSize++;
		rConnectsSize++;
		
		return true;
	}
	
	private boolean putOutputLayer(int layerSize, int activationCode) {
		if(!allowOutputLayer) return false;
		
		layers.add(new OutputLayer(layerSize, batchSize, activationCode));
		nLayerSize++;
		cLayerSize++;
		
		allowHiddenLayer = false;
		allowOutputLayer = false;
		
		allowFinalize = true;
		
		return true;
	}
	
	public Network finalize(int trainSize) {
		if(!allowFinalize) return null;
		
		this.trainSize = trainSize;
		
		nLayers = new Layer[nLayerSize];
		cLayers = new ConnectionLayer[cLayerSize];
		rConnects = new RConnectionLayer[rConnectsSize];
		
		for(int index = 0; index < nLayerSize; index++) {
			nLayers[index] = layers.get(index);
		}
		
		for(int index = 0; index < cLayerSize; index++) {
			cLayers[index] = new ConnectionLayer(nLayers[index], nLayers[index + 1]);
		}
		
		for(int index = 0; index < rConnectsSize; index++) {
			rConnects[index] = new RConnectionLayer((RecurrentLayer)nLayers[rLayers.get(index)]);
		}
		
		return new Network(nLayers, cLayers, rConnects, batchSize, trainSize);
	}
	
	public void writeFile(File path, File file, Network rnn) {
		Formatter fileWriter;
		
		if(!path.exists()) path.mkdirs();
		
		try {
			fileWriter = new Formatter(file);
			fileWriter.format("%d,%d,%d%n", layers.size(), batchSize, trainSize);
			
			String[] layerData = rnn.getLayerData();
			
			for(int index = 0; index < layerData.length; index++) {
				fileWriter.format("%s%n", layerData[index]);
			}
			
			int[] sizes = rnn.getSizes();
			
			for(int index = 0; index < sizes[Network.CLAYERS_INDEX]; index++) {
				fileWriter.format("%s", rnn.getConnectionData(index, false));
				fileWriter.format("%n%s%n", "-");
			}
			
			for(int index = 0; index < sizes[Network.RCONNECTS_INDEX]; index++) {
				fileWriter.format("%s", rnn.getConnectionData(index, true));
				fileWriter.format("%n%s", "-");
				if(index < sizes[Network.RCONNECTS_INDEX] - 1) fileWriter.format("%n");
			}
			
			fileWriter.close();
			
		} catch (Exception e) { }
	}
	
	public void writeFile(String path, String fileName, Network rnn, boolean count) {
		
		int identifier = 0;
		String name = fileName + identifier;
		
		while(new File(path + name + ".rnn").exists() && count) {
			identifier++;
			name = fileName + identifier;
		}
		name += ".rnn";
		writeFile(new File(path), new File(path + name), rnn);
	}
	
	public Network writeFromFile(File network) {
		try{
			Scanner sc = new Scanner(network);
			String line = sc.nextLine();
			String[] vals = line.split(",");
			int[] valuesInt = StringtoInt(vals);
			
			this.batchSize = valuesInt[1];
			this.trainSize = valuesInt[2];
			
			int numLayers = valuesInt[0];
			
			for(int count = 0; count < numLayers; count++) {
				line = sc.nextLine();
				vals = line.split(",");
				valuesInt = StringtoInt(vals);
				this.putLayer(valuesInt[0], valuesInt[1], valuesInt[2], valuesInt[3] == 1);
			}
			
			Network rnn = finalize(trainSize);
			
			
			for(int count = 0; count < 2; count++) {
				int layerSize;
				boolean recurrent = false;
				ConnectionLayer[] conLayer;
				if(count == 0) {
					layerSize = cLayerSize;
					recurrent = false;
					conLayer = cLayers;
				} else {
					layerSize = rConnectsSize;
					recurrent = true;
					conLayer = rConnects;
				}
				
				
				for(int index = 0; index < layerSize; index++) {
					double[][] matrix = new double[conLayer[index].getMatrix().getColumns()][conLayer[index].getMatrix().getRows()];
					
					line = sc.nextLine();
					vals = line.split(",");
					
					int ind = 0;
					
					while(!line.equals("-")) {
						double[] values = StringtoDouble(vals);
						matrix[ind] = values;
						
						
						line = sc.nextLine();
						vals = line.split(",");
						
						ind++;
					}
					
					rnn.writeConnection(matrix, index, recurrent);
				}
				
			}
			
			sc.close();
			//this.writeFile("files/", "test", rnn);
			
			return rnn;
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private int[] StringtoInt(String[] arr) {
		int[] ret = new int[arr.length];
		
		for(int index = 0; index < ret.length; index++) {
			ret[index] = Integer.parseInt(arr[index]);
		}
		
		return ret;
	}
	
	private double[] StringtoDouble(String[] arr) {
		double[] ret = new double[arr.length];
		
		for(int index = 0; index < ret.length; index++) {
			ret[index] = Double.parseDouble(arr[index]);
		}
		
		return ret;
	}
	
	public Network writeFromFile(String file) {
		return writeFromFile(new File(file));
	}
}
