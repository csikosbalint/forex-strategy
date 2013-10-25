package hu.fnf.devel.forex.states;

import hu.fnf.devel.forex.Main;
import hu.fnf.devel.forex.strategies.Strategy;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.Period;

public class ScalpHolderState extends State {

	public ScalpHolderState(Strategy strategy) {
		this.strategy = strategy;
	}

	@Override
	public void transaction(Instrument instrument, ITick tick) {
		// TODO Auto-generated method stub

	}

	@Override
	public void transaction(Instrument instrument, Period period, IBar askBar, IBar bidBar) {
		Main.LOGGER.info("transaction from " + strategy.getName());
		stateStrategy.setState(stateStrategy);
	}

}
