package hu.fnf.devel.forex;

import hu.fnf.devel.forex.states.State;
import hu.fnf.devel.forex.states.SignalSeekerState;
import hu.fnf.devel.forex.strategies.Strategy;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;

public class StateStrategy implements IStrategy {
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	private static StateStrategy instance;
	private static State state;
	private Set<Strategy> strategies;
	private IContext context;

	// synchronized if needed
	public static StateStrategy getInstance() {
		if (instance == null) {
			instance = new StateStrategy();
		}
		return instance;
	}
	/*
	 * THREAD SAFE!!!
	 */
	public static synchronized void setState(State state) {
		LOGGER.debug("received state for setState is " + state.getName() + ". Actual state is "
				+ StateStrategy.state.getName());
		if (StateStrategy.state != null && StateStrategy.state.isAllowed(state)) {
			LOGGER.info("state change: " + StateStrategy.state.getName() + " -> " + state.getName());
			StateStrategy.state = state;
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
		StateStrategy.state = recignizeState(strategies);
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
		LOGGER.debug(StateStrategy.state.getName() + " tick " + instrument.name() );
		StateStrategy.state.transaction(instrument, tick);
		
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
