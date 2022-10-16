package prediction;

import java.io.File;
import java.util.Scanner;
import java.util.Stack;

import data.CoinbaseData;
import window.IntroFrame;

public class Runner {
	
	//coins_coinbase
	public static final String[] coins_coinbase = {"ZRX", "1INCH", "AAVE", "ALCX", "ACH", "AGLD", "ALGO", "AMP", "FORTH", "API3", "ARPA", "ASM", "REP", "AVAX", "AXS", "BADGER", "BAL", "BNT", "BAND", "BOND", "BAT", "BICO", "BTC", "BCH", "BLZ", "AUCTION", "BTRST", "ADA", "CTSI", "CGLD", "LINK", "CVC", "CLV", "COMP", "ATOM", "COTI", "COVAL", "CRO", "CRV", "DAI", "DASH", "MANA", "DESO", "DDX", "YFII", "DNT", "DOGE", "ENJ", "MLN", "EOS", "ETH", "ETC", "ENS", "FET", "FIL", "FX", "GALA", "GTC", "GODS", "GYEN", "FARM", "ZEN", "IDEX", "RLC", "IMX", "ICP", "IOTX", "JASMY", "KEEP", "KRL", "KNC", "LCX", "LTC", "LPT", "LOOM", "LRC", "MASK", "MDT", "MIR", "MCO2", "MUSD", "NKN", "NU", "NMR", "OMG", "OXT", "OGN", "TRAC", "ORN", "PAX", "PERP", "PLA", "DOT", "POLS", "MATIC", "POLY", "POWR", "QNT", "QUICK", "RAD", "RAI", "RLY", "RGT", "RARI", "REN", "REQ", "RBN", "FOX", "SHIB", "SKL", "SKL", "SOL", "SPELL", "XLM", "STORJ", "SUKU", "SUPER", "SUSHI", "SNX", "TRB", "UST", "USDT", "XTZ", "GRT", "TRIBE", "TRU", "UMA", "UNI", "VGX", "WBTC", "wCFG", "WLUNA", "XYO", "YFI", "ZEC"};
	
	//coins_robin
	public static final String[] coins = {"BTC", "ETH", "BCH", "COMP", "LTC", "SOL", "ETC", "POLY", "DOGE", "SHIB"};
	
	//coins_ftx
	public static final String[] coins_ftx = {"BTC", "ETH", "LTC", "SOL", "LINK", "YFI", "SUSHI", "UNI", "BAT", "WBTC", "DOGE", "GRT", "DAI", "MKR", "AAVE", "MATIC", "SHIB", "AVAX"};
	
	public static final char[] gran = {'m', 'h', 'd'};
	public static final String[] granularity = {"MINUTES", "HOURS", "DAYS"};
	
	static IntroFrame start;
	
	public static void main(String[]args) {
		
		start = new IntroFrame();
		start.open();
		
	}
	
	public static void updateDatabase() {
		CoinbaseData.updateTime();
		for(String n: coins) {
			for(char c: gran) {
				CoinbaseData.buildFile(n, c);
			}
		}
		
		start.setLabelText("Database Completed");
	}
	
	public static double[][][] readDataset(File trainingFile, int batchSize, int inSize){
		Stack<double[]> data = new Stack<>();
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
				
				data.push(dubArr);
			}
			
			sc.close();
			
			int dataLength = data.size() - 1;
			dataLength -= (dataLength % batchSize);
			
			ret = new double[dataLength][2][inSize];
			
			while(data.size() > dataLength + 1) {
				data.pop();
			}
			
			double[] array = data.pop();
			
			for(int index = 0; index < dataLength; index++) {
				
				ret[index][0] = array;
				array = data.pop();
				ret[index][1] = array;
				
			}
			
		} catch (Exception e) {
			System.out.println("Error");
			e.printStackTrace();
		}
		
		return ret;
	}
	
	public static double[][][] normalizeData(double[][][] dataset) {
		double maxPrice = 0, minPrice = Double.MAX_VALUE;
		double maxVolume = 0, minVolume = Double.MAX_VALUE;
		final double EXPANSION_FACTOR = 1.05;
		
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
			
			/*
			if(index > 1) {
				System.out.println(Arrays.toString(dataset[index][0]));
				System.out.println(Arrays.toString(dataset[index - 1][1]) + "\n");
			}
			*/
		}
		
		return dataset;
	}
	
	public static double normalizeEqu(double input, double max, double min) {
		final double upper = 1, lower = 0;
		
		double calc = ((input - min) / (max - min)) * (upper - lower) + lower;
		
		
		if(calc < 0 || calc > 1) {
			//System.out.printf("%.4f, %.4f, %.4f, %.4f%n", input, max, min, calc);
			
			//System.exit(0);
		}
		
		//System.out.printf("%.4f%n", calc);
		
		return calc;
	}
	
	public static double[][][] openOnly(double[][][] dataset) {
		double[][][] ret = new double[dataset.length][2][1];
		for(int index = 0; index < dataset.length; index++) {
			ret[index][0][0] = dataset[index][0][0];
			ret[index][1][0] = dataset[index][1][0];
			
		}
		
		return ret;
	}
}
