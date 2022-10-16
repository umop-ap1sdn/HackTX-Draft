package recurrentNN;

public class RConnectionLayer extends ConnectionLayer {
	
	private RecurrentLayer layer;
	
	protected RConnectionLayer(RecurrentLayer layer) {
		super(layer, layer);
		this.layer = layer;
		this.layer.setRConnects(this);
	}
	
}
