package hu.fnf.devel.forex;

import hu.fnf.devel.forex.utils.Info;
import hu.fnf.devel.forex.utils.WebInfo;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.dukascopy.api.Instrument;
import com.dukascopy.api.system.ClientFactory;
import com.dukascopy.api.system.IClient;
import com.dukascopy.api.system.ISystemListener;
import com.dukascopy.api.system.ITesterClient;
import com.dukascopy.api.system.TesterFactory;

public class Main {

	private static final Logger logger = Logger.getLogger(Main.class);
	private static String jnlpUrl = "https://www.dukascopy.com/client/demo/jclient/jforex.jnlp";
	private static String userName = "DEMO10037VbnccEU";
	private static String password = "Vbncc";
	private static IClient client;
	private static Info info;

	private static String phase;
	private static String lastDLog;
	private static String lastILog;

	public static void setPhase(String phase) {
		if (Main.phase != null) {
			logger.info("--- Stopping " + getPhase() + "phase ---");
		}
		Main.phase = phase;
		if (Main.phase != null) {
			logger.info("----------------------------------------");
			logger.info("--- Starting " + getPhase() + "phase ---");
		}
	}

	public static String getPhase() {
		return phase + " ";
	}

	public static void main(String[] args) {

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				Main.setPhase("Interrupted");
				closing();
				logger.info("------------------------- Done. ----------------------------");
			}
		}));

		if (args.length > 0) {
			PropertyConfigurator.configure(args[0]);
			logger.info("Using config from file.");
			logger.debug("log4j.properties: " + args[0]);
		} else {
			BasicConfigurator.configure();
			logger.info("Using basic configuration for logging.");
		}
		logger.info("-------------- Forex robot written by johnnym  --------------");

		setPhase("Initalization");
		info = new WebInfo();
		if (args.length > 1 && args[1].equalsIgnoreCase("test")) {
			try {
				client = TesterFactory.getDefaultInstance();
				//setting initial deposit
		        ((ITesterClient) client).setInitialDeposit(Instrument.EURUSD.getSecondaryCurrency(), 50000);
			} catch (Exception e) {
				logger.fatal("Cannot instanciate client!", e);
				return;
			}
		} else {
			try {
				client = ClientFactory.getDefaultInstance();
			} catch (Exception e) {
				logger.fatal("Cannot instanciate client!", e);
				return;
			}
		}

		client.setSystemListener(new ISystemListener() {

			public void onStop(long arg0) {
				setPhase(null);
			}

			public void onStart(long arg0) {
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
			closing();
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
				closing();
				return;
			}
			i--;
			if (i == 0) {
				logger.fatal("Connection was made, but acknowledge timed out!");
				closing();
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
			closing();
			return;
		}
		logger.info("strategy started.");
		setPhase("Running");
	}

	private static void closing() {
		setPhase("Closing");
		if (client != null && client.isConnected()) {
			client.disconnect(); // onStop called
		}
	}

	public static boolean isMarketOpen(String market) {
		return info.isMarketOpen(market);
	}

	public static void massDebug(Logger logger, String msg) {
		if (msg.equalsIgnoreCase(lastDLog)) {
			return;
		}
		lastDLog = msg;
		logger.debug(msg);
	}

	public static void massInfo(Logger logger, String msg) {
		if (msg.equalsIgnoreCase(lastILog)) {
			return;
		}
		lastILog = msg;
		logger.info(msg);
	}
}
