package hu.fnf.devel.forex.utils;

import hu.fnf.devel.forex.Main;

import java.io.IOException;

import org.apache.log4j.Logger;

public class WebInfo implements Info {
	private static final Logger logger = Logger.getLogger(WebInfo.class);
	private Info marketInfo;
	private Thread retriveThread;
	private boolean retrieving = false;
	@Override
	public boolean isMarketOpen(String market) {
		
		if (marketInfo != null) {
			return marketInfo.isMarketOpen(market);
		} else if (!retrieving) {
			retriveThread = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						marketInfo = new MarketWebProxy();
					} catch ( IOException e ) {
						return;
					}
				}
			});
			retriveThread.start();
			retrieving = retriveThread.isAlive();
		}
		Main.massDebug(logger, "Until info download is finished false information will be returned.");
		return false;
	}

}
