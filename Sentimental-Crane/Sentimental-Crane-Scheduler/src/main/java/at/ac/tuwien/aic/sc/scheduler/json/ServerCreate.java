package at.ac.tuwien.aic.sc.scheduler.json;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement(name = "server")
public class ServerCreate implements Serializable {
	public Server server;

	public static final class Server {
		public static String name;
		public static String imageRef;
		public static String flavorRef;
	}
}
