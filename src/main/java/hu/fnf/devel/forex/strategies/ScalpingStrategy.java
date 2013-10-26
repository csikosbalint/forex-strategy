package hu.fnf.devel.forex.strategies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.fnf.devel.forex.StateStrategy;
import hu.fnf.devel.forex.states.ScalpHolderState;
import hu.fnf.devel.forex.states.State;

import com.dukascopy.api.IBar;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.Period;

public class ScalpingStrategy extends BarStrategy {
	private static final Logger LOGGER = LoggerFactory.getLogger(ScalpingStrategy.class);


	@Override
	public int signalStrength(Instrument instrument, Period period, IBar askBar, IBar bidBar) {
		return 1;
	}

	@Override
	public State onStart(Instrument instrument, Period period, IBar askBar, IBar bidBar) {
		/*
		 * calculation
		 */
		LOGGER.info("...calculation...");
		/*
		 * open order
		 */
		LOGGER.info("...operations...");
		ScalpHolderState scalpState = new ScalpHolderState(StateStrategy.getInstance());
		scalpState.addStrategy(this);
		scalpState.setInstrument(instrument);
		scalpState.setPeriod(period);
		return scalpState;
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
