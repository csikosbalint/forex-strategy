package hu.fnf.devel.forex;

import hu.fnf.devel.forex.states.MACDSample452State;
import hu.fnf.devel.forex.states.ScalpHolder7State;
import hu.fnf.devel.forex.states.SignalSeekerState;
import hu.fnf.devel.forex.utils.Signal;
import hu.fnf.devel.forex.utils.State;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IChart;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;
import com.dukascopy.api.drawings.IScreenLabelChartObject;
import com.dukascopy.api.drawings.IScreenLabelChartObject.Corner;

public class StateMachine implements IStrategy {
	private static final Logger logger = Logger.getLogger(StateMachine.class);

	public static final int OPEN 	= 0;
	public static final int CLOSE 	= 1;

	private static StateMachine instance;
	private static State state;
	private IContext context;

	private Collection<IChart> charts = new ArrayList<IChart>();

	/*
	 * THREAD SAFE!!!
	 */
	public synchronized static StateMachine getInstance() {
		if (instance == null) {
			instance = new StateMachine();
		}
		return instance;
	}

	public static synchronized void changeState(State newState) {
		if (newState == null) {
			return;
		}
		if (StateMachine.state != null && !StateMachine.state.getName().equalsIgnoreCase(newState.getName())) {
			logger.info("state change " + StateMachine.state.getName() + " -> " + newState.getName());
			if (StateMachine.state.onLeaving() && newState.onArriving()) {
				StateMachine.state = newState;
			}
		} else {
			if (newState.onArriving()) {
				StateMachine.state = newState;
			}
		}
	}

	/*
	 * --------- END ---------
	 */

	public State getState() {
		return state;
	}

	public List<IOrder> getOrders() throws JFException {
		try {
			return context.getEngine().getOrders();
		} catch (JFException e) {
			throw new JFException("Cannot load orders from server.", e);
		}
	}

	public IContext getContext() {
		return context;
	}

	private State recignizeState() throws JFException {
		// TODO: recognize states
		try {
			if (context.getEngine().getOrders().size() == 0) {
				return new SignalSeekerState();
			} else {
				logger.fatal("No alg. to determine state!");
				return null;
			}
		} catch (JFException e) {
			throw new JFException("Cannot load orders from server.", e);
		}
	}

	@Override
	public void onStart(IContext context) throws JFException {
		this.context = context;

		changeState(recignizeState());

		if (context.getEngine().getType().equals(IEngine.Type.TEST)) {
			for (IChart c : charts) {
				setChartDecoration(c);
			}
		}
	}

	private void setChartDecoration(IChart c) {
		IScreenLabelChartObject label = c.getChartObjectFactory().createScreenLabel("screenLabel");
		label.setCorner(Corner.TOP_LEFT);
		label.setxDistance(5);
		label.setyDistance(5);
		label.setText(
				c.getInstrument().name() + " " + c.getFeedDescriptor().getPeriod().name(),
				new Font(Font.MONOSPACED , Font.PLAIN, 12));
		label.setColor(Color.BLACK);
		c.add(label); 
    	if ( (new MACDSample452State().getInstruments().contains(c.getInstrument()))) {
    		/*
    		 * MACD Sample indicators
    		 */
    		c.add(context.getIndicators().getIndicator("MACD"), new Object[] { 12, 26, 9 });
    	} 
		
	}

	@Override
	public void onTick(Instrument instrument, ITick tick) throws JFException {
		State bestState = null;
		Signal bestSignal = new Signal();
		for (State nextState : StateMachine.state.getNextStates()) { // instantiate
																	// all
																	// states!

			Signal nextSignal = nextState.getSignal(instrument, tick, StateMachine.state);
			if (nextSignal != null && nextSignal.getValue() != 0) {
				logger.debug(nextState.getName() + " signal is " + nextSignal.getValue() + " (" + instrument.name()
						+ ")");
				if (nextSignal.getValue() > bestSignal.getValue()) {
					bestState = nextState;
					bestSignal = nextSignal;
				}
			}
		}

		if (bestState != null) {
			logger.info(bestState.getName() + " state is selected(from " + bestSignal.getValue() + ","
					+ bestSignal.getTag());
			bestState.prepareCommands(bestSignal);
			if (bestState.executeCommands()) {
				changeState(bestState);
			}
		} else {
			Main.massInfo(logger, "Searching for promising state(s)...");
		}
	}

	@Override
	public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
		State bestState = null;
		Signal bestSignal = new Signal();
		for (State nextState : StateMachine.state.getNextStates()) { // instantiate
																	// all
																	// states!
			Signal nextSignal = nextState.getSignal(instrument, period, askBar, bidBar, StateMachine.state);
			if (nextSignal != null && nextSignal.getValue() != 0) {
				logger.debug(nextState.getName() + " signal is " + nextSignal.getValue() + " (" + instrument.name()
						+ ")");
				if (nextSignal.getValue() > bestSignal.getValue()) {
					bestState = nextState;
					bestSignal = nextSignal;
				}
			}
		}

		if (bestState != null) {
			logger.info(bestState.getName() + " state is selected with " + bestSignal.getValue());
			bestState.prepareCommands(bestSignal);
			if (bestState.executeCommands()) {
				changeState(bestState);
				for (IChart c : StateMachine.getInstance().getCharts()) {
					IScreenLabelChartObject label = c.getChartObjectFactory().createScreenLabel("screenLabel");
					label.setCorner(Corner.TOP_LEFT);
					label.setxDistance(5);
					label.setyDistance(5);
					label.setText("$ " + context.getAccount().getBalance(), new Font(Font.MONOSPACED, Font.PLAIN, 12));
					label.setColor(Color.BLACK);

					if (c.getInstrument().equals(Instrument.EURUSD)) {
						c.add(label);
					}

				}
			}
		} else {
			Main.massInfo(logger, "Searching for promising state(s)...");
		}
	}

	@Override
	public void onMessage(IMessage message) throws JFException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAccount(IAccount account) throws JFException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStop() throws JFException {
		if (context.getEngine().getOrders().size() != 0) {
			for (IOrder o : StateMachine.getInstance().getOrders()) {
				logger.info("profit for #" + o.getId() + " is $" + o.getProfitLossInUSD());
			}
			logger.error("Closing all remaining orders!");
			context.getEngine().closeOrders(context.getEngine().getOrders());
		}

	}

	public void addChart(IChart chart) {
		charts.add(chart);
	}

	public Collection<IChart> getCharts() {
		return charts;
	}
}
