package window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import prediction.*;

public class IntroFrame extends JFrame implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	JFrame frame;
	JButton train, test, update;
	
	JLabel message;
	
	final int buttonW = 150, buttonH = 30, buttonX = 75;
	
	final int frameW = 300, frameH = 300;
	
	boolean databaseFlag = false;
	
	BackgroundProcess bp;
	Thread runner;
	
	TrainingWindow trainWin;
	TestWindow testWin;
	
	public IntroFrame() {
		
		bp = new BackgroundProcess();
		runner = new Thread(bp);
		trainWin = new TrainingWindow();
		testWin = new TestWindow();
		
		frame = new JFrame();
		
		train = new JButton("Train");
		test = new JButton("Test");
		update = new JButton("Update Dataset");
		
		frame.setBounds(0, 0, frameW, frameH);
		frame.setLayout(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Option Select");
		frame.setResizable(false);
		
		train.setBounds(buttonX, 30, buttonW, buttonH);
		test.setBounds(buttonX, 90, buttonW, buttonH);
		update.setBounds(buttonX, 150, buttonW, buttonH);
		
		train.setFocusable(false);
		test.setFocusable(false);
		update.setFocusable(false);
		
		train.addActionListener(this);
		test.addActionListener(this);
		update.addActionListener(this);
		
		message = new JLabel("Creating Dataset...");
		message.setVisible(false);
		message.setSize(frameW, frameH - 50);
		message.setVerticalAlignment(SwingConstants.BOTTOM);
		message.setHorizontalAlignment(SwingConstants.CENTER);
		
	}
	
	public void open() {
		frame.add(train);
		frame.add(test);
		frame.add(update);
		frame.add(message);
		frame.setVisible(true);
		
	}
	

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
		if(e.getSource() == update) {
			databaseFlag = true;
			message.setText("Creating Dataset...");
			message.setVisible(true);
			
			//frame.update(null);
		}
		
		if(e.getSource() == train) {
			//open train window
			trainWin = new TrainingWindow();
			trainWin.open();
		}
		
		if(e.getSource() == test) {
			testWin = new TestWindow();
			testWin.open();
		}

		if(databaseFlag) {
			bp.updateFlag = true;
			runner.start();
			databaseFlag = false;
		}
	}
	
	public void setLabelText(String s) {
		message.setText(s);
		runner = new Thread(bp);
	}
}
