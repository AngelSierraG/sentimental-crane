package at.ac.tuwien.aic.sc.scheduler;

import at.ac.tuwien.aic.sc.scheduler.json.Access;
import at.ac.tuwien.aic.sc.scheduler.json.Credentials;
import at.ac.tuwien.aic.sc.scheduler.json.Server;
import at.ac.tuwien.aic.sc.scheduler.json.Servers;
import com.sun.jersey.api.client.*;
import com.sun.jersey.api.client.filter.ClientFilter;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

/**
 * @author Dominik Strasser, dominikstr@gmail.com
 * @author Gregor Schauer
 */
@Stateless
public class ClusterManager {
	private static final Logger logger = Logger.getLogger(ClusterManager.class.getName());
	public static final String TENANT_ID = "cfa61aed5c0a4d6bb10d111e63a3337c";
	public static final String USERNAME = "aic12w02";
	public static final String PASSWORD = "Vu6Sotee";

	public static void main(String[] args) {
		System.out.println(new ClusterManager().getNumberOfRunningNodes());
	}

	@Asynchronous
	public void startClusterNode() {
		List<Server> servers = getServers();
		for (Server server : servers) {
			if (server.name.startsWith("node") && !server.name.equals("node01") && !server.isActive()) {
				action(server, "os-start");

				if (!server.isActive() && logger.isLoggable(Level.INFO)) {
					logger.info("Last known status of server " + server.name + " was " + server.status);
				}
				logger.info("Starting server " + server.name);
				return;
			}
		}
	}

	@Asynchronous
	public void shutdownClusterNode() {
		List<Server> servers = getServers();
		for (Server server : servers) {
			if (server.name.startsWith("node") && !server.name.equals("node01") && server.isActive()) {
				action(server, "os-stop");

				if (!server.isActive() && logger.isLoggable(Level.INFO)) {
					logger.info("Last known status of server " + server.name + " was " + server.status);
				}
				logger.info("Stopping server " + server.name);
				return;
			}
		}
	}

	public int getNumberOfRunningNodes() {
		int nodes = 0;
		for (Server server : getServers()) {
			if (server.name.startsWith("node") && server.isActive()) {
				nodes++;
			}
		}
		return nodes;
	}

	private void action(Server server, String action) {
		Client client = getClient();
		client.resource(getUrl("2", TENANT_ID, "servers", server.id, "action"))
				.type(APPLICATION_JSON_TYPE).post("{\"" + action + "\": null}");
		client.destroy();
	}

	private List<Server> getServers() {
		Client client = getClient();
		Servers servers = client.resource(getUrl("2", TENANT_ID, "servers", "detail"))
				.type(APPLICATION_JSON_TYPE).get(Servers.class);
		client.destroy();
		return servers.servers;
	}

	private Client getClient() {
		Client client = Client.create();
		final List<Access> access = client.resource(getUrl("2.0", "tokens"))
				.type(APPLICATION_JSON_TYPE).post(new GenericType<List<Access>>() {
				}, new Credentials(USERNAME, PASSWORD, TENANT_ID));

		client.addFilter(new ClientFilter() {
			@Override
			public ClientResponse handle(ClientRequest cr) throws ClientHandlerException {
				cr.getHeaders().add("X-Auth-Token", access.get(0));
				return getNext().handle(cr);
			}
		});

		return client;
	}

	private String getUrl(String version, String... paths) {
		StringBuilder url = new StringBuilder();
		if (version.equals("2.0")) {
			url.append("http://openstack.infosys.tuwien.ac.at:5000/v2.0");
		} else if (version.equals("2")) {
			url.append("http://openstack.infosys.tuwien.ac.at:8774/v2");
		} else {
			throw new IllegalArgumentException("API version " + version + " not supported.");
		}

		for (String path : paths) {
			url.append("/").append(path);
		}
		return url.toString();
	}
}
