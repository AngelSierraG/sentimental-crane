package at.ac.tuwien.aic.sc.scheduler.json;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * @author Dominik Strasser, dominikstr@gmail.com
 * @author Gregor Schauer
 */
@XmlRootElement(name = "auth")
public class Credentials implements Serializable {
	public Authentication auth;

	public Credentials() {
	}

	public Credentials(String username, String password, String tenantId) {
		this.auth = new Authentication();
		this.auth.tenantId = tenantId;
		this.auth.passwordCredentials = new Authentication.PasswordCredentials();
		this.auth.passwordCredentials.username = username;
		this.auth.passwordCredentials.password = password;
	}

	@XmlRootElement(name = "auth")
	public static class Authentication implements Serializable {
		public PasswordCredentials passwordCredentials;
		public String tenantId;

		public static final class PasswordCredentials {
			public String username;
			public String password;
		}
	}

	@Override
	public String toString() {
		return auth != null && auth.passwordCredentials != null ? auth.passwordCredentials.username : "INVALID_CREDENTIALS";
	}
}
