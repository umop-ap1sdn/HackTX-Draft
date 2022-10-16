package prediction;

public class BackgroundProcess implements Runnable {
	
	public boolean updateFlag = false;
	private boolean finished = true;
	
	@Override
	public void run() {
		
		if(updateFlag) {
			finished = false;
			Runner.updateDatabase();
			updateFlag = false;
			finished = true;
		}
	}
	
	public boolean getFinished() {
		return this.finished;
	}
}
