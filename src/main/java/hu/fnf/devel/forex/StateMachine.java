package hu.fnf.devel.forex;

import hu.fnf.devel.forex.database.Orders;
import hu.fnf.devel.forex.database.Order;
import hu.fnf.devel.forex.database.Strategy;
import hu.fnf.devel.forex.states.SignalSeekerState;
import hu.fnf.devel.forex.utils.RobotException;
import hu.fnf.devel.forex.utils.Signal;
import hu.fnf.devel.forex.utils.State;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

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
	public static final int TREND = 2;
	public static final int CHAOS = 3;
	public static final double BRAVE_VALUE = 0.6;
	private static final int maxOrderResubmitCount = 5;

	private static StateMachine instance;
	private static State state;
	private static State nextState;
	private static boolean stateLock = false;
	
	private final Orders orders = new Orders();
	private IContext context;
	
	
	private double startBalance;

	private Collection<IChart> charts = new ArrayList<IChart>();
	private Map<IOrder, Integer> resubmitAttempts = new HashMap<IOrder, Integer>();
	
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
		if (StateMachine.state != null && newState != null
				&& !StateMachine.state.getName().equalsIgnoreCase(newState.getName())) {
			/*
			 * real state change
			 */
			logger.info("------------- " + StateMachine.state.getName() + " -> " + newState.getName()
					+ " -------------");
			if (!StateMachine.state.onLeaving()) {
				throw new RobotException("cannot leave " + StateMachine.state.getName() + " state.");
			}
			if (!newState.onArriving()) {
				throw new RobotException("cannot arrive to " + newState.getName() + " state.");
			}
			setState(newState);
		} else {
			if ( StateMachine.state == null && newState != null ) {
				if (!newState.onArriving()) {
					throw new RobotException("cannot arrive to " + newState.getName() + " state.");
				}
				setState(newState);
				logger.info("------------- initalize state -> " + StateMachine.state.getName() + " -------------");
			} else {
				setStateLock(false);
			}
		}
	}

	/*
	 * --------- END ---------
	 */

	/*
	 * static
	 */

	public static void setNextState(State nextState) throws JFException {
		if (!isStateLock()) {
			setStateLock(true);
			StateMachine.nextState = nextState;
		} else {
			throw new JFException("state is locked.");
		}
	}
	
	/*
	 * public
	 */
	public void setStartBalance(double startBalance) {
		this.startBalance = startBalance;
	}

	public double getStartBalance() {
		return startBalance;
	}

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

	public void pushPosition(IOrder order, Period period) {
		orders.add(order, period);
		resubmitAttempts.put(order, 1);
	}

	public Period getPeriod(IOrder order) {
		return Period.valueOf(orders.get(order.getCreationTime()).getPeriod());
	}

	public IContext getContext() {
		return context;
	}

	private State recignizeState() throws JFException {
		// TODO: recognize states
		State ret = null;

		if (context.getEngine().getOrders().size() == 0) {
			ret = new SignalSeekerState();
			logger.info("------------- " + ret.getName() + " state recognized!");
			return ret;
		} else {
			logger.fatal("No alg. to determine state!");
			return null;
		}

	}

	@Override
	public void onStart(IContext context) throws JFException {
		
		if (!context.isFullAccessGranted()) {
			logger.fatal("Full access need to run this strategy!");
			throw new JFException("Full access need to run this strategy!");
		}
		this.context = context;
		checkStartEnvironment();

		try {
			changeState(recignizeState());
		} catch (RobotException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void checkStartEnvironment() {
		setStartBalance(context.getAccount().getBalance());
//		try {
//			positions.addAll(context.getEngine().getOrders());
//		} catch (JFException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		if (context.getEngine().getType().equals(IEngine.Type.TEST)) {
			for (IChart c : charts) {
				setChartDecoration(c);
			}
		}
		logger.info("account id:		    " + context.getAccount().getAccountId());
		logger.info("account state:         " + context.getAccount().getAccountState());
		logger.info("account balance:	    " + context.getAccount().getBalance() + " "
				+ context.getAccount().getCurrency().getCurrencyCode());
		logger.info("current leverage:	    1:" + context.getAccount().getLeverage());
		logger.info("account is global:	    " + context.getAccount().isGlobal());
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

		// c.add(context.getIndicators().getIndicator("BBANDS"), new Object[] {
		// 50, 2.0, 2.0, 0 });
		// c.add(context.getIndicators().getIndicator("BBANDS"), new Object[] {
		// 50, 2.2, 2.2, 0 });
		// c.add(context.getIndicators().getIndicator("ADX"), new Object[] { 13
		// });
		// c.add(context.getIndicators().getIndicator("MACD"), new Object[] {
		// 12, 26, 9 });

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

	@Override
	public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
		Map<State, Signal> children = new HashMap<State, Signal>();
		for (State childState : StateMachine.state.getNextStates()) { // instantiate
																		// all
																		// states!
			children.put(childState, childState.getSignal(instrument, period, askBar, bidBar, StateMachine.state));
//			logger.debug("state: " + childState);
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
	public void onMessage(IMessage message) throws JFException {
		/*
		 * http://www.dukascopy.com/wiki/files/Market%20Order%20States%20Diagram%2030.05.2012.pdf
		 */
		if (message.getOrder() == null) {
			/*
			 * not order message
			 */
			if ( message.getContent() != null ) {
				logger.info("Message: " + message.getContent());
			}
			if (message.getReasons().size() != 0) {
				logger.info("Reasons: " + message.getReasons());
			}
			return;
		}
		IOrder iorder = message.getOrder();
		
		Orders.userTransaction.begin();
		Order morder = orders.get(iorder.getCreationTime());
		morder.setLaststate(iorder.getState().name());
		Orders.userTransaction.commit();
		/*
		 * order message
		 */
//		if (message.getOrder().getId() != null && !positions.contains(message.getOrder())) {
//			logger.warn("Not managed order event happened! Order#" + message.getOrder().getId() + ": "
//					+ message.getContent());
//			return;
//		} else {
			if (message.getOrder().getState() == IOrder.State.CLOSED) {
				/*
				 * closing procedure
				 */
				Order o = orders.get(message.getOrder().getCreationTime());
				logger.info("Order #" + o.getOrderid() + " removed from memory.");
			}
//		}
		switch (message.getType()) {
		case ORDER_SUBMIT_OK:
			orderMessage(message.getOrder(), " submitted: ");
			break;
		case ORDER_SUBMIT_REJECTED:
			orderMessage(message.getOrder(), " rejected: ");
			Integer attempts = resubmitAttempts.get(message.getOrder());
			if (attempts > maxOrderResubmitCount) {
				logger.error("Rejected order has exceeeded resubmit attempt count. Rollback!");
				logger.error("Reason: " + message.getReasons());
				try {
					changeState(recignizeState());
				} catch (RobotException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				resubmitAttempts.remove(message.getOrder());
				IOrder newOrder = StateMachine
						.getInstance()
						.getContext()
						.getEngine()
						.submitOrder(message.getOrder().getLabel(), message.getOrder().getInstrument(),
								message.getOrder().getOrderCommand(), message.getOrder().getAmount() + 0.001);
				resubmitAttempts.put(newOrder, ++attempts);
				logger.warn("Resubmitted order: " + newOrder + " attempts left: "
						+ (maxOrderResubmitCount - attempts + 1));
			}

			break;
		case ORDER_FILL_OK:
			orderMessage(message.getOrder(), " filled:    ");
			try {
				changeState(getNextState());
			} catch (RobotException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case ORDER_FILL_REJECTED:
			orderMessage(message.getOrder(), " cancelled: ");
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
			orderMessage(message.getOrder(), " closed:   ");
			logger.info("New balance:............................................ $"
					+ StateMachine.getInstance().getContext().getAccount().getBalance());
			try {
				changeState(getNextState());
			} catch (RobotException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case ORDER_CLOSE_REJECTED:
			orderMessage(message.getOrder(), " close err: ");
			Integer attemptz = resubmitAttempts.get(message.getOrder());
			if (attemptz > maxOrderResubmitCount) {
				logger.error("Rejected order has exceeeded resubmit attempt count. Rollback!");
				logger.error("Reason: " + message.getReasons());
				try {
					changeState(recignizeState());
				} catch (RobotException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				resubmitAttempts.remove(message.getOrder());
				IOrder newOrder = StateMachine
						.getInstance()
						.getContext()
						.getEngine()
						.submitOrder(message.getOrder().getLabel(), message.getOrder().getInstrument(),
								message.getOrder().getOrderCommand(), message.getOrder().getAmount() + 0.001);
				resubmitAttempts.put(newOrder, ++attemptz);
				logger.warn("Resubmitted order: " + newOrder + " attempts left: "
						+ (maxOrderResubmitCount - attemptz + 1));
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

	private void orderMessage(IOrder order, String string) {
		if (order.getId() != null) {
			logger.info("Order #" + order.getId() + " @ " + order.getInstrument() + string + order);
		} else {
			logger.info("Order ctime-" + order.getCreationTime() + " @ " + order.getInstrument() + string + order);
		}
	}

	@Override
	public void onAccount(IAccount account) throws JFException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStop() throws JFException {
		if (context.getEngine().getOrders().size() != 0) {
			for (IOrder o : StateMachine.getInstance().getContext().getEngine().getOrders()) {
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
