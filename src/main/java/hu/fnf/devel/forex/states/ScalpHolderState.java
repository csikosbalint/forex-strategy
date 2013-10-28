package hu.fnf.devel.forex.states;

import hu.fnf.devel.forex.StateStrategy;
import hu.fnf.devel.forex.strategies.BarStrategy;
import hu.fnf.devel.forex.strategies.TickStrategy;

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
		if (!instrument.equals(this.getInstrument()) ){ 
	         return; 
	     }
		LOGGER.info(strategy.getName() + " tick transaction " + instrument.name() + " " + period.getInterval() );
		/*
		 * is close signal?
		 */
		if ( ((TickStrategy) strategy).signalStrength(instrument, tick).getStrength() > 0 ) {
			StateStrategy.setState(((TickStrategy) strategy).onStop());
		} else {
			LOGGER.debug("still in state " + getName());
		}
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
		if ( ((BarStrategy) strategy).signalStrength(instrument, period, askBar, bidBar).getStrength() > 0 ) {
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
