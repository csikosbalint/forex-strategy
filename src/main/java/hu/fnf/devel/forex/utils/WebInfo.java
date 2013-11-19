package hu.fnf.devel.forex.utils;

import hu.fnf.devel.forex.Main;
import hu.fnf.devel.forex.StateMachine;

import java.io.IOException;
import org.apache.log4j.Logger;

import com.dukascopy.api.IEngine;

public class WebInfo implements Info {
	private static final Logger logger = Logger.getLogger(WebInfo.class);
	private Info marketInfo;
	private Thread retriveThread;
	private boolean retrieving = false;
	@Override
	public boolean isMarketOpen(String market) {
		//  IEngine.Type 	getType()
        // Returns type of the engine, one of the IEngine.Type.LIVE, IEngine.Type.DEMO or IEngine.Type.TEST for tester.
		if ( StateMachine.getInstance().getContext().getEngine().getType().equals(IEngine.Type.TEST) ) {
			// TODO: on testing the market open is determined by the time and date
			return true;
		}
		
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
