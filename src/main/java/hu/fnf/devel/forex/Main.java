package hu.fnf.devel.forex;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;
import com.dukascopy.api.system.ClientFactory;
import com.dukascopy.api.system.IClient;
import com.dukascopy.api.system.IStrategyExceptionHandler;
import com.dukascopy.api.system.ISystemListener;
import com.dukascopy.api.system.JFAuthenticationException;
import com.dukascopy.api.system.JFVersionException;

public class Main {

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	private static String jnlpUrl = "https://www.dukascopy.com/client/demo/jclient/jforex.jnlp";
	private static String userName = "DEMO10037hLwUqEU";
	private static String password = "hLwUq";

	public static void main(String[] args) {
		LOGGER.info("hello world");
		IClient client = null;

		try {
			client = ClientFactory.getDefaultInstance();
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		client.setSystemListener(new ISystemListener() {

			public void onStop(long arg0) {
				// TODO Auto-generated method stub

			}

			public void onStart(long arg0) {
				// TODO Auto-generated method stub

			}

			public void onDisconnect() {
				// TODO Auto-generated method stub

			}

			public void onConnect() {
				// TODO Auto-generated method stub

			}
		});

		try {
			client.connect(jnlpUrl, userName, password);
		} catch (JFAuthenticationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JFVersionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		 * is .connect() an async call???
		 */

		// wait for it to connect
		int i = 10; // wait max ten seconds
		while (i > 0 && !client.isConnected()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			i--;
		}
		if (!client.isConnected()) {
			LOGGER.error("Failed to connect Dukascopy servers");
			System.exit(1);
		}

		Set<Instrument> instruments = new HashSet<Instrument>();
		instruments.add(Instrument.EURUSD);

		client.setSubscribedInstruments(instruments);

		// workaround for LoadNumberOfCandlesAction for JForex-API versions >
		// 2.6.64
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		client.startStrategy(new IStrategy() {

			public void onTick(Instrument instrument, ITick tick) throws JFException {
				// TODO Auto-generated method stub

			}

			public void onStop() throws JFException {
				// TODO Auto-generated method stub

			}

			public void onStart(IContext context) throws JFException {
				// TODO Auto-generated method stub
				context.getConsole().getInfo().println("Started strategy");
			}

			public void onMessage(IMessage message) throws JFException {
				// TODO Auto-generated method stub

			}

			public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
				// TODO Auto-generated method stub

			}

			public void onAccount(IAccount account) throws JFException {
				// TODO Auto-generated method stub

			}
		}, new IStrategyExceptionHandler() {

			public void onException(long strategyId, Source source, Throwable t) {
				// TODO Auto-generated method stub

			}
		});
	}
}
