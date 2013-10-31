package hu.fnf.devel.forex.states;

import hu.fnf.devel.forex.Signal;
import hu.fnf.devel.forex.StateMachine;
import hu.fnf.devel.forex.commands.OpenCommand;

import java.util.HashSet;
import java.util.Set;

import com.dukascopy.api.IContext;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;

public class SignalSeekerState extends State {

	public SignalSeekerState() {
		super("SignalSeekerState");
	}

	@Override
	public Set<State> nextStates() {
		// TODO: make more logic
		Set<State> ret = new HashSet<State>();
		ret.add(new ScalpHolderState());
		return ret;
	}

	@Override
	public boolean onArriving() {
		LOGGER.info("subscribing to instruments:");
		instruments = new HashSet<Instrument>();
		for (State s : nextStates()) {
			for (Instrument i : s.getInstruments()) {
				instruments.add(i);
			}
		}

		for (Instrument i : instruments) {
			LOGGER.info("\t*" + i.name());
		}

		StateMachine.getInstance().getContext().setSubscribedInstruments(instruments);
		return true;
	}

	@Override
	public boolean onLeaving() {
		return true;
	}

	@Override
	public Signal signalStrength(Instrument instrument, ITick tick, State actual) {
		double range = 10;
		Signal ret = new Signal();
		ret.setValue(0);
		if (actual.getName().equalsIgnoreCase("ScalpHolderState")) {
			if (StateMachine.getInstance().getOrders().size() == 0) {
				ret.setTag(StateMachine.CLOSE);
				if (StateMachine.getInstance().getOrders().get(0).getProfitLossInPips() > range * 0.6) {
					LOGGER.info("close!\tpip for " + StateMachine.getInstance().getOrders().get(0).getId() + ": "
							+ StateMachine.getInstance().getOrders().get(0).getProfitLossInPips());
					ret.setValue(1);
					// TODO: self type for openning and closing
					ret.setType(null);
				} else if (StateMachine.getInstance().getOrders().get(0).getProfitLossInPips() < -1 * range * 0.3) {
					LOGGER.info("close!\tpip for " + StateMachine.getInstance().getOrders().get(0).getId() + ": "
							+ StateMachine.getInstance().getOrders().get(0).getProfitLossInPips());
					ret.setValue(1);
					// TODO: self type for openning and closing
					ret.setType(null);
				} else {
					LOGGER.debug("No close:\tpip for " + StateMachine.getInstance().getOrders().get(0).getId() + ": "
							+ StateMachine.getInstance().getOrders().get(0).getProfitLossInPips());
				}
			}
		}
		return ret;
	}

	@Override
	public void prepareCommands(Signal signal) {
		commands.add(new OpenCommand(signal));
	}

}
