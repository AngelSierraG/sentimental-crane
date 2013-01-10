package at.ac.tuwien.aic.sc.scheduler;

import at.ac.tuwien.aic.sc.core.event.ServerInstanceChangeEvent;
import at.ac.tuwien.aic.sc.scheduler.json.*;
import com.sun.jersey.api.client.*;
import com.sun.jersey.api.client.filter.ClientFilter;

import javax.ejb.Asynchronous;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_XML_TYPE;

/**
 * @author Dominik Strasser, dominikstr@gmail.com
 * @author Gregor Schauer
 */
@Stateless
public class ClusterManager {
	private static final Logger logger = Logger.getLogger(ClusterManager.class.getName());
	/**
	 * The unique ID of the tenant to use
	 */
	private static final String TENANT_ID = "cfa61aed5c0a4d6bb10d111e63a3337c";
	/**
	 * The username to access the services
	 */
	private static final String USERNAME = "aic12w02";
	/**
	 * The password used for authentication
	 */
	private static final String PASSWORD = "Vu6Sotee";
	/**
	 * The maximum cluster size
	 */
	private static final int CLUSTER_SIZE = 8;
	/**
	 * The base URL of the OpenStack API
	 */
	private static final String BASE_URL = "http://openstack.infosys.tuwien.ac.at";
	/**
	 * The ID of the image to use
	 */
	private static final String IMAGE_ID = "0e778831-6983-435d-bbb8-85935792cec1";
	/**
	 * The ID of the machine flavor to use
	 */
	private static final String FLAVOR_ID = "1";
	/**
	 * Generates random IDs for the cluster nodes
	 */
	private static int ID = new Random().nextInt(9990) + 10;

	@Inject
	private Event<ServerInstanceChangeEvent> changeEvent;

	/**
	 * Lists the available disc images.
	 */
	private void listImages() {
		Client client = createClient();
		String url = getUrl("2", TENANT_ID, "images", "detail");
		String response = client.resource(url)
				.type(APPLICATION_JSON_TYPE).accept(APPLICATION_XML_TYPE).get(String.class);
		System.out.println(response);
	}

	/**
	 * Lists the available machine flavors.
	 */
	private void listFlavors() {
		Client client = createClient();
		String url = getUrl("2", TENANT_ID, "flavors", "detail");
		String response = client.resource(url)
				.type(APPLICATION_JSON_TYPE).accept(APPLICATION_XML_TYPE).get(String.class);
		System.out.println(response);
	}

	/**
	 * Starts up to {@code n} cluster nodes.
	 *
	 * @param n number of nodes to start
	 */
	@Asynchronous
	public void startClusterNodes(int n) {
		n = Math.max(0, Math.min(CLUSTER_SIZE, n));

		Client client = createClient();
		for (int i = 0; i < n; i++) {
			ServerCreate serverCreate = new ServerCreate();
			serverCreate.server.name = "node" + (ID++);
			serverCreate.server.flavorRef = FLAVOR_ID;
			serverCreate.server.imageRef = BASE_URL + "/" + TENANT_ID + "/images/" + IMAGE_ID;

			String request = "{\n" +
					"    \"server\": {\n" +
					"        \"flavorRef\": \"" + serverCreate.server.flavorRef + "\",\n" +
					"        \"imageRef\": \"" + serverCreate.server.imageRef + "\",\n" +
					"        \"name\": \"" + serverCreate.server.name + "\"" +
					"    }\n" +
					"}";

			try {
				client.resource(getUrl("2", TENANT_ID, "servers"))
						.type(APPLICATION_JSON_TYPE).post(request);
			} catch (Exception e) {
				logger.log(Level.WARNING, "Error occurred while starting server.", e);
			}
		}
		client.destroy();
	}

