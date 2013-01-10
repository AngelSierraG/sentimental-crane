package at.ac.tuwien.aic.sc.scheduler.json;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * @author Dominik Strasser, dominikstr@gmail.com
 * @author Gregor Schauer
 */
@XmlRootElement(name = "server")
public class ServerCreate implements Serializable {
	public Server server;

	public static final class Server {
		public String name;
		public String imageRef;
		public String flavorRef;
	}
}
