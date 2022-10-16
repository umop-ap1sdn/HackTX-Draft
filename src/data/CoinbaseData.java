package data;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Formatter;

public class CoinbaseData {
	
	static Calendar current;
	
	public static void buildFullBatch(String[] coins) {
		final char[] gran = {'m', 'h', 'd'};
		updateTime();
		
		for(char c: gran) {
			for(String coin: coins) {
				buildFile(coin, c);
			}
		}
	}
	
	public static void buildFile(String coin, char granularity) {
		
		int gran = 0;
		
		String granStr = "";
		
		if(granularity < 96) granularity += 32;
		
		switch(granularity) {
		case 'm':
			gran = 60;
			granStr = "MINUTES";
			break;
		case 'h':
			gran = 3600;
			granStr = "HOURS";
			break;
		case 'd':
			gran = 86400;
			granStr = "DAYS";
			break;
		default:
			System.out.println("Invalid granularity");
			return;
		}
		
		String link = String.format("https://api.pro.coinbase.com/products/%s-USD/candles?granularity=%d", coin, gran);
		
		String content= "";
		
		try {
			URL url = new URL(link);
			HttpURLConnection con = (HttpURLConnection) url.openConnection(); 
			
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			content = in.readLine();
			
			
		} catch (Exception e) {
			System.out.println(coin + " is invalid");
			return;
		}
		
		//Data has the format
		//Epoch Time, Open Price, High Price, Low Price, Close Price, Volume
		double[][] data = extractData(content);
		
		String dateStamp = String.format("%s-%s-%s", current.get(Calendar.MONTH) + 1, current.get(Calendar.DATE), current.get(Calendar.YEAR));
		dateStamp += "_" + current.get(Calendar.HOUR_OF_DAY);
		dateStamp += "_" + current.get(Calendar.MINUTE);
		
		String filePath = String.format("files/data/%s/%s/", dateStamp, granStr);
		String fileName = coin + "data.dat";
		
		createFile(filePath, fileName, data);
	}
	
	private static void createFile(String filePath, String fileName, double[][] data) {
		try {
			
			File file = new File(filePath);
			if(!file.exists()) file.mkdirs();
			
			Formatter writer = new Formatter(filePath + fileName);
			
			String[] datapoints = {"Epoch Time", "Open Price", "High Price", "Low Price", "Close Price", "Volume"};
			
			for(int index = 0; index < datapoints.length - 1; index++) {
				writer.format("%s, ", datapoints[index]);
			}
			writer.format("%s%n", datapoints[datapoints.length - 1]);
			
			
			for(double[] arr: data) {
				for(int index = 0; index < arr.length - 1; index++) {
					writer.format("%s,", arr[index]);
				}
				writer.format("%s%n", arr[arr.length - 1]);
			}
			
			writer.close();
			
		} catch(Exception e) {
			
		}
	}
	
	private static double[][] extractData(String input){
		final int datapoint = 6;
		
		String[] arr = input.split(",");
		double[][] data = new double[arr.length / datapoint][datapoint];
		
		int count = 0;
		for(int x = 0; x < data.length; x++) {
			for(int y = 0; y < data[x].length; y++) {
				String num = "";
				String current = arr[count];
				count++;
				
				for(char c: current.toCharArray()) {
					if(c != '[' && c != ']') num += c;
				}
				
				data[x][y] = Double.parseDouble(num);
				
			}
		}
		
		return data;
	}
	
	public static void updateTime() {
		current = Calendar.getInstance();
	}
}
