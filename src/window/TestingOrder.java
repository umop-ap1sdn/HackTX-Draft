package window;

import java.io.File;
import javax.swing.JFrame;

import prediction.Runner;
import recurrentNN.Network;
import recurrentNN.NetworkBuilder;

public class TestingOrder {
	public static final int sizeX = 720;
	public static final int sizeY = 540;
	
	final static int inSize = 1;
	final int dataSize = 5;
	
	double[][][] data;
	double[][][] normalized;
	NetworkBuilder builder;
	Network rnn;
	
	JFrame frame;
	
	String coin, timeset;
	
	double pMax, pMin, vMax, vMin;
	
	GraphPanel graph;
	
	
	public TestingOrder(Network rnn, double[][][] data, String coin, String timeset, int futureSteps) {
		this.coin = coin;
		this.timeset = timeset;
		frame = new JFrame();
		
		findMaxMin(data);
		
		data = Runner.openOnly(data);
		
		double[] minmax = {pMin, pMax, vMin, vMax};
		
		graph = new GraphPanel(rnn, data, minmax, null, futureSteps);
		setupFrame();
		
	}
	
	public TestingOrder(File networkFile, File startupData, String coin, String timeset, int futureSteps) {
		this.coin = coin;
		this.timeset = timeset;
		
		String[] pathArr = startupData.getPath().split("/");
		
		if(pathArr.length == 1) pathArr = startupData.getPath().split("\\\\");
		
		data = Runner.readDataset(startupData, 1, dataSize);
		
		findMaxMin(data);
		
		double[] minmax = {pMin, pMax, vMin, vMax};
		//System.out.printf("%.2f %.2f %.2f %.2f%n", pMin, pMax, vMin, vMax);
		
		
		normalized = Runner.normalizeData(data);
		
		int suppressWarnings = inSize;
		if(suppressWarnings == 1) normalized = Runner.openOnly(normalized);
		
		builder = new NetworkBuilder(0);
		frame = new JFrame();
		rnn = builder.writeFromFile(networkFile);
		
		graph = new GraphPanel(rnn, normalized, minmax, pathArr, futureSteps);
		setupFrame();
	}
	
	public void close() {
		frame.setVisible(false);
		this.frame.dispose();
	}
	
	public void cryAboutIt(Exception e) {
		e.printStackTrace();
		System.exit(0);
	}
	
	public void setupFrame() {
		frame.setSize(sizeX, sizeY);
		frame.setTitle("Future Predictions for " + coin);
		frame.setVisible(false);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.add(graph);
	}
	
	public void open() {
		frame.setVisible(true);
	}
	
	public void setPosition(int x, int y) {
		frame.setLocation(x, y);
	}
	
	public void updateRNN(Network rnn) {
		graph.updateRNN(rnn);
	}
	
	public void findMaxMin(double[][][] dataset) {
		pMax = 0;
		pMin = Double.MAX_VALUE;
		vMax = 0;
		vMin = Double.MAX_VALUE;
		
		for(double[][] arr: dataset) {
			for(int index = 0; index < arr[0].length; index++) {
				if(index < dataSize - 1) {
					pMin = Math.min(pMin, arr[0][index]);
					pMax = Math.max(pMax, arr[0][index]);
				} else {
					vMin = Math.min(vMin, arr[0][index]);
					vMax = Math.max(vMax, arr[0][index]);
				}
			}
		}
	}
}
