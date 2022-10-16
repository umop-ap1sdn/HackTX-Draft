package prediction;

import recurrentNN.*;
import window.TestingOrder;

public class TrainingThread implements Runnable{
	
	Network rnn;
	NetworkBuilder filer;
	TrainingRunner parent;
	double[][][] data;
	int iterations, numOfFiles;
	
	String coin, granularity;
	String path, fileName;
	
	boolean running = false;
	
	int backSize, batchSize;
	
	final double ST_LEARN_RATE = 0.012;
	final double END_LEARN_RATE = 0.0006;
	
	//final double ST_LEARN_RATE = 0.0016;
	//final double END_LEARN_RATE = ST_LEARN_RATE;
	
	final int ITERATIONS_TO_DEC = 20;
	
	final double STATIC_LR = 0.002;
	
	TestingOrder graphWatch;
	
	double currLR, decLR;
	int count;
	
	public TrainingThread(Network rnn, NetworkBuilder filer, double[][][] data, String coin, String granularity, int files, int iterations) {
		this.rnn = rnn;
		this.filer = filer;
		this.data = data;
		this.iterations = iterations;
		this.numOfFiles = files;
		
		this.coin = coin;
		this.granularity = granularity;
		
		path = String.format("files/networks/multilayer/%s/%s/", granularity, coin);
		fileName = coin + "Network";
		
		
		decLR = ((ST_LEARN_RATE - END_LEARN_RATE) / iterations) * ITERATIONS_TO_DEC;
		count = 0;
		
		currLR = ST_LEARN_RATE;
	}
	
	public void setNetworkInfo(int batchSize, int backSize) {
		this.batchSize = batchSize;
		this.backSize = backSize;
	}
	
	public void setParent(TrainingRunner parent) {
		this.parent = parent;
		
		if(parent.inSize == 1) data = Runner.openOnly(data);
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		rnn.uploadDataset(data);
		
		rnn.setLearningRate(currLR);
		running = true;
		
		int dataIndex = 0;
		
		for(int index = 0; index < numOfFiles; index++) {
			for(int count = 0; count < iterations / numOfFiles; count++) {
				
				if(dataIndex >= data.length || dataIndex == 0) {
					
					dataIndex = 0;
					rnn.clearData();
					rnn.train(false);
					rnn.train(false);
					dataIndex += batchSize * 2;
					
				} else {
					
					//double[][][] test = rnn.trainSet(dataIndex, true);
					rnn.train(true);
					dataIndex += batchSize;
					//updateGraph();
					updateLearningRate();
					
				}
				
			}
			
			
			if(index % 5 == 4) filer.writeFile(path, fileName, rnn, true);

			//System.out.println(rnn.getTotalLoss());
			parent.updateError(this.coin, rnn.getTotalLoss());
			
		}
		
		running = false;
		parent.trainFinish(this.coin);
	}
	
	/*
	public void updateLearningRate() {
		final double baseRate = 0.0042, fullBias = 0.00052, desiredErr = 0.065;
		//final double baseRate = 0.00365, fullBias = 0.00042, desiredErr = 0.075;
		double learningRate = baseRate * (Math.max(Math.tanh(rnn.getNodeLoss() - desiredErr), 0) + fullBias);
		rnn.setLearningRate(learningRate);
	}
	
	*/
	
	public void updateLearningRate() {
		count++;
		if(count >= ITERATIONS_TO_DEC) {
			currLR -= decLR;
			count = 0;
		}
		
		rnn.setLearningRate(currLR);
	}
	
	public void updateGraph() {
		count++;
		if(count >= ITERATIONS_TO_DEC) {
			count = 0;
			
			filer.writeFile(path, "temp", rnn, false);
			Network tempRnn = new NetworkBuilder(0).writeFromFile(path + "temp0.rnn");
			
			if(graphWatch == null) {
				graphWatch = new TestingOrder(tempRnn, data, coin, granularity, 100);
				graphWatch.setPosition(0, 300);
				graphWatch.open();
			}
			
			graphWatch.updateRNN(tempRnn);
			
			
		}
	}
	
	public void closeGraph() {
		if(graphWatch != null) graphWatch.close();
	}
	
	public boolean getRunning() {
		return this.running;
	}
	
	public String getCoin() {
		return this.coin;
	}
}
