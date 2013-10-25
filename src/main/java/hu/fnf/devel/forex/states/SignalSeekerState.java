package hu.fnf.devel.forex.states;

import hu.fnf.devel.forex.Main;
import hu.fnf.devel.forex.StateStrategy;
import hu.fnf.devel.forex.strategies.BarStrategy;
import hu.fnf.devel.forex.strategies.Strategy;
import hu.fnf.devel.forex.strategies.TickStrategy;

import java.util.HashSet;
import java.util.Set;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.Period;

public class SignalSeekerState extends State {
	Set<TickStrategy> onTickStrategies = new HashSet<TickStrategy>();
	Set<BarStrategy> onBarStrategies = new HashSet<BarStrategy>();

	public SignalSeekerState(Set<Strategy> strategies, StateStrategy stateStrategy) {
		this.stateStrategy = stateStrategy;
		for (Strategy s : strategies) {
			if (s instanceof BarStrategy) {
				this.onBarStrategies.add((BarStrategy) s);
			} else if (s instanceof TickStrategy) {
				this.onTickStrategies.add((TickStrategy) s);
			}
		}
	}

	@Override
	public void transaction(Instrument instrument, ITick tick) {
		Main.LOGGER.info("SignalSeekerState tick transaction");
		int max = 0;
		TickStrategy bestStrategy = null;
		for (TickStrategy s : onTickStrategies) {
			// TODO: thread and singleton pattern
			int signal = s.signalStrength(instrument, tick);
			if (signal > max) {
				max = signal;
				bestStrategy = s;
			}
		}
		if ( bestStrategy != null ) {
			Main.LOGGER.info("...selected strategy: " + bestStrategy.getName());
			stateStrategy.setState(bestStrategy.onStart(instrument, tick));
		}
	}

	@Override
	public void transaction(Instrument instrument, Period period, IBar askBar, IBar bidBar) {
		Main.LOGGER.info("SignalSeekerState bar transaction");
		int max = 0;
		BarStrategy bestStrategy = null;
		for (BarStrategy s : onBarStrategies) {
			// TODO: thread and singleton pattern
			int signal = s.signalStrength(instrument, period, askBar, bidBar);
			if (signal > max) {
				max = signal;
				bestStrategy = s;
			}
		}
		if ( bestStrategy != null ) {
			stateStrategy.setState(bestStrategy.onStart(instrument, period, askBar, bidBar));
			Main.LOGGER.info("new state?");
		}
	}
}
