package es.bsc.clurge.fake;

import com.google.gson.Gson;
import es.bsc.clurge.Clurge;
import es.bsc.clurge.common.cloudmw.CloudMiddlewareException;
import es.bsc.clurge.common.monit.Host;
import es.bsc.clurge.common.monit.HostsMonitoringManager;
import org.apache.logging.log4j.LogManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mmacias on 4/11/15.
 */
public class FakeMonitoringManager implements HostsMonitoringManager {

	private static boolean fakeHostsGenerated = false;
	private static final String FAKE_HOSTS_DESCRIPTIONS_PATH = "/hostsFakeMonitoring.json";
	private static final Gson gson = new Gson();
	private static Map<String, Host> hosts = new HashMap<>(); // List of hosts already created

	@Override
	public void generateHosts(String[] hostNames) {

	}

	@Override
	public Host getHost(String hostname) {

		if (!fakeHostsGenerated) {
			generateFakeHosts();
			fakeHostsGenerated = true;
		}

		return hosts.get(hostname);
	}

	/**
	 * Generates fake hosts read from a JSON file.
	 * The hosts generated are added to the fake middleware connector received.
	 *
	 */
	private static void generateFakeHosts() {
		BufferedReader bReader = new BufferedReader(new InputStreamReader(
				FakeMonitoringManager.class.getResourceAsStream(FAKE_HOSTS_DESCRIPTIONS_PATH)));
		List<HostFake> hostsFromFile = Arrays.asList(gson.fromJson(bReader, HostFake[].class));

		for (HostFake host: hostsFromFile) {
			HostFake hostFake = new HostFake(host.getHostname(),
					host.getTotalCpus(),
					(int) host.getTotalMemoryMb(),
					(int) host.getTotalDiskGb(),
					0, 0, 0);

			hosts.put(host.getHostname(), hostFake);
			try {
				Clurge.INSTANCE.getCloudMiddleware().addHost(hostFake);
			} catch(CloudMiddlewareException e) {
				LogManager.getLogger(FakeMonitoringManager.class).error(e.getMessage(),e);
			}
		}
		try {
			bReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
