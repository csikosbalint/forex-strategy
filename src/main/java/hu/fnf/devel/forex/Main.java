package hu.fnf.devel.forex;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.dukascopy.api.system.ClientFactory;
import com.dukascopy.api.system.IClient;
import com.dukascopy.api.system.ISystemListener;

public class Main {

	private static final Logger LOGGER = Logger.getLogger(Main.class);
	private static String jnlpUrl = "https://www.dukascopy.com/client/demo/jclient/jforex.jnlp";
	private static String userName = "DEMO10037hLwUqEU";
	private static String password = "hLwUq";
	
	private static String phase;
	
	public static void setPhase(String phase) {
		if ( Main.phase != null ) {
			LOGGER.info("--- Ending " + getPhase() + "phase ---");
		}
		Main.phase = phase;
		LOGGER.info("--- Starting " + getPhase() + "phase ---");
	}
	
	public static String getPhase() {
		return phase + " ";
	}
	
	public static void main(String[] args) {
		if ( args.length > 0 ) {
			PropertyConfigurator.configure(args[0]);
		} else {
			BasicConfigurator.configure();
		}

		IClient client = null;

		LOGGER.info("info");
		LOGGER.debug("debug");
		LOGGER.warn("warn");
		LOGGER.error("error");

		try {
			client = ClientFactory.getDefaultInstance();
		} catch (Exception e1) {
			e1.printStackTrace();
			LOGGER.error("Cannot instanciate client!");
			return;
		}

		client.setSystemListener(new ISystemListener() {

			public void onStop(long arg0) {
				// TODO Auto-generated method stub
			}

			public void onStart(long arg0) {
				// TODO Auto-generated method stub
			}

			public void onDisconnect() {
				LOGGER.info("Client has been disconnected...");
			}

			public void onConnect() {
				LOGGER.info("Client has been re-connected...");
			}
		});

		setPhase("Connection");
		try {
			client.connect(jnlpUrl, userName, password);
		} catch (Exception e) {
			LOGGER.fatal("Cannot connect to " + jnlpUrl + "@" + userName + ":" + password, e);
			return;
		}

		// wait for it to connect
		int i = 10; // wait max ten seconds
		while (i > 0 && !client.isConnected()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				LOGGER.fatal("Connection process has been aborted!", e);
				return;
			}
			i--;
		}
		if (!client.isConnected()) {
			LOGGER.error("Failed to connect Dukascopy servers!");
		}

		// workaround for LoadNumberOfCandlesAction for JForex-API versions >
		// 2.6.64
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			LOGGER.error("Workaround for LoadNumberOfCandlesAction for JForex-API versions 2.6.64 has been aborted.", e);
		}

		setPhase("Strategy starting");
		try {
			client.startStrategy(StateMachine.getInstance());
		} catch (Exception e) {
			LOGGER.fatal("Cannot start strategy, possibly connection error or no strategy!", e);
			return;
		}
	}
}
