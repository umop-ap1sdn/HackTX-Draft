package data;

import java.io.File;
import java.util.Scanner;
import java.util.Stack;

public class FileToArray {
	
	public static final int FORMAT_LENGTH = 5; 
	
	public static double[][] buildFromFile(File file, boolean[] include){
		
		int dataLength = 0;
		int dataWidth = 0;
		
		if(include.length != FORMAT_LENGTH) {
			System.out.println("\"include\" array must be of size " + FORMAT_LENGTH);
			return null;
		} else {
			for(boolean enable: include) {
				dataWidth += enable ? 1 : 0;
			}
		}
		
		Scanner sc;
		double[][] ret = null;
		
		try {
			sc = new Scanner(file);
			
			//First line is not necessary
			sc.nextLine();
			
			Stack<double[]> reverse = new Stack<>();
			
			while(sc.hasNextLine()) {
				String line = sc.nextLine();
				String[] arr = line.split(",");
				
				double[] dubArr = strToDub(arr);
				double[] stackArr = new double[dataWidth];
				int stArrIndex = 0;
				
				for(int index = 0; index < dubArr.length; index++) {
					if(include[index]) {
						stackArr[stArrIndex] = dubArr[index];
					} else continue;
				}
				
				reverse.push(stackArr);
			}
			
			dataLength = reverse.size();
			ret = new double[dataLength][dataWidth];
			
			for(int index = 0; index < dataLength; index++) {
				ret[index] = reverse.pop();
			}
			
		} catch (Exception e) {
			
		}
		
		return ret;
	}
	
	public static double[][] buildFromFile(File file){
		boolean[] include = {true, true, true, true, true};
		return buildFromFile(file, include);
	}
	
	private static double[] strToDub(String[] arr) {
		double[] ret = new double[arr.length];
		for(int index = 0; index < arr.length; index++) {
			ret[index] = Double.parseDouble(arr[index]);
		}
		
		return ret;
	}
}
