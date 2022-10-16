package recurrentNN.matrix;

public class Vector extends Matrix {

	public Vector(int j, int fillCode) {
		super(j, 1, fillCode);
		// TODO Auto-generated constructor stub
	}

	public Vector(Vector copy) {
		super(copy);
	}
	
	public Vector(double[] arr){
		super(transpose(arr));
	}
	
	public Vector(double[][] arr){
		super(arr);
	}
	
	void buildVector(double[] arr) throws Exception {
		super.buildArray(transpose(arr));
	}
	
	private static double[][] transpose(double[] arr) {
		double[][] ret = new double[arr.length][1];
		
		for(int index = 0; index < arr.length; index++) {
			ret[index][0] = arr[index];
		}
		
		return ret;
	}
	
	public double[] getTArray() {
		double[][] arr = super.arrForm;
		double[] ret = new double[arr.length];
		
		for(int index = 0; index < arr.length; index++) {
			ret[index] = arr[index][0];
		}
		
		return ret;
	}
	
	public void setValue(int row, double value) throws Exception {
		super.setValue(row, 0, value);
	}
	
	public double getValue(int row) throws Exception {
		return super.getVal(row, 0);
	}
}
