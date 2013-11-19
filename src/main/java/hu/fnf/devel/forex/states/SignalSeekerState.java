package hu.fnf.devel.forex.states;

import hu.fnf.devel.forex.StateMachine;
import hu.fnf.devel.forex.utils.Signal;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.dukascopy.api.IBar;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;

public class SignalSeekerState extends State {
	private static final Logger LOGGER = Logger.getLogger(SignalSeekerState.class);

	public SignalSeekerState() {
		super("SignalSeekerState");
	}

	@Override
	public Set<State> nextStates() {
		// TODO: make more logic
		Set<State> ret = new HashSet<State>();
		ret.add(new ScalpHolder7State());
		ret.add(new MACDSample452State());
		return ret;
	}

	@Override
	public boolean onArriving() {
//		if (StateMachine.getInstance().getContext() != null) {
//			for (String s : StateMachine.getInstance().getContext().getIndicators().getAllNames()) {
//				LOGGER.debug("ind: " + s);
//			}
//		}
		LOGGER.info("type: " + StateMachine.getInstance().getContext().getEngine().getType());
		if (StateMachine.getInstance().getContext().getEngine().getType().equals(IEngine.Type.TEST)) {
			LOGGER.info("Not subscribing to instruments for " + getName());
			return true;
		} else {
			LOGGER.info("Subscribing to instruments for " + getName());
			// IEngine.Type getType()
			// Returns type of the engine, one of the IEngine.Type.LIVE,
			// IEngine.Type.DEMO or IEngine.Type.TEST for tester.
			instruments = new HashSet<Instrument>();
			for (State s : nextStates()) {
				for (Instrument i : s.getInstruments()) {
					instruments.add(i);
				}
			}

			for (Instrument i : instruments) {
				LOGGER.debug("\t-" + i.name());
			}

			StateMachine.getInstance().getContext().setSubscribedInstruments(instruments);
			return true;
		}
	}

	@Override
	public boolean onLeaving() {
		// removing references
		this.commands = null;
		this.instruments = null;
		this.periods = null;
		this.signal = null;
		return true;
	}

	@Override
	public Signal signalStrength(Instrument instrument, ITick tick, State actual) throws JFException {
		// LOGGER.debug("candidate: " + getName() + " actual: " +
		// actual.getName() + " signalStrength calculation ");
		if (actual instanceof ScalpHolder7State || actual instanceof MACDSample452State) {
			return actual.signalStrength(instrument, tick, null);
		}
		return new Signal();
	}

	@Override
	public Signal signalStrength(Instrument instrument, Period period, IBar askBar, IBar bidBar, State actual)
			throws JFException {
		Signal ret = new Signal();
		ret.setValue(0);
		return ret;
	}

}
