package hu.fnf.devel.forex.states;

import hu.fnf.devel.forex.StateMachine;
import hu.fnf.devel.forex.utils.Signal;
import hu.fnf.devel.forex.utils.State;

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
	private static final Logger logger = Logger.getLogger(SignalSeekerState.class);

	public SignalSeekerState() {
		super("SignalSeekerState");
		/*
		 * config
		 */
		// SignalSeeker has no criteria
		// SignalSeeker has no intruments

		this.periods.add(Period.TICK);
	}

	@Override
	public boolean onArriving() {
		if (StateMachine.getInstance().getContext().getEngine().getType().equals(IEngine.Type.TEST)) {
			return true;
		} else {
			logger.info("Subscribing to instruments for " + getName());

			/*
			 * subscribe to instruments related to next states
			 */
			instruments = new HashSet<Instrument>();
			for (State s : getNextStates()) {
				for (Instrument i : s.getInstruments()) {
					instruments.add(i);
				}
			}

			for (Instrument i : instruments) {
				logger.debug("\t-" + i.name());
			}

			StateMachine.getInstance().getContext().setSubscribedInstruments(instruments);
			return true;
		}
	}

	@Override
	public boolean onLeaving() {
		// removing references
		this.instruments = null;
		this.periods = null;
		this.signal = null;
		return true;
	}

	@Override
	public Signal getSignal(Instrument instrument, ITick tick, State actual) throws JFException {
		if (actual.getInstruments().contains(instrument)) {
			if (StateMachine.getInstance().getContext().getEngine().getOrders().size() > 0
					&& StateMachine.getInstance().getContext().getEngine().getOrders().get(0).getInstrument()
							.equals(instrument)) {
				Signal challenge = new Signal(instrument, StateMachine.getInstance().getContext().getEngine()
						.getOrders().get(0).getAmount(), StateMachine.CLOSE);
				challenge.setPeriod(StateMachine.getInstance().getPeriod(
						StateMachine.getInstance().getContext().getEngine().getOrders().get(0)));

				double max = actual.getClose().getMax();
				double act = actual.getClose().calcProbability(challenge, tick, actual);
				challenge.setValue(act / max);
				return challenge;
			}
		}
		return null;
	}

	@Override
	public Signal getSignal(Instrument instrument, Period period, IBar askBar, IBar bidBar, State actual)
			throws JFException {
		return null;
	}

	@Override
	public Set<State> getNextStates() {
		Set<State> nextstates = new HashSet<State>();
		nextstates.add(new ScalpHolder7State());
		nextstates.add(new MACDSample452State());
		//nextstates.add(new ThreeLittlePigsState());
		return nextstates;
	}

	@Override
	public double getAmount() {
		// TODO Auto-generated method stub
		return 0;
	}

}
