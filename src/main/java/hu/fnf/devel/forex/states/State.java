package hu.fnf.devel.forex.states;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.fnf.devel.forex.StateStrategy;
import hu.fnf.devel.forex.strategies.Strategy;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.Period;

public abstract class State {
	protected static final Logger LOGGER = LoggerFactory.getLogger(State.class);

	protected String name;
	protected Strategy strategy;
	protected Period period;
	protected Instrument instrument;
	
	public State(String name) {
		this.name = name;
		LOGGER.info("new " + getName());
	}

	public abstract String getName();
	
	public abstract void transaction(Instrument instrument, ITick tick);

	public abstract void transaction(Instrument instrument, Period period, IBar askBar, IBar bidBar);

	public void addStrategy(Strategy strategy) {
		this.strategy = strategy;
	}

	public Strategy getStrategy() {
		return strategy;
	}

	public void setStrategy(Strategy strategy) {
		this.strategy = strategy;
	}

	public Period getPeriod() {
		return period;
	}

	public void setPeriod(Period period) {
		this.period = period;
	}

	public Instrument getInstrument() {
		return instrument;
	}

	public void setInstrument(Instrument instrument) {
		this.instrument = instrument;
	}

	public boolean isAllowed(State state) {
		if ( this == state && this instanceof SignalSeekerState ) {
			return false;
		} else {
			// TODO: IMPORTANT! Only with 1 strategy works this! Plan it!
			return true;
		}
	}


}
