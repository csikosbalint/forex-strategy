package hu.fnf.devel.forex.states;

import hu.fnf.devel.forex.StateStrategy;
import hu.fnf.devel.forex.strategies.Strategy;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.Period;

public abstract class State {
	protected StateStrategy stateStrategy;
	protected Strategy strategy;
	public abstract void transaction(Instrument instrument, ITick tick);
	public abstract void transaction(Instrument instrument, Period period, IBar askBar, IBar bidBar);
}
