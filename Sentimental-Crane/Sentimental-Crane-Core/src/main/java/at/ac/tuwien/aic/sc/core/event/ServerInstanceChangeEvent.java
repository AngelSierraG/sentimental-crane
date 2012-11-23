package at.ac.tuwien.aic.sc.core.event;

/**
 * @author Philipp Zeppezauer, philipp.zeppezauer@gmail.com
 */
public class ServerInstanceChangeEvent {
	private int numberInstances;

	public ServerInstanceChangeEvent(int numberInstances) {
		this.numberInstances = numberInstances;
	}

	public int getNumberInstances() {
		return numberInstances;
	}
}
