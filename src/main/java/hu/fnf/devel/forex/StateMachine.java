package hu.fnf.devel.forex;

import hu.fnf.devel.forex.states.ScalpHolderState;
import hu.fnf.devel.forex.states.SignalSeekerState;
import hu.fnf.devel.forex.states.State;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

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
	private static final Logger LOGGER = Logger.getLogger(Main.class);

	public static final int OPEN = 0;
	public static final int CLOSE = 1;

	private static StateMachine instance;
	private static State state;

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
		if (newState == null) {
			return;
		}
		if (StateMachine.state != null && !StateMachine.state.getName().equalsIgnoreCase(newState.getName())) {
			LOGGER.info("state change " + StateMachine.state.getName() + " -> " + newState.getName());
			if (StateMachine.state.onLeaving() && newState.onArriving()) {
				StateMachine.state = newState;
			}
		} else {
			if (newState.onArriving()) {
				StateMachine.state = newState;
			}
		}
	}

	public State getState() {
		return state;
	}

	public List<IOrder> getOrders() {
		try {
			return context.getEngine().getOrders();
		} catch (JFException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ArrayList<IOrder>();
		}
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
		try {
			if (context.getEngine().getOrders().size() == 0) {
				return new SignalSeekerState();
			} else {
				return new ScalpHolderState();
			}
		} catch (JFException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void onStart(IContext context) throws JFException {
		this.context = context;
		LOGGER.debug(context.getAccount().getLeverage());
		changeState(recignizeState());
	}

	@Override
	public void onTick(Instrument instrument, ITick tick) throws JFException {
		// changeState(StateMachine.state.transaction(instrument, tick));
		State bestState = null;
		Signal bestSignal = new Signal();
		for (State nextState : StateMachine.state.nextStates()) { // instantiate
																	// all
																	// states!!
			LOGGER.debug("checking " + nextState.getName());
			// TODO:
			// for (Instrument i : nextState.getInstruments()) {
			// if (instrument.equals(i)) {
			Signal nextSignal = nextState.signalStrength(instrument, tick, StateMachine.state);
			LOGGER.debug(nextState.getName() + " signal strength: " + nextSignal.getValue());
			if (nextSignal.getValue() > bestSignal.getValue()) {
				bestState = nextState;
				bestSignal = nextSignal;
				LOGGER.debug(nextState.getName() + " is the new max with " + nextSignal.getValue());
			}
			// }
			// }
		}
		if (bestState != null) {
			bestState.prepareCommands(bestSignal);
			if (bestState.executeCommands()) {
				changeState(bestState);
			}
		}
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
