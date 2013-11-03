package hu.fnf.devel.forex;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.dukascopy.api.system.ClientFactory;
import com.dukascopy.api.system.IClient;
import com.dukascopy.api.system.ISystemListener;

public class Main {

	private static final Logger logger = Logger.getLogger(Main.class);
	private static String jnlpUrl = "https://www.dukascopy.com/client/demo/jclient/jforex.jnlp";
	private static String userName = "DEMO10037hLwUqEU";
	private static String password = "hLwUq";

	private static String phase;

	public static void setPhase(String phase) {
		if (Main.phase != null) {
			logger.info("--- Ending " + getPhase() + "phase ---");
		}
		Main.phase = phase;
		logger.info("----------------------------------------");
		logger.info("--- Starting " + getPhase() + "phase ---");
	}

	public static String getPhase() {
		return phase + " ";
	}

	public static void main(String[] args) {
		if ( args.length > 0 ) {
			PropertyConfigurator.configure(args[0]);
			logger.debug("Using config from file " + args[0]); 
		} else {
			BasicConfigurator.configure();
			logger.debug("Using basic configuration for logging.");
		}
		logger.info("---------------- Forex robot written by johnnym  ----------------");
		
		setPhase("Downloading client");

		IClient client = null;

		try {
			client = ClientFactory.getDefaultInstance();
		} catch (Exception e) {
			logger.fatal("Cannot instanciate client!", e);
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
				logger.info("Client has been disconnected...");
			}

			public void onConnect() {
				logger.info("Client has been connected...");
			}
		});

		setPhase("Connection");
		try {
			client.connect(jnlpUrl, userName, password);
		} catch (Exception e) {
			logger.fatal("Cannot connect to " + jnlpUrl + "@" + userName + ":" + password, e);
			return;
		}

		// wait for it to connect
		int i = 10; // wait max ten seconds
		while (i > 0 && !client.isConnected()) {
			logger.debug("waiting for connection ...");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.fatal("Connection process has been aborted!", e);
				return;
			}
			i--;
			if ( i == 0 ) {
				logger.fatal("Connection was made, but acknowledge timed out!");
				return;
			}
		}

		// workaround for LoadNumberOfCandlesAction for JForex-API versions >
		// 2.6.64
		try {
			int ms = 5000;
			logger.info("Wainting " + ms + "ms for candles to load.");
			logger.debug("workaround: JForex-API versions 2.6.64.");
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			logger.error("Workaround for LoadNumberOfCandlesAction for JForex-API versions 2.6.64 has been aborted.", e);
		}
		logger.info("Number of candles loaded.");

		setPhase("Strategy starting");
		try {
			client.startStrategy(StateMachine.getInstance());
		} catch (Exception e) {
			logger.fatal("Cannot start strategy, possibly connection error or no strategy!", e);
			return;
		}
		logger.info("strategy started.");
		setPhase("Running");
	}
}
