package hu.fnf.devel.forex.strategies;

import hu.fnf.devel.forex.Signal;
import hu.fnf.devel.forex.states.State;

import com.dukascopy.api.IBar;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.Period;

public abstract class BarStrategy extends Strategy {
	abstract public Signal signalStrength(Instrument instrument, Period period, IBar askBar, IBar bidBar);
	abstract public State onStart(Instrument instrument, Period period, IBar askBar, IBar bidBar, Signal signal);
	abstract public State onStop();
}
