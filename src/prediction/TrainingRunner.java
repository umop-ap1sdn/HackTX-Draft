package prediction;

import java.io.File;

import recurrentNN.*;
import window.*;

public class TrainingRunner {
	
	File[] trainFiles;
	String presetPath;
	
	int iterations, files;
	
	NetworkBuilder[] constructors;
	Network[] rnn;
	TrainingOrder parent;
	
	final int inSize = 1, outSize = 1, datasize = 5;
	
	final int batchSize = 12, backSize = 36;
	
	double[][][] dataset;
	
	Thread[] threads;
	TrainingThread[] trainingSets;
	
	String coin, gran;
	
	int threadIndex;
	
	public TrainingRunner(File trainingFile, boolean trainAll, boolean multiPreset, int iterations, int files, String coin, String gran) {
		
		presetPath = null;
		
		if(!trainAll) {
			this.trainFiles = new File[]{trainingFile};
			
		}
		else {
			String[] list = trainingFile.list();
			trainFiles = new File[list.length];
			
			for(int index = 0; index < list.length; index++) {
				trainFiles[index] = new File(trainingFile.getPath() + "/" + list[index]);
				//System.out.println(trainFiles[index].getPath());
			}
		}
		
		constructors = new NetworkBuilder[trainFiles.length];
		rnn = new Network[trainFiles.length];
		
		//constructor = new NetworkBuilder(networkInputs, networkHidden, networkOutputs, batchSize, backSize);
		//rnn = constructor.construct();
		
		for(int index = 0; index < constructors.length; index++) {
			
			if(multiPreset) {
				try {
					constructors[index] = new NetworkBuilder(backSize);
					rnn[index] = constructors[index].writeFromFile(findRecent(Runner.coins[index], gran));
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				constructors[index] = new NetworkBuilder(backSize);
				rnn[index] = buildNetwork(constructors[index]);
			}
		}
		
		dataset = Runner.normalizeData(Runner.readDataset(trainFiles[0], batchSize, datasize));
		
		trainingSets = new TrainingThread[rnn.length];
		threads = new Thread[trainingSets.length];
		
		this.iterations = iterations;
		this.files = files;
		this.coin = coin;
		this.gran = gran;
		this.threadIndex = 0;
		
		prepareTrainThreads(coin);
	}
	
	public TrainingRunner(File trainingFile, String presetPath, int iterations, int files, String coin, String gran) {
		this.presetPath = presetPath;
		this.trainFiles = new File[] {trainingFile};
		
		constructors = new NetworkBuilder[]{new NetworkBuilder(backSize)};
		
		rnn = new Network[1];
		
		trainingSets = new TrainingThread[1];
		
		threads = new Thread[1];
		
		
		try {
			rnn[0] = constructors[0].writeFromFile(presetPath);
		} catch (Exception e) {}
		
		this.iterations = iterations;
		this.files = files;
		this.coin = coin;
		this.gran = gran;
		this.threadIndex = 0;
		
		prepareTrainThreads(coin);
	}
	
	public void setParent(TrainingOrder parent) {
		this.parent = parent;
	}
	
	public String findRecent(String coin, String gran) {
		
		//substring() startIndex is inclusive, endIndex is exclusive
		
		String fullName = coin + "Network";
		
		String path = String.format("files/networks/multilayer/%s/%s/", gran, coin);
		
		File folder = new File(path);
		
		String[] names = folder.list();
		
		int srtIndex = fullName.length();
		int endIndex = 0;
		int max = 0;
		
		for(String n: names) {
			//coin###.rnn length = 11
			//123456789AB
			//name.substring(5, 8); - coin.length() - 1, name.length() - 4;
			
			endIndex = n.length() - 4;
			int index = Integer.parseInt(n.substring(srtIndex, endIndex));
			if(index > max) max = index;
		}
		
		path += String.format("%s%d.rnn", fullName, max);
		
		return path;
	}
	
	public void prepareTrainThreads(String coin) {
		if(coin == null) {
			for(int index = 0; index < trainingSets.length; index++) {
			
				double[][][] dataset = Runner.normalizeData(Runner.readDataset(trainFiles[index], batchSize, datasize));
				trainingSets[index] = new TrainingThread(rnn[index], constructors[index], dataset, Runner.coins[index], gran, files, iterations);
				trainingSets[index].setParent(this);
				trainingSets[index].setNetworkInfo(batchSize, backSize);
				threads[index] = new Thread(trainingSets[index]);
			}
			
		} else {
			double[][][] dataset = Runner.normalizeData(Runner.readDataset(trainFiles[0], batchSize, datasize));
			trainingSets[0] = new TrainingThread(rnn[0], constructors[0], dataset, coin, gran, files, iterations);
			trainingSets[0].setParent(this);
			trainingSets[0].setNetworkInfo(batchSize, backSize);
			threads[0] = new Thread(trainingSets[0]);
		}
	}
	
	public void setTrainingOrder(TrainingOrder parent) {
		this.parent = parent;
	}
	
	public void train() {
		while(threadIndex < TrainingOrder.MAX_THREADS) {
			if(threadIndex >= threads.length) break;
			threads[threadIndex].start();
			parent.putText(trainingSets[threadIndex].getCoin(), 0.0);
			parent.updateText(threadIndex, false);
			threadIndex++;
		}
	}
	
	public void updateError(String coin, double value) {
		parent.putText(coin, value);
		parent.updateText(threadIndex, true);
	}
	
	public void trainFinish(String coin) {
		parent.removeText(coin);
		
		if(threadIndex < threads.length) {
			
			try {
				threads[threadIndex].start();
				parent.putText(trainingSets[threadIndex].getCoin(), 0.0);
				parent.updateText(threadIndex, false);
				threadIndex++;
				
			} catch (IllegalThreadStateException e) {
				
				threads[threadIndex].start();
				parent.putText(trainingSets[threadIndex].getCoin(), 0.0);
				parent.updateText(threadIndex, false);
				threadIndex++;
				
			}
			
		} else {
			parent.endThread();
		}
	}
	
	private Network buildNetwork(NetworkBuilder constructor) {
		constructor.putLayer(inSize, Network.LINEAR, Network.INPUT_LAYER, true);
		//constructor.putLayer(50, Network.SIGMOID, Network.RECURRENT_LAYER, true);
		constructor.putLayer(32, Network.SIGMOID, Network.RECURRENT_LAYER, true);
		constructor.putLayer(32, Network.SIGMOID, Network.RECURRENT_LAYER, true);
		constructor.putLayer(32, Network.SIGMOID, Network.RECURRENT_LAYER, true);
		constructor.putLayer(32, Network.SIGMOID, Network.RECURRENT_LAYER, true);
		constructor.putLayer(32, Network.SIGMOID, Network.RECURRENT_LAYER, true);
		//constructor.putLayer(32, Network.SIGMOID, Network.RECURRENT_LAYER, true);
		constructor.putLayer(outSize, Network.SIGMOID, Network.OUTPUT_LAYER, false);
		
		return constructor.finalize(batchSize);
	}
	
	/* 
	public double[][][] readDataset(File trainingFile){
		ArrayList<double[]> data = new ArrayList<>();
		double[][][] ret = null;
		
		
		try {
			Scanner sc = new Scanner(trainingFile);
			
			String[] arr;
			double[] dubArr;
			sc.nextLine();
			
			while(sc.hasNextLine()) {
				String str = sc.nextLine();
				arr = str.split(",");
				dubArr = new double[arr.length - 1];
				
				for(int index = 1; index < arr.length; index++) {
					dubArr[index - 1] = Double.parseDouble(arr[index]);
					
				}
				
				data.add(dubArr);
			}
			
			sc.close();
			
			int dataLength = data.size() - 1;
			dataLength -= (dataLength % batchSize);
			
			ret = new double[dataLength][2][inSize];
			
			for(int index = 0; index < dataLength; index++) {
				int invIndex = (data.size() + index) - (1 + dataLength);
				ret[index][0] = data.get(invIndex);
				ret[index][1] = data.get(invIndex + 1);
				
			}
			
		} catch (Exception e) {
			System.out.println("Error");
			e.printStackTrace();
		}
		
		return normalizeData(ret);
	}
	
	public double[][][] normalizeData(double[][][] dataset) {
		double maxPrice = 0, minPrice = Double.MAX_VALUE;
		double maxVolume = 0, minVolume = Double.MAX_VALUE;
		final double EXPANSION_FACTOR = 1.25;
		
		final int MAX_P_INDEX = 1, MIN_P_INDEX0 = 0, MIN_P_INDEX1 = 2, MIN_P_INDEX2 = 3, VOL_INDEX = 4;
		
		for(int index = 0; index < dataset.length; index++) {
			double[] arr = dataset[index][0];
			if(arr[MAX_P_INDEX] > maxPrice) maxPrice = arr[MAX_P_INDEX];
			if(arr[MIN_P_INDEX0] < minPrice) minPrice = arr[MIN_P_INDEX0];
			if(arr[MIN_P_INDEX1] < minPrice) minPrice = arr[MIN_P_INDEX1];
			if(arr[MIN_P_INDEX2] < minPrice) minPrice = arr[MIN_P_INDEX2];
			if(arr[VOL_INDEX] > maxVolume) maxVolume = arr[VOL_INDEX];
			if(arr[VOL_INDEX] < minVolume) minVolume = arr[VOL_INDEX];
		}
		
		maxPrice *= EXPANSION_FACTOR;
		minPrice /= EXPANSION_FACTOR;
		maxVolume *= EXPANSION_FACTOR;
		minVolume /= EXPANSION_FACTOR;
		
		//System.out.printf("%n%.2f, %.2f, %.2f, %.2f%n%n", maxPrice, minPrice, maxVolume, minVolume);
		
		for(int index = 0; index < dataset.length; index++) {
			
			//System.out.println(Arrays.toString(dataset[index][0]));
			
			for(int index1 = 0; index1 < dataset[index][0].length; index1++) {
				double max, min;
				if(index1 < dataset[index][0].length - 1) {
					max = maxPrice;
					min = minPrice;
				} else {
					max = maxVolume;
					min = minVolume;
				}
				
				dataset[index][0][index1] = normalizeEqu(dataset[index][0][index1], max, min);
			}
			
			for(int index1 = 0; index1 < dataset[index][1].length; index1++) {
				double max, min;
				if(index1 < dataset[index][1].length - 1) {
					max = maxPrice;
					min = minPrice;
				} else {
					max = maxVolume;
					min = minVolume;
				}
				
				if(index == dataset.length - 1) dataset[index][1][index1] = normalizeEqu(dataset[index][1][index1], max, min);
			}
			
			if(index > 1) {
				System.out.println(Arrays.toString(dataset[index][0]));
				System.out.println(Arrays.toString(dataset[index - 1][1]) + "\n");
			}
		}
		
		return dataset;
	}
	
	public double normalizeEqu(double input, double max, double min) {
		final double upper = 1, lower = 0;
		
		double calc = ((input - min) / (max - min)) * (upper - lower) + lower;
		
		
		if(calc < 0 || calc > 1) {
			System.out.printf("%.4f, %.4f, %.4f, %.4f%n", input, max, min, calc);
			
			//System.exit(0);
		}
		
		
		return calc;
	}
	
	*/
}
