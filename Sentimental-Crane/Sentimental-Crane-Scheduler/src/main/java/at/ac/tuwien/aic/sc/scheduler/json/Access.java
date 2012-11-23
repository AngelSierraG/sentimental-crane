package at.ac.tuwien.aic.sc.scheduler.json;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * @author Dominik Strasser, dominikstr@gmail.com
 * @author Gregor Schauer
 */
@XmlRootElement(name = "access")
public class Access {
	public Token token;

	public static final class Token implements Serializable {
		public String id;
	}

	@Override
	public String toString() {
		return token != null ? token.id : "INVALID_TOKEN";
	}
}
