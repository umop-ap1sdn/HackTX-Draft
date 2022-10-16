package prediction;

import java.awt.Robot;

public class PreventSleep implements Runnable{
	
	boolean run;
	
	public PreventSleep() {
		run = true;
	}
	
	@Override
	public void run() {
		
		Robot mouseMover;
		
		try {
			mouseMover = new Robot();
			
			int defaultX = 500;
			int defaultY = 500;
			
			int xDirect = 1;
			int yDirect = -1;
			
			while(run) {
				Thread.sleep(150000);
				mouseMover.mouseMove(defaultX + xDirect, defaultY + yDirect);
				xDirect *= -1;
				yDirect *= -1;
				
			}
			
		} catch(Exception e) {
			
		}
		
		System.out.println("Thread completed");
	}
	
	public void turnoff() {
		run = false;
	}
}
