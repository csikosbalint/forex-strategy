package hu.fnf.devel.forex.states;

import hu.fnf.devel.forex.StateStrategy;
import hu.fnf.devel.forex.strategies.BarStrategy;
import hu.fnf.devel.forex.strategies.Strategy;
import hu.fnf.devel.forex.strategies.TickStrategy;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.Period;

public class SignalSeekerState extends State {
	private static final Logger LOGGER = LoggerFactory.getLogger(SignalSeekerState.class);

	Set<TickStrategy> onTickStrategies = new HashSet<TickStrategy>();
	Set<BarStrategy> onBarStrategies = new HashSet<BarStrategy>();

	public SignalSeekerState(StateStrategy stateStrategy) {
		this.stateMachine = stateStrategy;
	}
	
	public void addStrategy(Strategy strategy) {
		if ( strategy instanceof BarStrategy ) {
			onBarStrategies.add((BarStrategy) strategy );
		} else if ( strategy instanceof TickStrategy ) {
			onTickStrategies.add((TickStrategy) strategy );
		}
	}

	@Override
	public void transaction(Instrument instrument, ITick tick) {
		LOGGER.info("SignalSeekerState tick transaction");
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
			LOGGER.info("...selected strategy: " + bestStrategy.getName());
			stateMachine.setState(bestStrategy.onStart(instrument, tick));
		}
	}

	@Override
	public void transaction(Instrument instrument, Period period, IBar askBar, IBar bidBar) {
		LOGGER.info("SignalSeekerState bar transaction " + instrument.name() + " " + period.getInterval());
		int max = 0;
		BarStrategy bestStrategy = null;
		for (BarStrategy s : onBarStrategies) {
			// TODO: thread and singleton pattern
			int signal = s.signalStrength(instrument, period, askBar, bidBar);
			LOGGER.debug(s.getName() + " signal strength: " + signal);
			if (signal > max) {
				max = signal;
				bestStrategy = s;
				LOGGER.debug(s.getName() + " is the new max with " + signal);
			}
		}
		if ( bestStrategy != null ) {
			LOGGER.info("...selected strategy: " + bestStrategy.getName());
			stateMachine.setState(bestStrategy.onStart(instrument, period, askBar, bidBar));
		}
	}
}
