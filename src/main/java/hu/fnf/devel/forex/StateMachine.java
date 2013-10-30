package hu.fnf.devel.forex;

import hu.fnf.devel.forex.states.SignalSeekerState;
import hu.fnf.devel.forex.states.State;

import java.util.ArrayList;
import java.util.List;

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

	private List<IOrder> orders;
	private IContext context;

	// synchronized if needed
	public static StateMachine getInstance() {
		if (instance == null) {
			instance = new StateMachine();
		}
		return instance;
	}

	/*
	 * THREAD SAFE SETTER!!!
	 */
	public static synchronized void changeState(State newState) {
		LOGGER.debug(StateMachine.state.getName() + ".setState(" + newState.getName() + ")");
		if (StateMachine.state != null && StateMachine.state != newState) {
			LOGGER.info("state change " + StateMachine.state.getName() + " -> " + newState.getName());
			if ( StateMachine.state.onLeaving() && newState.onArriving() ) {
				StateMachine.state = newState;
			}
		}
	}

	public synchronized void addOrder(IOrder order) {
		if (!orders.contains(order)) {
			orders.add(order);
		}
	}

	public State getState() {
		return state;
	}

	public List<IOrder> getOrders() {
		return orders;
	}

	public IContext getContext() {
		return context;
	}

	private State recignizeState() {
		// TODO: recognize states
		/*
		 * For now every start assumed a new beginning
		 */
		/*
		 * return new ScalpHolderState(new ScalpingStrategy());
		 */
		SignalSeekerState signalSeekerState = new SignalSeekerState();
		return signalSeekerState;
	}

	@Override
	public void onStart(IContext context) throws JFException {
		this.context = context;
		this.orders = context.getEngine().getOrders();
		StateMachine.state = recignizeState();
		LOGGER.info("Initalization");
		LOGGER.debug("\tstate:\t" + state.getName());
		LOGGER.debug("\torders:\t" + orders.size());
	}

	@Override
	public void onTick(Instrument instrument, ITick tick) throws JFException {
		changeState(StateMachine.state.transaction(instrument, tick));
	}

	@Override
	public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {

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

}
