package recurrentNN;

public class RecurrentLayer extends HiddenLayer {
	
	private RConnectionLayer rConnects;
	
	protected RecurrentLayer(int layerSize, int batchSize, int activationCode, boolean bias) {
		super(layerSize, batchSize, activationCode, bias);
	}
	
	protected void setRConnects(RConnectionLayer rConnects) {
		this.rConnects = rConnects;
	}
	
	protected RConnectionLayer getRConnects() {
		return this.rConnects;
	}
}
