package hu.fnf.devel.forex.states;

import hu.fnf.devel.forex.StateMachine;
import hu.fnf.devel.forex.commands.CloseCommand;
import hu.fnf.devel.forex.utils.Signal;
import hu.fnf.devel.forex.utils.State;

import java.util.HashSet;

import org.apache.log4j.Logger;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;

public class SignalSeekerState extends State {
	private static final Logger logger = Logger.getLogger(SignalSeekerState.class);
	/*
	 * singleton
	 */
	private static SignalSeekerState instance;

	public synchronized static SignalSeekerState getInstance() {
		if (instance == null) {
			instance = new SignalSeekerState();
		}
		return instance;
	}

	private SignalSeekerState() {
		super("SignalSeekerState");
		/*
		 * config
		 */
		// SignalSeeker has no criteria
		// SignalSeeker has no intruments

		this.periods.add(Period.TICK);
		this.instruments = new HashSet<Instrument>();
	}

	@Override
	public Signal getSignal(Instrument instrument, ITick tick, State actual) throws JFException {
		if (actual.getInstruments().contains(instrument)) {
			if (StateMachine.getInstance().getContext().getEngine().getOrders().size() > 0
					&& StateMachine.getInstance().getContext().getEngine().getOrders().get(0).getInstrument()
							.equals(instrument)) {
				Signal challenge = new Signal(instrument, StateMachine.getInstance().getContext().getEngine()
						.getOrders().get(0).getAmount(), StateMachine.CLOSE);
				challenge.setCommand(new CloseCommand(actual));
				double max = actual.getClose().getMax();
				double act = actual.getClose().calcProbability(challenge, tick, actual);
				challenge.setValue(act / max);
				return challenge;
			}
		}
		return null;
	}

	@Override
	public Signal getSignal(Instrument instrument, Period period, IBar askBar, IBar bidBar, State actual)
			throws JFException {
		return null;
	}

	@Override
	public double getAmount() {
		// TODO Auto-generated method stub
		return 0;
	}

}
