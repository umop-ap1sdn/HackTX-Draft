package window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import prediction.PreventSleep;
import prediction.TrainingRunner;

public class TrainingOrder extends JFrame implements ActionListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final int MAX_THREADS = 2;
	
	HashMap<String, Double> text;
	JLabel[] frameTexts;
	JLabel progress;
	JButton close;
	JPanel buttonCon;
	
	String database, timeset, coin, preset;
	int iterations;
	int files;
	int calls = 0, totalThreads = 0, totalCalls = 0;
	boolean trainAll;
	
	File data;
	File preRNN;
	
	TrainingRunner train;
	
	JFrame frame;
	final int sizeX = 480, sizeY = 360;
	
	PreventSleep mouser;
	Thread sleep;
	
	String path;
	
	public TrainingOrder(String database, String timeset, String coin, String preset, int iterations, boolean trainAll, boolean multiPreset) {
		this.database = database;
		this.timeset = timeset;
		this.coin = coin;
		this.preset = preset;
		this.iterations = iterations;
		this.trainAll = trainAll;
		
		files = 10;
		
		path = String.format("files/data/%s/%s/", database, timeset);
		if(!trainAll) {
			path += coin + "data.dat";
			totalThreads = 1;
		}
		
		String presetPath = null;
		
		data = new File(path);
		
		if(trainAll) totalThreads = data.list().length;
		
		totalCalls = totalThreads * files;
		
		if(preset != null) {
			presetPath = String.format("files/networks/multilayer/%s/%s/%s", timeset, coin, preset);
			preRNN = new File(presetPath);
			train = new TrainingRunner(data, presetPath, iterations, files, coin, timeset);
			
		} else {
			train = new TrainingRunner(data, trainAll, multiPreset, iterations, files, coin, timeset);
			
		}
		
		mouser = new PreventSleep();
		sleep = new Thread(mouser);
		
		frameTexts = new JLabel[MAX_THREADS];
		text = new HashMap<>(MAX_THREADS);
		
		close = new JButton();
		buttonCon = new JPanel();
		
		initFrame();
	}
	
	public void initFrame() {
		frame = new JFrame();
		frame.setSize(sizeX, sizeY);
		frame.setResizable(false);
		frame.setTitle("Training Networks");
		frame.setLayout(null);
		frame.setVisible(false);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		train.setParent(this);
		
		for(int index = 0; index < frameTexts.length; index++) {
			frameTexts[index] = new JLabel();
			frameTexts[index].setBounds(0, 40 * (index + 1), sizeX, 20);
			frameTexts[index].setHorizontalAlignment(SwingConstants.CENTER);
		}
		
		progress = new JLabel();
		progress.setBounds(0, 40 * (MAX_THREADS + 2), sizeX, 60);
		progress.setHorizontalAlignment(SwingConstants.CENTER);
		//progress.setVerticalAlignment(SwingConstants.BOTTOM);
		
		close.setText("Close");
		close.addActionListener(this);
		close.setBounds(360, 260, 100, 50);
		close.setFocusable(false);
	}
	
	public void open() {
		frame.setVisible(true);
		addText();
		frame.add(close);
		
		train.train();
		sleep.start();
	}
	
	public void addText() {
		for(JLabel text: frameTexts) {
			text.setVisible(true);
			frame.add(text);
		}
		
		progress.setVisible(true);
		frame.add(progress);
	}
	
	public void endThread() {
		mouser.turnoff();
	}
	
	public void putText(String coin, double val) {
		text.put(coin, val);
	}
	
	/*
	public void putRNN(String coin, Network rnn) {
		graphs.put(coin, new TestingOrder());
	}
	*/
	
	public void removeText(String coin) {
		text.remove(coin);
	}
	
	public void updateText(int threadIndex, boolean add) {
		int index = 0;
		if(add) calls++;
		for(String coin: text.keySet()) {
			frameTexts[index].setText(String.format("%s Network Error: %.5f", coin, text.get(coin)));
			index++;
		}
		double percent = ((double)calls / totalCalls) * 100;
		progress.setText(String.format("<html>Thread %d / %d<br>Current Progress: %d / %d<br>%.2f%% Complete</html>", threadIndex, totalThreads, calls, totalCalls, percent));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getSource() == close) {
			frame.dispose();
		}
	}
}
