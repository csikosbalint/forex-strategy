package hu.fnf.devel.forex.strategies;

import java.util.List;

import hu.fnf.devel.forex.Signal;
import hu.fnf.devel.forex.states.State;

import com.dukascopy.api.ITick;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;

public abstract class TickStrategy extends Strategy{
	public abstract Signal signalStrength(Instrument instrument, ITick tick, List<IOrder> orders);
	abstract public State onStart(Instrument instrument, ITick tick, Signal signal, List<IOrder> orders);
}
