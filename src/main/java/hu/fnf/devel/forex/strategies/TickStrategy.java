package hu.fnf.devel.forex.strategies;

import hu.fnf.devel.forex.Signal;

import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;

public abstract class TickStrategy extends Strategy{
	public abstract Signal signalStrength(Instrument instrument, ITick tick);
	abstract public boolean onStart(Instrument instrument, ITick tick, Signal signal);
}
