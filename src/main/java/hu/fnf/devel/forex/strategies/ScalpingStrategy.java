package hu.fnf.devel.forex.strategies;

import hu.fnf.devel.forex.Main;
import hu.fnf.devel.forex.StateStrategy;
import hu.fnf.devel.forex.states.ScalpHolderState;
import hu.fnf.devel.forex.states.State;

import com.dukascopy.api.IBar;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.Period;

public class ScalpingStrategy extends BarStrategy {

	@Override
	public int signalStrength(Instrument instrument, Period period, IBar askBar, IBar bidBar) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public State onStart(Instrument instrument, Period period, IBar askBar, IBar bidBar) {
		/*
		 * calculation
		 */
		Main.LOGGER.info("...calculation...");
		/*
		 * open order
		 */
		Main.LOGGER.info("...operations...");
		return new ScalpHolderState(this);
	}

	@Override
	public State onStop() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return "ScalpingStrategy";
	}
	
}
