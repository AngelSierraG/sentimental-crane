package at.ac.tuwien.aic.sc.scheduler.json;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author Dominik Strasser, dominikstr@gmail.com
 * @author Gregor Schauer
 */
@XmlRootElement
public class Servers {
	public List<Server> servers;

	@Override
	public String toString() {
		return servers.toString();
	}
}