	/**
	 * Resumes up to {@code n} nodes.
	 *
	 * @deprecated use {@link #startClusterNodes(int)} instead
	 */
	@Deprecated
	private void resume(final int n) {
		int cnt = 0;
		List<Server> servers = getServers();
		for (Server server : servers) {
			if (server.name.startsWith("node") && !server.name.equals("node01") && !server.isActive()) {
				try {
					action(server, "resume");

					if (!server.isActive() && logger.isLoggable(Level.INFO)) {
						logger.info("Last known status of server " + server.name + " was " + server.status);
					}
					logger.info("Starting server " + server.name);
					if (++cnt >= n) {
						return;
					}
				} catch (Exception e) {
					logger.log(Level.WARNING, "Exception occurred during shutdown.", e);
				}
			}
		}
	}

	/**
	 * Terminates up to {@code n} cluster nodes.
	 *
	 * @param n number of nodes to terminate
	 */
	@Asynchronous
	public void shutdownClusterNodes(final int n) {
		if (n <= 0) {
			return;
		}

		// Prevent shutdown of last node
		int activeServers = 0;
		List<Server> servers = getServers();
		for (Server server : servers) {
			if (server.isActive()) {
				activeServers++;
			}
		}
		if (activeServers <= 1) {
			return;
		}

		int cnt = 0;
		for (Server server : servers) {
			if (server.name.startsWith("node") && !server.name.equals("node01") && server.isActive()) {
				try {
					terminate(server);

					if (!server.isActive() && logger.isLoggable(Level.INFO)) {
						logger.info("Last known status of server " + server.name + " was " + server.status);
					}
					logger.info("Stopping server " + server.name);
					if (++cnt >= n) {
						return;
					}
				} catch (Exception e) {
					logger.log(Level.WARNING, "Exception occurred during shutdown.", e);
				}
			}
		}
	}

	/**
	 * Terminates a specific instance.
	 *
	 * @param server the instance to terminate
	 */
	private void terminate(Server server) {
		Client client = createClient();
		client.resource(getUrl("2", TENANT_ID, "servers", server.id))
				.type(APPLICATION_JSON_TYPE).delete();
		client.destroy();
	}

	/**
	 * Returns the number of active nodes.
	 *
	 * @return running nodes
	 */
	public int getNumberOfRunningNodes() {
		int nodes = 0;
		for (Server server : getServers()) {
			if (server.name.startsWith("node") && server.isActive()) {
				nodes++;
			}
		}
		return nodes;
	}

	/**
	 * Periodically notifies subscribers about the current cluster size.
	 */
	@Schedule(second = "*/2", minute = "*", hour = "*", persistent = false)
	public void fireServerInstanceChangeEvent() {
		changeEvent.fire(new ServerInstanceChangeEvent(getNumberOfRunningNodes()));
	}

	/**
	 * Suspends/resumes instances
	 *
	 * @param server the instance
	 * @param action the action to execute
	 */
	@Deprecated
	private void action(Server server, String action) {
		Client client = createClient();
		client.resource(getUrl("2", TENANT_ID, "servers", server.id, "action"))
				.type(APPLICATION_JSON_TYPE).post("{\"" + action + "\": null}");
		client.destroy();
	}

	/**
	 * Returns all available instances.
	 *
	 * @return list of instances
	 */
	private List<Server> getServers() {
		Client client = createClient();
		Servers servers = client.resource(getUrl("2", TENANT_ID, "servers", "detail"))
				.type(APPLICATION_JSON_TYPE).get(Servers.class);
		client.destroy();
		return servers.servers;
	}

	/**
	 * Creates a new HTTP client and authenticate it.
	 *
	 * @return the client
	 */
	private Client createClient() {
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

	/**
	 * Returns the HTTP URL of the desired resource.
	 *
	 * @param version the version of the OpenStack API to use
	 * @param paths   the URL path elements
	 * @return the URL
	 */
	private String getUrl(String version, String... paths) {
		StringBuilder url = new StringBuilder();
		if (version.equals("2.0")) {
			url.append(BASE_URL + ":5000/v2.0");
		} else if (version.equals("2")) {
			url.append(BASE_URL + ":8774/v2");
		} else {
			throw new IllegalArgumentException("API version " + version + " not supported.");
		}

		for (String path : paths) {
			url.append("/").append(path);
		}
		return url.toString();
	}
}