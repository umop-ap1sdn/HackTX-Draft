package window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.*;

import prediction.*;

public class TestWindow extends JFrame implements ItemListener, ActionListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	JFrame frame;
	JPanel panel, presetPanel, presetEPanel;
	
	ArrayList<JLabel> labels;
	
	JComboBox<String> timeset, granset, coinSelect, preset;
	JTextField iterationText;
	JButton finish;
	
	JLabel errorText;
	
	final int sizeX = 480, sizeY = 360;
	final int comboW = 200, comboH = 20;
	final int locX = 140, distanceY = 20;
	final int buttonW = 100, buttonH = 40, buttonX = (sizeX - (buttonW + distanceY * 2)), buttonY = (sizeY - (buttonH + distanceY) * 2);
	
	final int marginLeft = 10, labelW = 120;
	
	int itemNum = 0;
	
	String dataset, gran, coin, presetName; 
	int iterations;
	
	TrainingOrder train;
	
	public TestWindow() {
		setup();
	}
	
	public void open() {
		frame.setVisible(true);
		
	}
	
	public void close() {
		frame.setVisible(false);
	}
	
	public void setup() {
		frame = new JFrame();
		frame.setResizable(false);
		frame.setSize(sizeX, sizeY);
		frame.setLayout(null);
		frame.setTitle("Testing Window");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		panel = new JPanel();
		panel.setLayout(null);
		panel.setBounds(0, 10, sizeX, sizeY - 10);
		
		addTimeset();
		itemNum++;
		addGranSet();
		itemNum++;
		addCoinSelect();
		itemNum++;
		addPreset();
		itemNum++;
		addIterations();
		addButton();
		
		panel.add(presetPanel);
		
		frame.add(panel);
	}
	
	public void addTimeset() {
		
		File file = new File("files/data/");
		
		if(!file.exists()) {
			addErrorMessage(false);
			return;
		}
		
		String[] options = file.list();
		timeset = new JComboBox<>(options);
		
		timeset.addItemListener(this);
		
		dataset = options[0];
		
		int offset = 1 + (2 * itemNum);
		
		timeset.setBounds(locX, distanceY * offset, comboW, comboH);
		//timeset.setSize(comboW, comboH);
		JLabel instr = new JLabel("Select Database");
		instr.setBounds(marginLeft, distanceY * offset, labelW, comboH);
		
		panel.add(timeset);
		panel.add(instr);
		
	}
	
	public void addGranSet() {
		String[] options = Runner.granularity;
		granset = new JComboBox<>(options);
		
		granset.addItemListener(this);
		
		gran = options[0];
		
		int offset = 1 + (2 * itemNum);
		granset.setBounds(locX, distanceY * offset, comboW, comboH);
		
		JLabel instr = new JLabel("Select Timeset");
		instr.setBounds(marginLeft, distanceY * offset, labelW, comboH);
		
		panel.add(granset);
		panel.add(instr);
	}
	
	public void addCoinSelect() {
		String[] options = Runner.coins;
		coinSelect = new JComboBox<>(options);
		
		coinSelect.addItemListener(this);
		
		coin = options[0];
		
		int offset = 1 + (2 * itemNum);
		coinSelect.setBounds(locX, distanceY * offset, comboW, comboH);
		
		JLabel instr = new JLabel("Select Coin");
		instr.setBounds(marginLeft, distanceY * offset, labelW, comboH);
		
		panel.add(coinSelect);
		panel.add(instr);
		
	}
	
	public void addPreset() {
		
		int offset = 1 + (2 * itemNum);
		
		presetPanel = new JPanel();
		presetPanel.setLayout(null);
		presetPanel.setBounds(0, distanceY * offset, sizeX, comboH);
		
		presetEPanel = new JPanel();
		presetEPanel.setLayout(null);
		presetEPanel.setBounds(0, distanceY * offset, sizeX, comboH);
		
		File file = new File(String.format("files/networks/multilayer/%s/%s/", gran, coin));
		
		errorText = addErrorMessage(true);
		
		String[] options = {" "};
		
		boolean showResult = true;
		
		if(!file.exists() || file.list().length == 0) {
			showResult = false;
		} else {
			options = order(file.list(), coin);
		}
		
		presetName = options[0];
		
		preset = new JComboBox<>(options);
		preset.addItemListener(this);
		
		preset.setBounds(locX, 0, comboW, comboH);
		
		JLabel instr = new JLabel("Select Preset");
		instr.setBounds(marginLeft, 0, labelW, comboH);
		
		presetPanel.add(preset);
		presetPanel.add(instr);
		
		if(showResult) {
			presetPanel.setVisible(true);
			presetEPanel.setVisible(false);
		} else {
			presetPanel.setVisible(false);
			presetEPanel.setVisible(true);
		}
		
		panel.add(presetPanel);
		panel.add(presetEPanel);
		
	}
	
	public String[] order(String[] arr, String coin) {
		String[] ret = new String[arr.length];
		
		String begin = coin + "Network";
		
		for(int index = 0; index < arr.length; index++) {
			int num = Integer.valueOf(arr[index].substring(begin.length(), arr[index].length() - 4));
			ret[num] = arr[index];
		}
		
		return ret;
	}
	
	public void evalPreset() {
		File file = new File(String.format("files/networks/multilayer/%s/%s/", gran, coin));
		if(file.exists()) {
			
			//System.out.println("exists");
			
			String[] options = order(file.list(), coin);
			
			preset.removeAllItems();
			
			for(String s: options) preset.addItem(s);
			
			presetPanel.setVisible(true);
			presetEPanel.setVisible(false);
		} else {
			
			//System.out.println("Existsn't");
			
			presetPanel.setVisible(false);
			presetEPanel.setVisible(true);
		}
	}
	
	public void addIterations() {
		iterationText = new JTextField();
		int offset = 1 + (2 * itemNum);
		
		iterationText.setBounds(locX, distanceY * offset, comboW, comboH);
		
		JLabel prompt = new JLabel("Enter Future Steps");
		prompt.setBounds(marginLeft, distanceY * offset, labelW, comboH);
		
		iterationText.setText("100");
		
		panel.add(iterationText);
		panel.add(prompt);
	}
	
	public void addButton() {
		finish = new JButton("Send Order");
		finish.addActionListener(this);
		finish.setBounds(buttonX, buttonY, buttonW, buttonH);
		finish.setFocusable(false);
		
		panel.add(finish);
	}
	
	public JLabel addErrorMessage(boolean preset) {
		JLabel error = new JLabel("--Not Available--");
		int offset = 1 + (2 * itemNum);
		error.setHorizontalAlignment(SwingConstants.CENTER);
		
		if(preset) {
			error.setBounds(locX, 0, comboW, comboH);
			presetEPanel.add(error);
		} else {
			error.setBounds(locX, distanceY * offset, comboW, comboH);
			panel.add(error);
		}
		
		return error;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(!presetPanel.isVisible()) presetName = null;
		
		if(e.getSource() == finish && presetName != null) {
			iterations = Integer.parseInt(iterationText.getText());
			File datafile = new File(String.format("files/data/%s/%s/%sdata.dat", dataset, gran, coin));
			File networkfile = new File(String.format("files/networks/multilayer/%s/%s/%s", gran, coin, presetName));
			
			TestingOrder test = new TestingOrder(networkfile, datafile, coin, gran, iterations);
			test.open();
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if(e.getSource() == timeset) dataset = (String)timeset.getSelectedItem();
		if(e.getSource() == granset) {
			gran = (String)granset.getSelectedItem();
			//System.out.println(gran);
			evalPreset();
		}
		if(e.getSource() == coinSelect) {
			coin = (String)coinSelect.getSelectedItem();
			//System.out.println(coin);
			
			evalPreset();
		}
		if(e.getSource() == preset) presetName = (String)preset.getSelectedItem();
		
	}
}
