package hu.fnf.devel.forex.states;

import hu.fnf.devel.forex.Signal;
import hu.fnf.devel.forex.strategies.Strategy;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.Period;

public abstract class State {
	protected static final Logger LOGGER = LoggerFactory.getLogger(State.class);

	protected String name;
	protected Period period;
	protected Instrument instrument;

	// TODO: Iterator pattern
	public abstract Set<State> nextStates();

	public abstract Set<Instrument> getInstruments();

	public abstract Signal signalStrength(Instrument instrument, ITick tick, State actual);

	public abstract boolean onArriving(Instrument instrument, Period period, IBar askBar, IBar bidBar, Signal signal);

	public abstract boolean onArriving(Instrument instrument, ITick tick, Signal signal);

	public abstract boolean onLeaving(Instrument instrument, Period period, IBar askBar, IBar bidBar, Signal signal);

	public abstract boolean onLeaving(Instrument instrument, ITick tick, Signal signal);

	public abstract String getName();

	public abstract void transaction(Instrument instrument, ITick tick);

	public abstract void transaction(Instrument instrument, Period period, IBar askBar, IBar bidBar);

	public abstract Set<Strategy> getStrategies();
	
	public abstract Instrument getInstrument();

	public State(String name) {
		this.name = name;
		LOGGER.debug("new " + getName());
	}

	public Period getPeriod() {
		return period;
	}

	public void setPeriod(Period period) {
		this.period = period;
	}

	public void setInstrument(Instrument instrument) {
		this.instrument = instrument;
	}

}
