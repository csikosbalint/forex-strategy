package hu.fnf.devel.forex.states;

import java.util.Random;

import hu.fnf.devel.forex.StateStrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.Period;

public class ScalpHolderState extends State {
	private static final Logger LOGGER = LoggerFactory.getLogger(ScalpHolderState.class);


	public ScalpHolderState(StateStrategy stateStrategy) {
		this.stateMachine = stateStrategy;
	}

	@Override
	public void transaction(Instrument instrument, ITick tick) {
	}

	@Override
	public void transaction(Instrument instrument, Period period, IBar askBar, IBar bidBar) {
		if ( instrument != this.getInstrument() || period != this.getPeriod() ) {
			return;
		}
		LOGGER.info(strategy.getName() + " bar transaction " + instrument.name() + " " + period.interval );
		Random r = new Random(10);
		if ( r.nextInt() == 5 ) {
			stateMachine.setState(new SignalSeekerState(stateMachine));
		}
	//		LOGGER.info("still in this state");
		stateMachine.setState(new SignalSeekerState(stateMachine));
	}
}
