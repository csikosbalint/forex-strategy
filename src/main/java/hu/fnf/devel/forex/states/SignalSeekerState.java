package hu.fnf.devel.forex.states;

import hu.fnf.devel.forex.StateMachine;
import hu.fnf.devel.forex.utils.Signal;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;


import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;

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
		return ret;
	}

	@Override
	public boolean onArriving() {
		LOGGER.info("Subscribing to instruments for " + getName());
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
		//LOGGER.debug("candidate: " + getName() + " actual: " + actual.getName() + " signalStrength calculation ");
		if (actual instanceof ScalpHolder7State) {
			return actual.signalStrength(instrument, tick, null);
		}
		return new Signal();
	}

}
