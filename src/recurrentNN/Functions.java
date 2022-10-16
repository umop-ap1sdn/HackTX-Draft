package recurrentNN;

public class Functions {
	
	protected static double linear(double input) {
		return input;
	}
	
	protected static double relu(double input) {
		double ret = input;
		if(ret < 0) ret = 0;
		return ret;
	}
	
	protected static double sigmoid(double input) {
		double ret = 1 / (1 + Math.exp(-1 * input));
		return ret;
	}
	
	protected static double tanh(double input) {
		double ret = Math.tanh(input);
		return ret;
	}
	
	protected static double linearDeriv(double input) {
		return 1;
	}
	
	protected static double reluDeriv(double input) {
		if(input < 0) return 0;
		else return 1;
	}
	
	protected static double sigmoidDeriv(double input) {
		double ret = sigmoid(input) * (1 - sigmoid(input));
		return ret;
	}
	
	protected static double tanhDeriv(double input) {
		double ret = 1 / (Math.cosh(input));
		ret = Math.pow(ret, 2);
		return ret;
	}
}
