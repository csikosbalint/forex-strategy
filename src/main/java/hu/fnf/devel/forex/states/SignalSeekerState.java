package hu.fnf.devel.forex.states;

import hu.fnf.devel.forex.Signal;
import hu.fnf.devel.forex.StateMachine;
import hu.fnf.devel.forex.strategies.Strategy;

import java.util.HashSet;
import java.util.Set;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.Period;

public class SignalSeekerState extends State {

	public SignalSeekerState() {
		super("SignalSeekerState");

		Set<Strategy> strategies = new HashSet<Strategy>();
		Strategy scalp7 = new Scalping7Strategy();
		strategies.add(scalp7);

		Set<Instrument> instruments = new HashSet<Instrument>();
		LOGGER.info("subscribing to instruments:");

		for (Strategy s : getStrategies()) {
			for (Instrument i : s.getInstruments()) {
				instruments.add(i); // no duplicate
				LOGGER.info("\t*" + i.name() + "(" + s.getName() + ")");
			}
		}

		StateMachine.getInstance().getContext().setSubscribedInstruments(instruments);
	}

	@Override
	public State transaction(Instrument instrument, ITick tick) {
		// max selection
		State bestState = null;
		Signal bestSignal = new Signal();
		for (State nextState : nextStates()) { // instantiate all states!!
			LOGGER.debug("checking " + nextState.getName());
			for (Instrument i : nextState.getInstruments()) {
				if (instrument.equals(i)) {
					Signal nextSignal = nextState.signalStrength(instrument, tick, this);
					LOGGER.debug(nextState.getName() + " signal strength: " + nextSignal.getValue());
					if (nextSignal.getValue() > bestSignal.getValue()) {
						bestState = nextState;
						bestSignal = nextSignal;
						LOGGER.debug(nextState.getName() + " is the new max with " + nextSignal.getValue());
					}
				}
			}
		}
		bestState.setInstrument(instrument);
		
		return bestState;
	}

	@Override
	public void transaction(Instrument instrument, Period period, IBar askBar, IBar bidBar) {
		// BarStrategy bestStrategy = null;
		// Signal bestSignal = null;
		// for (BarStrategy s : onBarStrategies) {
		// for (Period p : s.getPeriods()) {
		// if (period == p) {
		// for (Instrument i : s.getInstruments()) {
		// if (instrument == i) {
		// // TODO: thread and singleton pattern
		// Signal signal = s.signalStrength(instrument, period, askBar, bidBar);
		// LOGGER.debug(s.getName() + " signal strength: " + signal.getValue());
		// int max = 0;
		// if (signal.getValue() > max) {
		// max = signal.getValue();
		// bestStrategy = s;
		// bestSignal = signal;
		// LOGGER.debug(s.getName() + " is the new max with " +
		// signal.getValue());
		// }
		// }
		// }
		// }
		// }
		// }
		// if (bestStrategy != null) {
		// LOGGER.info("selected strategy is " + bestStrategy.getName() +
		// " with " + bestSignal.getValue()
		// + " strength(" + bestSignal.getType().name() + ")");
		// StateMachine.setState(bestStrategy.onStart(instrument, period,
		// askBar, bidBar, bestSignal));
		// }
	}

	@Override
	public String getName() {
		return "SignalSeekerState";
	}

	@Override
	public Set<State> nextStates() {
		// TODO: make more logic
		Set<State> ret = new HashSet<State>();
		ret.add(new ScalpHolderState());
		return ret;
	}

	@Override
	public Set<Instrument> getInstruments() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onArriving(Instrument instrument, Period period, IBar askBar, IBar bidBar, Signal signal) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onArriving(Instrument instrument, ITick tick, Signal signal) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onLeaving(Instrument instrument, Period period, IBar askBar, IBar bidBar, Signal signal) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onLeaving(Instrument instrument, ITick tick, Signal signal) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Signal signalStrength(Instrument instrument, ITick tick, State actual) {
		// TODO Auto-generated method stub
		return null;
	}

}
