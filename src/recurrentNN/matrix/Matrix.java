package recurrentNN.matrix;

public class Matrix {
	
	public static final int FILL_ZERO = 0;
	public static final int FILL_RANDOM = 1;
	
	protected int rows, columns;
	protected double[][] arrForm;
	
	public Matrix(int j, int i, int fillCode){
		this.rows = i;
		this.columns = j;
		
		arrForm = new double[columns][rows];
		
		this.initialize(fillCode);
	}
	
	public Matrix(double[][] arrForm){
		this.arrForm = arrForm;
		this.columns = arrForm.length;
		this.rows = arrForm[0].length;
	}
	
	//Constructor to make deep clones
	public Matrix(Matrix copy) {
		this.rows = copy.rows;
		this.columns = copy.columns;
		
		arrForm = new double[columns][rows];
		
		try {
			buildArray(copy.arrForm);
		} catch (Exception e) { };
	}
	
	private void initialize(int fillCode) {
		switch(fillCode) {
		case FILL_ZERO:
			fill0();
			break;
		case FILL_RANDOM:
			fillRand();
			break;
		}
	}
	
	private void fill0() {
		for(int index1 = 0; index1 < arrForm.length; index1++) {
			for(int index2 = 0; index2 < arrForm[index1].length; index2++) {
				arrForm[index1][index2] = 0;
			}
		}
	}
	
	private void fillRand() {
		for(int index1 = 0; index1 < arrForm.length; index1++) {
			for(int index2 = 0; index2 < arrForm[index1].length; index2++) {
				arrForm[index1][index2] = (2 * Math.random()) - 1;
			}
		}
	}
	
	public void setValue(int column, int row, double value) throws Exception {
		if(column >= this.columns || column < 0) throw new Exception("Target out of range");
		if(row >= this.rows || row < 0) throw new Exception("Target out of range");
		
		
		this.arrForm[column][row] = value;
	}
	
	public double getVal(int column, int row) throws Exception {
		if(column >= this.columns || column < 0) throw new Exception("Target out of range");
		if(row >= this.rows || row < 0) throw new Exception("Target out of range");
		
		return this.arrForm[column][row];
	}
	
	void buildArray(double[][] arr) throws Exception {
		
		if(arr.length != this.columns || arr[0].length != this.rows) throw new Exception("Size Mismatch");
		
		for(int column = 0; column < this.columns; column++) {
			for(int row = 0; row < this.rows; row++) {
				this.setValue(column, row, arr[column][row]);
			}
		}
	}
	
	public static Matrix scale(Matrix m, double scalar) throws Exception {
		Matrix result = new Matrix(m);
		
		for(int row = 0; row < result.rows; row++) {
			for(int col = 0; col < result.columns; col++) {
				double newVal = m.getVal(col, row) * scalar;
				result.setValue(col, row, newVal);
			}
		}
		
		return result;
	}
	
	public static Matrix add(Matrix m1, Matrix m2) throws Exception {
		if(m1.rows != m2.rows || m1.columns != m2.columns) throw new Exception("Size Mismatch");
		
		Matrix result = new Matrix(m1.columns, m1.rows, FILL_ZERO);
		
		for(int col = 0; col < m1.columns; col++) {
			for(int row = 0; row < m1.rows; row++) {
				double newVal = m1.getVal(col, row) + m2.getVal(col, row);
				result.setValue(col, row, newVal);
			}
		}
		
		return result;
	}
	
	public static Matrix multiply(Matrix m1, Matrix m2) throws Exception {
		if(m1.rows != m2.columns) { 
			if(m1.columns == m2.rows) return multiply(m2, m1);
			else throw new Exception("Size Mismatch");
		}
		
		Matrix result = new Matrix(m1.columns, m2.rows, FILL_ZERO);
		
		for(int col = 0; col < result.columns; col++) {
			for(int row = 0; row < result.rows; row++) {
				result.setValue(col, row, getProduct(col, row, m1, m2));
			}
		}
		
		return result;
	}
	
	private static double getProduct(int col, int row, Matrix fac1, Matrix fac2) throws Exception {
		double runningSum = 0;
		
		for(int index = 0; index < fac1.rows; index++) {
			runningSum += fac1.getVal(col, index) * fac2.getVal(index, row);
		}
		
		return runningSum;
	}
	
	public static Vector getAsVector(Matrix m1) throws Exception {
		if(m1.rows > 1) throw new Exception("Must be 1 dimensional");
		
		return new Vector(m1.arrForm);
	}
	
	public double[][] getArray(){
		return this.arrForm;
	}
	
	public int getRows() {
		return this.rows;
	}
	
	public int getColumns() {
		return this.columns;
	}
	
	@Override
	public String toString() {
		String build = "";
		
		for(int col = 0; col < this.columns; col++) {
			build += "[";
			for(int row = 0; row < this.rows; row++) {
				
				try { 
					build += String.format("%.2f", this.getVal(col, row));
				} catch (Exception e) { }
				
				if(row < this.rows - 1) build += ", ";
			}
			
			build += "]";
			if(col < this.columns - 1) build += "\n";
		}
		
		return build;
	}
	
	public String dataString() {
		String ret = "";
		
		for(int col = 0; col < this.columns; col++) {
			for(int row = 0; row < this.rows; row++) {
				try {
					ret += this.getVal(col, row);
				} catch (Exception e) { }
				
				if(row < this.rows - 1) ret += ",";
			}
			if(col < this.columns - 1) ret += "\n";
		}
		
		return ret;
	}
}
