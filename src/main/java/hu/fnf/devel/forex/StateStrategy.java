package hu.fnf.devel.forex;

import hu.fnf.devel.forex.states.SignalSeekerState;
import hu.fnf.devel.forex.states.State;
import hu.fnf.devel.forex.strategies.Strategy;

import java.util.HashSet;
import java.util.Set;
import com.dukascopy.api.*;

public class StateStrategy implements IStrategy {

	hu.fnf.devel.forex.states.State state;
	private IContext context;
	Set<Strategy> onTickStrategies;
	Set<Strategy> onBarStrategies;

	public StateStrategy(Strategy... strategies) {
		
		for (Strategy s : strategies) {
			switch (s.getFrequency()) {
			case Main.ONBAR:
				this.onBarStrategies.add(s);
				break;
			case Main.ONTICK:
				this.onTickStrategies.add(s);
				break;
			default:
				break;
			}
		}
		this.state = getState();
	}

	private State getState() {
		return new SignalSeekerState();
	}

	@Override
	public void onStart(IContext context) throws JFException {
		this.context = context;
		this.context.setSubscribedInstruments(new HashSet<Instrument>());
	}

	@Override
	public void onTick(Instrument instrument, ITick tick) throws JFException {
		this.state.transaction(instrument, tick);
	}

	@Override
	public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
		this.state.transaction(instrument, period, askBar, bidBar);
	}

	@Override
	public void onMessage(IMessage message) throws JFException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAccount(IAccount account) throws JFException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStop() throws JFException {
		// TODO Auto-generated method stub

	}

}
