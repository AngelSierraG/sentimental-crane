package at.ac.tuwien.aic.sc.scheduler;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.openstack.keystone.KeystoneClient;
import org.openstack.keystone.api.Authenticate;
import org.openstack.keystone.api.ListTenants;
import org.openstack.keystone.model.Access;
import org.openstack.keystone.model.Authentication;
import org.openstack.keystone.model.Tenants;
import org.openstack.keystone.model.Authentication.PasswordCredentials;
import org.openstack.keystone.model.Authentication.Token;
import org.openstack.keystone.utils.KeystoneUtils;
import org.openstack.nova.NovaClient;
import org.openstack.nova.api.FlavorsCore;
import org.openstack.nova.api.ServersCore;
import org.openstack.nova.model.Flavor;
import org.openstack.nova.model.Flavors;
import org.openstack.nova.model.Server;
import org.openstack.nova.model.ServerForCreate;

/**
 * @author Dominik Strasser, dominikstr@gmail.com
 */
@Stateless
public class ClusterManager {
	private static final Logger logger = Logger.getLogger(ClusterManager.class.getName());

	private static final String KEYSTONE_AUTH_URL = "http://openstack.infosys.tuwien.ac.at:5000/v2.0";
	private static final String KEYSTONE_USERNAME = "aic12w02";
	private static final String KEYSTONE_PASSWORD = "Vu6Sotee";
	
	private KeystoneClient keystone;
	private Authentication authentication;
	private PasswordCredentials passwordCredentials;
	private Access access;
	private Tenants tenants;
	private NovaClient novaClient;
	
	private int nodeCounter;
	private Map<String, Server> servers;
	
	public ClusterManager(){
		keystone = new KeystoneClient(KEYSTONE_AUTH_URL);
		authentication = new Authentication();
		passwordCredentials = new PasswordCredentials();
		passwordCredentials.setUsername(KEYSTONE_USERNAME);
		passwordCredentials.setPassword(KEYSTONE_PASSWORD);
		authentication.setPasswordCredentials(passwordCredentials);

		nodeCounter=0;
		servers = new HashMap<String, Server>();
		
		//access with unscoped token
		access = keystone.execute(new Authenticate(authentication));

		//use the token in the following requests
		keystone.setToken(access.getToken().getId());

		tenants = keystone.execute(new ListTenants());
		//try to exchange token using the first tenant
		if(tenants.getList().size() > 0) {

			authentication = new Authentication();
			Token token = new Token();
			token.setId(access.getToken().getId());
			authentication.setToken(token);
			authentication.setTenantId(tenants.getList().get(0).getId());

			access = keystone.execute(new Authenticate(authentication));

			novaClient = new NovaClient(KeystoneUtils.findEndpointURL(access.getServiceCatalog(), "compute", null, "public"), access.getToken().getId());

			Flavors flavors = novaClient.execute(FlavorsCore.listFlavors());
			for(Flavor flavor : flavors) {
				System.out.println(flavor);
			}

		} else {
			System.out.println("No tenants found!");
		}
	}
	
	@Asynchronous
	public void startClusterNode() {
		ServerForCreate serverForCreate = new ServerForCreate();
		serverForCreate.setName("node" + nodeCounter);
		serverForCreate.setFlavorRef("100"); 	// flavor = type of machine -> find out id of m1.tiny
		serverForCreate.setImageRef("120"); 	// reference of image or snapshot -> find out id of snapshot
		serverForCreate.setKeyName("node" + nodeCounter);
		serverForCreate.getSecurityGroups().add(new ServerForCreate.SecurityGroup("default"));
		
		Server server = novaClient.execute(ServersCore.createServer(serverForCreate));
		
		servers.put(server.getId(), server);
		
		System.out.println("Created: \n" + server);
		
		logger.info("Starting up cluster node");

	}

	@Asynchronous
	public void shutdownClusterNode(String id) {
		novaClient.execute(ServersCore.deleteServer(id));
		servers.remove(id);
		logger.info("shutting down cluster node");
	}
	
	@Asynchronous
	public void shutdownAllClusterNodes() {
		for(Entry<String, Server> server : servers.entrySet()) {
			novaClient.execute(ServersCore.deleteServer(server.getKey()));
			servers.remove(server.getKey());
		}

		logger.info("shutting down all cluster nodes");
	}

	public int getNumberOfRunningNodes() {
		return servers.size();
	}
}
