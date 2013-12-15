package hu.fnf.devel.forex;

import hu.fnf.devel.forex.states.MACDSample452State;
import hu.fnf.devel.forex.states.ScalpHolder7State;
import hu.fnf.devel.forex.states.SignalSeekerState;
import hu.fnf.devel.forex.utils.RobotException;
import hu.fnf.devel.forex.utils.Signal;
import hu.fnf.devel.forex.utils.State;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

	public static final int OPEN = 0;
	public static final int CLOSE = 1;
	public static final double BRAVE_VALUE = 0.6;

	private static StateMachine instance;
	private static State state;
	private static State nextState;
	private static boolean stateLock = false;
	private IContext context;
	private Map<IOrder, Period> database;
	private double startBalance;

	private Collection<IChart> charts = new ArrayList<IChart>();

	private StateMachine() {
		database = new HashMap<IOrder, Period>();
	}

	public void setStartBalance(double startBalance) {
		this.startBalance = startBalance;
	}

	public double getStartBalance() {
		return startBalance;
	}

	/*
	 * THREAD SAFE!!!
	 */
	public synchronized static StateMachine getInstance() {
		if (instance == null) {
			instance = new StateMachine();
		}
		return instance;
	}

	public static synchronized void changeState(State newState) throws RobotException {
		if (newState == null) {
			return;
		}
		if (StateMachine.state != null && !StateMachine.state.getName().equalsIgnoreCase(newState.getName())) {
			logger.info("state change " + StateMachine.state.getName() + " -> " + newState.getName());
			if (!StateMachine.state.onLeaving()) {
				throw new RobotException("cannot leave " + StateMachine.state.getName() + " state.");
			}
		}
		if (!newState.onArriving()) {
			throw new RobotException("cannot arrive to " + newState.getName() + " state.");
		}
		setState(newState);
	}

	public static void setNextState(State nextState) throws JFException {
		if (!isStateLock()) {
			setStateLock(true);
			StateMachine.nextState = nextState;
		} else {
			throw new JFException("state is locked.");
		}
	}

	/*
	 * --------- END ---------
	 */
	public static void setState(State state) {
		StateMachine.state = state;
		setStateLock(false);
	}

	public State getState() {
		return state;
	}

	public static State getNextState() {
		return nextState;
	}

	public static boolean isStateLock() {
		return stateLock;
	}

	public static void setStateLock(boolean stateLock) {
		StateMachine.stateLock = stateLock;
	}

	public void pushDatabase(IOrder order, Period period) {
		logger.debug("pushed to database.");
		database.put(order, period);
	}

	public Period getPeriod(IOrder order) {
		return database.get(order);
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
		setStartBalance(context.getAccount().getBalance());

		try {
			changeState(recignizeState());
		} catch (RobotException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
		label.setText(c.getInstrument().name() + " " + c.getFeedDescriptor().getPeriod().name(), new Font(
				Font.MONOSPACED, Font.PLAIN, 12));
		label.setColor(Color.BLACK);
		c.add(label);

		c.add(context.getIndicators().getIndicator("BBANDS"), new Object[] { 50, 2.0, 2.0, 0 });
		c.add(context.getIndicators().getIndicator("BBANDS"), new Object[] { 50, 2.2, 2.2, 0 });
		c.add(context.getIndicators().getIndicator("RSI"), new Object[] { 50 });
		//c.add(context.getIndicators().getIndicator("MACD"), new Object[] { 12, 26, 9 });

	}

	@Override
	public void onTick(Instrument instrument, ITick tick) throws JFException {
		Map<State, Signal> children = new HashMap<State, Signal>();
		for (State childState : StateMachine.state.getNextStates()) { // instantiate
																		// all
																		// states!
			children.put(childState, childState.getSignal(instrument, tick, StateMachine.state));
		}

		selectBestTransition(children);
	}

	private void selectBestTransition(Map<State, Signal> children) throws JFException {
		State bestState = null;
		Signal bestSignal = new Signal();
		for (State state : children.keySet()) {
			Signal signal = children.get(state);
			if (signal != null && signal.getValue() != 0) {
				logger.debug(" signal is " + signal.getValue() + "(" + signal.getInstrument().name() + ")");
				if (signal.getValue() > bestSignal.getValue()) {
					bestState = state;
					bestSignal = signal;
				}
			}
		}

		if (bestState != null && bestSignal != null) {
			if (bestSignal.getValue() >= BRAVE_VALUE) {
				logger.info(bestState.getName() + " state is selected with " + bestSignal.getValue());
				try {
					setNextState(bestState);
				} catch (JFException e) {
					if (e.getMessage().contains("locked")) {
						logger.debug("State change already in progress. Dropping this change thread!");
						return;
					} else {
						throw e;
					}
				}
				bestState.prepareCommands(bestSignal);
				if (!bestState.executeCommands()) {
					logger.fatal("Cannot execute best state commands! Trying to recognize actual state...");
					try {
						setNextState(recignizeState());
					} catch (JFException e) {
						if (e.getMessage().contains("locked")) {
							logger.debug("State change already in progress. Dropping this change thread!");
							return;
						} else {
							throw e;
						}
					}
				}
			} else {
				logger.debug(bestState.getName() + ": " + bestSignal.getValue());
			}
		} else {
			Main.massInfo(logger, "Searching for promising state(s)...");
		}

	}

	@Override
	public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
		// if (period.equals(Period.FIFTEEN_MINS)) {
		// for (IChart c : StateMachine.getInstance().getCharts()) {
		// IScreenLabelChartObject label =
		// c.getChartObjectFactory().createScreenLabel("screenLabel");
		// label.setCorner(Corner.TOP_LEFT);
		// label.setxDistance(5);
		// label.setyDistance(5);
		// StringBuilder l = new StringBuilder().append("$ " +
		// context.getAccount().getBalance());
		// if (StateMachine.getInstance().getOrders().size() > 0) {
		// for (IOrder o : StateMachine.getInstance().getOrders()) {
		// l.append("    #" + o.getId() + "    " + o.getProfitLossInUSD() + "$/"
		// + StateMachine.getInstance().getContext().getAccount().getBalance()
		// * StateMachine.BRAVE_VALUE * 0.4 + "/"
		// + StateMachine.getInstance().getContext().getAccount().getBalance()
		// * StateMachine.BRAVE_VALUE * -0.2);
		// }
		// }
		// label.setText(l.toString(), new Font(Font.MONOSPACED, Font.PLAIN,
		// 12));
		// label.setColor(Color.BLACK);
		//
		// if (c.getInstrument().equals(Instrument.EURUSD)) {
		// c.add(label);
		// }
		// }
		// }
		Set<Signal> signals = new HashSet<Signal>();
		for (State nextState : StateMachine.state.getNextStates()) { // instantiate
																		// all
																		// states!
			signals.add(nextState.getSignal(instrument, period, askBar, bidBar, StateMachine.state));
		}
	}

	@Override
	public void onMessage(IMessage message) throws JFException {
		switch (message.getType()) {
		case ORDER_SUBMIT_OK:
			logger.info("Order #" + message.getOrder().getId() + " submitted: " + message.getOrder());
			try {
				// for (IOrder order : database.keySet()) {
				// logger.info("oder #" + order.getId() + " state: " +
				// order.getState());
				// }
				changeState(getNextState());
			} catch (RobotException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case ORDER_SUBMIT_REJECTED:
			logger.info("Order #" + message.getOrder().getId() + " submitted: " + message.getOrder());
			try {
				logger.info("change back");
				changeState(recignizeState());
			} catch (RobotException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case ORDER_FILL_OK:
			logger.info("Order filled: " + message.getOrder());
			break;
		case ORDER_FILL_REJECTED:
			logger.info("Order cancelled: " + message.getOrder());
			try {
				logger.info("change back");
				changeState(recignizeState());
			} catch (RobotException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case CALENDAR:
			break;
		case CONNECTION_STATUS:
			break;
		case INSTRUMENT_STATUS:
			break;
		case MAIL:
			break;
		case NEWS:
			break;
		case NOTIFICATION:
			break;
		case ORDERS_MERGE_OK:
			break;
		case ORDERS_MERGE_REJECTED:
			break;
		case ORDER_CHANGED_OK:
			break;
		case ORDER_CHANGED_REJECTED:
			break;
		case ORDER_CLOSE_OK:
			logger.info("Order closed: " + message.getOrder());
			try {
				// for (IOrder order : database.keySet()) {
				// logger.info("oder #" + order.getId() + " state: " +
				// order.getState());
				// }
				changeState(getNextState());
			} catch (RobotException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case ORDER_CLOSE_REJECTED:
			logger.info("Order close failed: " + message.getOrder());
			try {
				logger.info("change back");
				changeState(recignizeState());
			} catch (RobotException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case SENDING_ORDER:
			break;
		case STOP_LOSS_LEVEL_CHANGED:
			break;
		case STRATEGY_BROADCAST:
			break;
		case WITHDRAWAL:
			break;
		default:
			break;
		}
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
		logger.info("Balance is: $" + context.getAccount().getBalance());
	}

	public void addChart(IChart chart) {
		charts.add(chart);
	}

	public Collection<IChart> getCharts() {
		return charts;
	}

}
