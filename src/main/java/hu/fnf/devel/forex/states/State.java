package hu.fnf.devel.forex.states;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.fnf.devel.forex.strategies.Strategy;

import com.dukascopy.api.IBar;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.Period;

public abstract class State {
	protected static final Logger LOGGER = LoggerFactory.getLogger(State.class);

	protected String name;
	protected Strategy strategy;
	protected Period period;
	protected Instrument instrument;
	// TODO: strategy cannot have the orders (this wont be destroyed!)
	protected List<IOrder> orders;
	
	public State(String name) {
		this.orders = new ArrayList<IOrder>();
		this.name = name;
		LOGGER.debug("new " + getName());
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
		return true;
//		if ( this == state && this instanceof SignalSeekerState ) {
//			return false;
//		} else {
//			// TODO: IMPORTANT! Only with 1 strategy works this! Plan it!
//			return true;
//		}
	}


}
