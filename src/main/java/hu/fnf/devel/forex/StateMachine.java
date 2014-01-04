package hu.fnf.devel.forex;

import hu.fnf.devel.forex.states.ExitState;
import hu.fnf.devel.forex.states.MACDSample452State;
import hu.fnf.devel.forex.states.PanicState;
import hu.fnf.devel.forex.states.ScalpHolder7State;
import hu.fnf.devel.forex.states.SignalSeekerState;
import hu.fnf.devel.forex.states.ThreeLittlePigsState;
import hu.fnf.devel.forex.utils.RobotException;
import hu.fnf.devel.forex.utils.Signal;
import hu.fnf.devel.forex.utils.State;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

import org.apache.log4j.Logger;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IChart;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IEngine.OrderCommand;
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

	public static final int OPEN = 1;
	public static final int CLOSE = 2;
	public static final int TREND = 3;
	public static final int CHAOS = 4;
	
	public static final int ADD = 1;
	public static final int UPDATE = 2;
	public static final int REMOVE = 3;
	
	public static final double BRAVE_VALUE = 0.6;
	private static final int maxOrderResubmitCount = 5;

	private static StateMachine instance;
	private static State state;
	private static State nextState;
	private static Set<State> allStates = new HashSet<State>();
	private static boolean stateLock = false;
	
	private IContext context;
	
	private double 	startBalance;
	private int		startID;

	private Collection<IChart> charts = new ArrayList<IChart>();
	public static Map<IOrder, Integer> resubmitAttempts = new HashMap<IOrder, Integer>();
	
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
	
	public static synchronized State getStateInstance(String stateName) {
		/*
		 * ..in instances
		 */
		for (State state : StateMachine.getAllStates()) {
			if ( state.getName().equalsIgnoreCase(stateName) ) {
				return state;
			}
		}
		/*
		 * ..creating a new instance
		 */
		logger.debug("searching for state \"" + stateName + "\"");
		State state = null;
		if (stateName == null ) {
			return null;
		} else if (stateName.contains("SignalSeekerState")) {
			state = new SignalSeekerState();
		} else if (stateName.contains("MACDSample452State")) {
			state = new MACDSample452State();
		} else if (stateName.contains("ScalpHolder7State")) {
			state = new ScalpHolder7State();
		} else if (stateName.contains("ThreeLittlePigsState")) {
			state = new ThreeLittlePigsState();
		} else if (stateName.contains("SignalSeekerState")) {
			state = new SignalSeekerState();
		} else if (stateName.contains("PanicState")) {
			state = new PanicState();
		} else if (stateName.contains("ExitState")) {
			state = new ExitState();
		}
		/*
		 * ..what if state is not allowed?
		 */
		logger.debug("new state \"" + stateName + "\" created.");
		allStates.add(state);
		return state;
		/*
		 * and maybe from text instantly if state is allowed
		 * Class<?> clazz = Class.forName(className);
		 * Constructor<?> ctor = clazz.getConstructor(String.class);
		 * Object object = ctor.newInstance(new Object[] { ctorArgument });
		 */
	}

	/*
	 * --------- END ---------
	 */

	/*
	 * static
	 */

	public static void setNextState(State nextState) throws RobotException {
		if (!isStateLock()) {
			setStateLock(true);
			StateMachine.nextState = nextState;
		} else {
			throw new RobotException("State change in progress. Next state is alredy locked!");
		}
	}
	
	/*
	 * private
	 */
	
	private void stateTraversal(State s) {
		Stack<State> todo = new Stack<State>();
		Stack<State> done = new Stack<State>();
		todo.addAll(s.getNextStates());
		while (!todo.empty()) {
			State pick = todo.pop();
			if ( !done.contains(pick) ) {
				done.add(pick);
				logger.debug("visit: " + pick.getName());
				if ( pick.getNextStates() != null && !todo.containsAll(pick.getNextStates())) { 
					todo.addAll(pick.getNextStates());
				}
			}
		}
	}
	
	/*
	 * public
	 */
	
	public static Set<State> getAllStates() {
		return allStates;
	}
	
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

	public Period getPeriod(IOrder order) {
		return Period.valueOf(order.getLabel().split("AND")[1]);
	}

	public IContext getContext() {
		return context;
	}

	private State recognizeState() throws RobotException, JFException{
		State ret = null;
		switch (context.getEngine().getOrders().size()) {
		case 0|1:
			/*
			 * no order..starting state
			 */
			ret = new SignalSeekerState();
			break;
		case 2:
			/*
			 * one order..trying to determine state
			 */
			IOrder iOrder = null;
			for (IOrder o: context.getEngine().getOrders() ) {
				if ( !o.getLabel().contains("START") ) {
					iOrder = o;
					break;
				}
			}
			if ( !iOrder.getLabel().contains("AND") ) {
				Main.printDetails(iOrder);
				throw new RobotException("Unmanaged order detected ctime:" + iOrder.getCreationTime() + " id #"
						+ iOrder.getId());
			}
			ret = StateMachine.getStateInstance(iOrder.getLabel().split("AND")[0]);
			if ( ret != null && nextState != null && !ret.getName().contains(nextState.getName()))  {
				logger.info("Not the same next and recognized: " + ret.getName() + "/" + nextState.getName());
			}

//			Order order = Database.get(iOrder.getCreationTime());
//			Database.add(order);
			break;
		default:
			/*
			 * more order..some complex state or unmanaged orders
			 */
			break;
		}
		
		logger.info("------------- " + ret.getName() + " state is recognized!");
		return ret;
	}

	@Override
	public void onStart(IContext context) throws JFException {
		this.context = context;
		startID = (new Random()).nextInt(1000);
		try {
			checkCapabilities();
		} catch (RobotException e) {
			if ( e.getMessage().contains("running")) {
				logger.fatal("Mandatory capabality check failed!", e);
				System.exit(-1);
			} else if ( e.getMessage().contains("database") ) {
				logger.debug("No database connection...robot still can go on...");
			}
			logger.warn("Some capabilities are not available", e);
		}

		try {
			changeState(recognizeState());
		} catch (RobotException e) {
			logger.fatal("Cannot change or recognize state onStart. Running strategy is not advised!", e);
			System.exit(-1);
		}
		/*
		 * gathering all possible states
		 */
		logger.info("Traversing the state graph...");
		stateTraversal(state);
		
		for (State state : allStates) {
			logger.info("\t" + state.getName());
		}
		/*
		 * subscribing
		 */

		logger.info("Subscribing to instruments...");

		/*
		 * subscribe to instruments related to next states
		 */
		Set<Instrument> instruments = new HashSet<Instrument>();
		for (State s : StateMachine.getAllStates()) {
			for (Instrument i : s.getInstruments()) {
				instruments.add(i);
			}
		}
		for (Instrument instrument: instruments ) {
			logger.info("\t" + instrument.name());
		}
		context.setSubscribedInstruments(instruments);

		/*
		 * log start time,version and state to database
		 */
//		
//		Order order = new Order();
//		order.setStrategyname(state.getName());;
//		startTime = new Date().getTime();
//		order.setId(startTime);
//		order.setOrderid("VERSION"); // VERSION is replaced by build.sh
//		order.setPeriod(Period.TICK.name());
//		order.setLaststate(IOrder.State.CREATED.name());
//		try {
//			Database.add(order);
//		} catch (RobotException e) {
//			e.printStackTrace();
//		}
	}

	private void checkCapabilities() throws RobotException, JFException {
		if (!context.isFullAccessGranted()) {
			logger.fatal("Full access need to run this strategy!");
			throw new RobotException("Full access need to run this strategy!");
		}
		
		setStartBalance(context.getAccount().getBalance());

		if (context.getEngine().getType().equals(IEngine.Type.TEST)) {
			for (IChart c : charts) {
				setChartDecoration(c);
			}
		}
		/*
		 * check if this is the only robot at account
		 * if true, than mark the account by startID
		 */
		for (IOrder iterable : context.getEngine().getOrders()) {
			if ( iterable.getLabel().contains("START") ) {
				throw new RobotException("Robot with label \"" +  iterable.getLabel()+ "\" is already running!");
			}
		}
		
			Set<Instrument> eurusd = new HashSet<Instrument>();
			eurusd.add(Instrument.EURUSD);
			context.setSubscribedInstruments(eurusd);
			context.getEngine().submitOrder("START" + String.valueOf(startID), Instrument.EURUSD, OrderCommand.SELLLIMIT, 0.001, 2.500);
			context.setSubscribedInstruments(null);
		
//		logger.info("testing database:		id:DATEANDTIME"); // DATEANDTIME is replaced by build.sh
//
//		Order order = new Order();
//		order.setStrategyname("TestState");
//		order.setId(Long.valueOf("DATEANDTIME")); // DATEANDTIME is replaced by build.sh
//		order.setPeriod(Period.TICK.name());
//		order.setLaststate(IOrder.State.CREATED.name());
//		try {
//			Database.add(order);
//		} catch (RobotException e) {
//			throw new RobotException("Cannot add test row into database!",e);
//		}
//		order.setLaststate(IOrder.State.CANCELED.name());
//		try {
//			Database.merge(order);
//		} catch (RobotException e) {
//			throw new RobotException("Cannot modify test row in database!",e);
//		}
//		try {
//			Database.remove(order.getId());
//		} catch (RobotException e) {
//			throw new RobotException("Cannot remove test row from database!",e);
//		}
//		logger.info("database rw/mod:        OK");
//		try {
//			Main.sendMail("*****SPAM***** start", "start");
//		} catch ( RobotException e) {
//			logger.error("Cannot send mail!", e);
//		}
		logger.info("start ID:              " + startID);
		logger.info("mail send:              OK");
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
				} catch ( RobotException e) {
					e.printStackTrace();
				}
				bestState.prepareCommands(bestSignal);
				if (!bestState.executeCommands()) {
					logger.fatal("Cannot execute best state commands! Trying to recognize actual state...");
					try {
						setNextState(recognizeState());
					} catch (JFException e) {
						if (e.getMessage().contains("locked")) {
							logger.debug("State change already in progress. Dropping this change thread!");
							return;
						} else {
							throw e;
						}
					} catch (RobotException e) {
						
					}
				}
			} else {
				logger.debug(bestState.getName() + ": " + bestSignal.getValue());
			}
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
		if ( message.getOrder().getLabel().contains("START") ) {
			return;
		}
		/*
		 * order message
		 */
		if ( !message.getOrder().getLabel().contains("AND") ) {
			throw new JFException("Label does not contain keyword...this is not my order. Leaving this account...");
		}
		switch (message.getType()) {
		case ORDER_SUBMIT_OK:
			orderMessage(message.getOrder(), " submitted: ");
//			try {
//				Database.add(message.getOrder());
//			} catch (RobotException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			try {
				changeState(recognizeState());
			} catch (RobotException e) {
				if ( state != null ) {
					state.setPanic(true);
				}
				e.printStackTrace();
			}
			break;
		case ORDER_SUBMIT_REJECTED:
			orderMessage(message.getOrder(), " rejected: ");
			Integer attempts = resubmitAttempts.get(message.getOrder());
			if (attempts > maxOrderResubmitCount) {
				logger.error("Rejected order has exceeeded resubmit attempt count. Rollback!");
				logger.error("Reason: " + message.getReasons());
//				try {
//					Database.add(message.getOrder());
//				} catch (RobotException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				try {
//					Database.remove(message.getOrder());
//				} catch (RobotException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				try {
					changeState(recognizeState());
				} catch (RobotException e) {
					if ( state != null ) {
						state.setPanic(true);
					}
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
//			try {
//				Database.add(message.getOrder());
//			} catch (RobotException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			try {
				changeState(recognizeState());
			} catch (RobotException e) {
				if ( state != null ) {
					state.setPanic(true);
				}
			}
			break;
		case ORDER_FILL_REJECTED:
			orderMessage(message.getOrder(), " cancelled: ");
			try {
				logger.info("change back");
				changeState(recognizeState());
			} catch (RobotException e) {
				if ( state != null ) {
					state.setPanic(true);
				}
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
//			try {
//				Database.remove(message.getOrder());
//			} catch (RobotException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			logger.info("New balance:............................................ $"
					+ StateMachine.getInstance().getContext().getAccount().getBalance());
			try {
				changeState(recognizeState());
			} catch (RobotException e) {
				if ( state != null ) {
					state.setPanic(true);
				}
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
					changeState(recognizeState());
				} catch (RobotException e) {
					if ( state != null ) {
						state.setPanic(true);
					}
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
		logger.info(string);
		Main.printDetails(order);
	}

	@Override
	public void onAccount(IAccount account) throws JFException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStop() throws JFException {
		Set<Instrument> eurusd = new HashSet<Instrument>();
		eurusd.add(Instrument.EURUSD);
		context.setSubscribedInstruments(eurusd);
		context.getEngine().getOrder("START"+String.valueOf(startID)).close();
//		Order order = Database.get(startTime);
//		logger.info(order.getId());
//		order.setLaststate(IOrder.State.CLOSED.name());
//		order.setClose( (new Date()).getTime() );
//		order.setProfit(context.getAccount().getBaseEquity());
//		context.getEngine().getOrder("START").close();
//		try {
//			Database.merge(order);
//			Database.remove(order.getId());
//		} catch (RobotException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		logger.info("Balance is: $" + context.getAccount().getEquity());
	}

	public void addChart(IChart chart) {
		charts.add(chart);
	}

	public Collection<IChart> getCharts() {
		return charts;
	}

}
