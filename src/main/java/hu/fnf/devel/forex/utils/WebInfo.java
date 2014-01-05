package hu.fnf.devel.forex.utils;

import hu.fnf.devel.forex.StateMachine;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;

public class WebInfo implements Info {
	private static final Logger logger = Logger.getLogger(WebInfo.class);
	private Info marketInfo;
	private Thread retriveThread;
	private boolean freshData = false;
	
	public WebInfo() {
		logger.info("Web information has been instantiated!");
		refreshData();
	}

	@Override
	public boolean isMarketOpen(String market) {
		/*
		 * test
		 */
		if (StateMachine.getInstance().getContext().getEngine().getType().equals(IEngine.Type.TEST)) {
			// if last tick on EURUSD is outdated, than something very bad thing has happened
			int hour = 0;
			int day = 0;
			try {
				long now = StateMachine.getInstance().getContext().getHistory().getLastTick(Instrument.EURUSD)
						.getTime();
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
				calendar.setTimeInMillis(now);
				hour = calendar.get(Calendar.HOUR_OF_DAY);
				day = calendar.get(Calendar.DAY_OF_WEEK);
			} catch (JFException e) {
				long now = 0;
				try {
					now = StateMachine.getInstance().getContext().getHistory().getLastTick(Instrument.GBPJPY)
							.getTime();
				} catch (JFException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
				calendar.setTimeInMillis(now);
				hour = calendar.get(Calendar.HOUR_OF_DAY);
				day = calendar.get(Calendar.DAY_OF_WEEK);
			}
			if ( day == Calendar.SATURDAY || day == Calendar.SUNDAY ) {
				return false;
			}
			
			if ( 22 < hour || hour < 8 ) {
				return false;
			}
			return true;
		}
		/*
		 * live, demo
		 */
		if (marketInfo != null) {
			return marketInfo.isMarketOpen(market);
		} else if (!freshData) {
			refreshData();
		}
		return false;
	}

	private void refreshData() {
		retriveThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					marketInfo = MarketWebProxy.getInstance();
				} catch (IOException e) {
					return;
				}
			}
		});
		retriveThread.start();
		freshData = retriveThread.isAlive();
	}

}
