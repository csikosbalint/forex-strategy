package hu.fnf.devel.forex;

import hu.fnf.devel.forex.states.State;
import hu.fnf.devel.forex.states.SignalSeekerState;
import hu.fnf.devel.forex.strategies.Strategy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;

public class StateMachine implements IStrategy {
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	private static StateMachine instance;
	private static State state;
	private Set<Strategy> strategies;
	private IContext context;
	// TODO: strategy cannot have the orders (this wont be destroyed!)
	private List<IOrder> orders = new ArrayList<IOrder>();

	// synchronized if needed
	public static StateMachine getInstance() {
		if (instance == null) {
			instance = new StateMachine();
		}
		return instance;
	}
	/*
	 * THREAD SAFE!!!
	 */
	public static synchronized void setState(State state) {
		LOGGER.debug("received state for setState is " + state.getName() + ". Actual state is "
				+ StateMachine.state.getName());
		if (StateMachine.state != null && StateMachine.state.isAllowed(state)) {
			LOGGER.info("state change: " + StateMachine.state.getName() + " -> " + state.getName());
			StateMachine.state = state;
		}
	}

	public State getState() {
		return state;
	}

	public IContext getContext() {
		return context;
	}

	public Set<Strategy> getStrategies() {
		return strategies;
	}
	
	public List<IOrder> getOrders() {
		return orders;
	}
	public void setOrders(List<IOrder> orders) {
		this.orders = orders;
	}
	public synchronized void  addOrders(IOrder order) {
		if ( !orders.contains(order) ) {
			orders.add(order);
		}
	}
	private State recignizeState(Set<Strategy> strategies) {
		// TODO: recognize states
		/*
		 * For now every start assumed a new beginning
		 */
		/*
		 * return new ScalpHolderState(new ScalpingStrategy());
		 */
		SignalSeekerState signalSeekerState = new SignalSeekerState();
		for (Strategy s : strategies) {
			signalSeekerState.addStrategy(s);
		}
		return signalSeekerState;
	}

	@Override
	public void onStart(IContext context) throws JFException {
		this.context = context;
		LOGGER.debug("state recognition...");
		StateMachine.state = recignizeState(strategies);
		LOGGER.info("recognized state is " + state.getName());
		Set<Instrument> instruments = new HashSet<Instrument>();
		LOGGER.info("subscribing to instruments:");
		for (Strategy s : strategies) {
			for (Instrument i : s.getInstruments()) {
				instruments.add(i); // no duplicate
				LOGGER.info("\t*" + i.name() + "(" + s.getName() + ")");
			}
		}
		context.setSubscribedInstruments(instruments);
	}

	/*
	 * Action
	 */

	@Override
	public void onTick(Instrument instrument, ITick tick) throws JFException {
		LOGGER.debug(StateMachine.state.getName() + " tick " + instrument.name() );
		StateMachine.state.transaction(instrument, tick);
		
	}

	@Override
	public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
		//LOGGER.debug(StateStrategy.state.getName() + " onBar tick " + instrument.name() + "/" + period.getInterval());
		
		//StateStrategy.state.transaction(instrument, period, askBar, bidBar);
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
		// TODO Auto-generated method stub

	}

	public void addStrategy(Strategy strategy) {
		if (strategies == null) {
			strategies = new HashSet<Strategy>();
		}
		strategies.add(strategy);
	}

}
