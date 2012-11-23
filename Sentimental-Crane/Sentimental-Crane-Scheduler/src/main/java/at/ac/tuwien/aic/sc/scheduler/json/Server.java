package at.ac.tuwien.aic.sc.scheduler.json;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;

/**
 * @author Dominik Strasser, dominikstr@gmail.com
 * @author Gregor Schauer
 */
@XmlRootElement(name = "server")
public class Server implements Serializable {
	public String status;
	public String name;
	public String id;
	public String hostId;

	@XmlTransient
	public boolean isActive() {
		return status != null && status.equalsIgnoreCase("active");
	}

	@Override
	public String toString() {
		return name + ":" + status;
	}
}
