package hu.fnf.devel.forex.states;

import java.util.Random;

import hu.fnf.devel.forex.StateStrategy;
import hu.fnf.devel.forex.strategies.BarStrategy;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.Period;

public class ScalpHolderState extends State {

	public ScalpHolderState() {
		super("ScalpHolderState");
	}

	@Override
	public void transaction(Instrument instrument, ITick tick) {
	}

	@Override
	public void transaction(Instrument instrument, Period period, IBar askBar, IBar bidBar) {
		if (!instrument.equals(this.getInstrument()) || !period.equals(this.getPeriod())){ 
	         return; 
	     }
		LOGGER.info(strategy.getName() + " bar transaction " + instrument.name() + " " + period.getInterval() );
		/*
		 * is close signal?
		 */
		if ( ((BarStrategy) strategy).signalStrength(instrument, period, askBar, bidBar).getSignal() > 0 ) {
			StateStrategy.setState(((BarStrategy) strategy).onStop());
		} else {
			LOGGER.debug("still in state " + getName());
		}
	}

	@Override
	public String getName() {
		return name;
	}
}
