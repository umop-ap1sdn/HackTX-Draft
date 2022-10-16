package window;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.Formatter;

import javax.swing.*;

import recurrentNN.Network;

public class GraphPanel extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	Network rnn;
	double[][][] dataset;
	
	double pMin, pMax, vMin, vMax;
	int futureCount, pastRef, totalPoints;
	
	double[] ioPuts;
	
	final int marginLeft = 40, marginRight = TestingOrder.sizeX - 40;
	final int marginBottom = TestingOrder.sizeY - 40, marginTop = 40;
	final int graphLeft = 120, graphRight = TestingOrder.sizeX - 120;
	final int graphBottom = TestingOrder.sizeY - 80, graphTop = 80;
	
	
	JButton export;
	double[][] predictions;
	String[] filePath;
	
	boolean[] drawLine;
	JCheckBox[] lineBox;
	
	boolean drawAll;
	boolean fillData;
	int exportIndex;
	
	public GraphPanel(Network rnn, double[][][] normalized, double[] minmax, String[] path, int futureCount) {
		this.rnn = rnn;
		this.dataset = normalized;
		this.pMin = minmax[0];
		this.pMax = minmax[1];
		this.vMin = minmax[2];
		this.vMax = minmax[3];
		this.filePath = path;
		
		this.futureCount = futureCount;
		this.pastRef = futureCount / 4;
		
		this.totalPoints = futureCount + pastRef;
		
		this.setSize(TestingOrder.sizeX, TestingOrder.sizeY);
		this.setLayout(null);
		
		this.ioPuts = new double[TestingOrder.inSize];
		this.drawLine = new boolean[TestingOrder.inSize];
		
		export = new JButton();
		predictions = new double[futureCount][TestingOrder.inSize];
		fillData = true;
		exportIndex = 0;
		
		Arrays.fill(drawLine, true);
		this.lineBox = new JCheckBox[TestingOrder.inSize];
		drawAll = true;
		
		if(path != null) setupExport();
		setupCheckBoxes();
	}
	
	public void setupExport() {
		export.addActionListener(this);
		export.setFocusable(false);
		export.setText("Export");
		export.setBounds(marginLeft - 10, marginTop - 20, 75, 40);
		this.add(export);
	}
	
	public void setupCheckBoxes() {
		for(int index = 0; index < lineBox.length; index++) {
			lineBox[index] = new JCheckBox();
			
			lineBox[index].setFocusable(false);
			lineBox[index].setSelected(true);
			lineBox[index].setVisible(true);
			lineBox[index].addActionListener(this);
		}
	}
	
	public void updateRNN(Network rnn) {
		this.rnn = rnn;
		this.repaint();
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2d = (Graphics2D)g;
		
		setupGraph(g2d);
		setupLineIdentifiers(g2d);
		
		drawGraph(g2d);
	}
	
	public void setupGraph(Graphics2D g2d) {
		g2d.setStroke(new BasicStroke(4));
		g2d.setColor(Color.black);
		g2d.drawLine(graphLeft, graphTop, graphLeft, graphBottom);
		g2d.drawLine(graphLeft, graphBottom, graphRight, graphBottom);
		g2d.drawLine(graphRight, graphTop, graphRight, graphBottom);
		g2d.drawLine(graphLeft, graphTop, graphRight, graphTop);
		
		int AxisLabelHeight = graphTop + ((graphBottom - graphTop) / 2);
		
		g2d.drawString("Price", marginLeft, AxisLabelHeight);
		g2d.drawString("" + pMax, marginLeft, graphTop);
		g2d.drawString("" + pMin, marginLeft, graphBottom);
		
		g2d.drawString("Volume", graphRight + 10, AxisLabelHeight);
		g2d.drawString("" + vMax, graphRight + 10, graphTop);
		g2d.drawString("" + vMin, graphRight + 10, graphBottom);
		
		final int plotPoints = 5;
		
		int[] points = prepXAxis(plotPoints);
		
		int baseX = (graphRight - graphLeft) / plotPoints;
		
		for(int index = 0; index < points.length; index++) {
			g2d.drawString("" + points[index], graphLeft + (baseX * index), marginBottom - 20);
		}
		
		g2d.drawString("Iteration", (graphLeft + (graphRight - graphLeft) / 2) - 20, marginBottom - 10);
	}
	
	public void setupLineIdentifiers(Graphics2D g2d) {
		final int boxX = 15;
		final int boxY = 10;
		
		Color[] identColors = {Color.red, Color.orange, Color.blue, Color.magenta, Color.darkGray};
		String[] identities = {"Open Price", "High Price", "Low Price", "Close Price", "Volume Traded"};
		final int lines = TestingOrder.inSize;
		int baseX = (graphRight - graphLeft) / lines;
		
		for(int index = 0; index < lines; index++) {
			
			g2d.setColor(identColors[index]);
			g2d.fillRect(graphLeft + (baseX * index) + 25, marginTop, boxX, boxY);
			g2d.drawString(identities[index], graphLeft + (baseX * index) + 50, marginTop);
			
			lineBox[index].setBounds((baseX * index) + graphLeft, marginTop + 10, 20, 20);
			
			this.add(lineBox[index]);
		}
	}
	
	public int[] prepXAxis(int plotPoints) {
		int[] ret = new int[plotPoints + 1];
		ret[1] = 0;
		
		double base = (double) futureCount / (plotPoints - 1);
		
		for(int index = 0; index < ret.length - 1; index++) {
			ret[index] = (int)((base * index) - pastRef);
		}
		
		ret[ret.length - 1] = futureCount;
		return ret;
	}
	
	public void drawGraph(Graphics2D g2d) {
		g2d.setStroke(new BasicStroke(2));
		runupData();
		Color[] identColors = {Color.red, Color.orange, Color.blue, Color.magenta, Color.darkGray};
		
		double[] currentVals = new double[ioPuts.length];
		double[] newVals = new double[ioPuts.length];
		
		double baseX = (double)(graphRight - graphLeft) / totalPoints;
		
		for(int index = 0; index < totalPoints; index++) {
			
			
			if(index < pastRef) {
				currentVals = copyArr(ioPuts);
				calcPastNext();
				newVals = copyArr(ioPuts);
				
			} else {
				currentVals = copyArr(ioPuts);
				calcNext();
				newVals = copyArr(ioPuts);
			
			}
			
			for(int line = 0; line < TestingOrder.inSize; line++) {
				int x1 = (int)Math.round((baseX * index) + graphLeft), x2 = (int)Math.round(x1 + baseX);
				int y1 = graphTop + (int)((graphBottom - graphTop) * (1.00 - currentVals[line]));
				int y2 = graphTop + (int)((graphBottom - graphTop) * (1.00 - newVals[line]));
				
				if(drawLine[line]) {
					g2d.setColor(identColors[line]);
					g2d.drawLine(x1, y1, x2, y2);
				}
				
			}
		}
		
		fillData = false;
	}
	
	public double[] copyArr(double[] arr) {
		double[] ret = new double[arr.length];
		for(int index = 0; index < arr.length; index++) {
			ret[index] = arr[index];
		}
		
		return ret;
	}
	
	public void runupData() {
		for(double[][] data: dataset) {
			ioPuts = rnn.testInputs(data[0]);
			//System.out.println(Arrays.toString(data[0]));
		}
		
		//System.out.println();
	}
	
	
	int indexP = 0;
	public void calcPastNext() {
		int ind = indexP + ((dataset.length) - pastRef);
		//System.out.println(ind);
		ioPuts = dataset[ind][0];
		indexP++;
		indexP %= pastRef;
	}
	
	public void calcNext() {
		ioPuts = rnn.testInputs(ioPuts);
		if(fillData) {
			
			int test = TestingOrder.inSize;
			
			if(test == 5) {
			
				for(int index = 0; index < TestingOrder.inSize - 1; index++) {
					double m = pMax - pMin;
					double b = pMin;
					predictions[exportIndex][index] = (ioPuts[index] * m) + b;
					
				}
				
				double m = vMax - vMin;
				double b = vMin;
				predictions[exportIndex][TestingOrder.inSize - 1] = (ioPuts[TestingOrder.inSize - 1] * m) + b;
				
				exportIndex++;
			
			} else {
				double m = pMax - pMin;
				double b = pMin;
				predictions[exportIndex][0] = (ioPuts[0] * m) + b; 
				
				exportIndex++;
			}
		}
	}
	
	int index = 0;
	public void testNext() {
		ioPuts = dataset[index][0];
		index++;
	}
	
	public void export() throws Exception {
		String path = "files/excel/" + filePath[2] + "/" + filePath[3] + "/";
		File excel = new File(path);
		if(!excel.exists()) excel.mkdirs();
		
		path += filePath[4].substring(0, filePath[4].length() - 8) + ".csv";
		excel = new File(path);
		
		Formatter writer = new Formatter(excel);
		
		int inSize = TestingOrder.inSize;
		
		
		if(inSize == 5) {
			writer.format("%s,%s,%s,%s,%s%n", "Open Price", "High Price", "Low Price", "Close Price", "Volume Traded");
			for(int index = 0; index < predictions.length; index++) {
				double[] x = predictions[index];
				
				writer.format("%f,%f,%f,%f,%f%n", x[0], x[1], x[2], x[3], x[4]);
			}
		
		} else {
			writer.format("%s%n", "Open Price");
			for(int index = 0; index < predictions.length; index++) {
				double[] x = predictions[index];
				
				writer.format("%f%n", x[0]);
			}
		}
		
		writer.close();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
		if(e.getSource() == export) {
			try {
				export();
			} catch (Exception f) { 
				System.out.println("Error Writing Files");
			}
			
			return;
		}
		
		for(int index = 0; index < lineBox.length; index++) {
			drawLine[index] = lineBox[index].isSelected();
		}
		
		this.repaint();
	}
}
