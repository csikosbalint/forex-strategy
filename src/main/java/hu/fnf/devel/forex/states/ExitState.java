package hu.fnf.devel.forex.states;

import java.util.HashSet;
import java.util.Set;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;

import hu.fnf.devel.forex.StateMachine;
import hu.fnf.devel.forex.utils.Signal;
import hu.fnf.devel.forex.utils.State;

public class ExitState extends State {

	public ExitState() {
		super("ExitState");
	}

	@Override
	public boolean onArriving() {
		logger.warn("Robot will exit in a minute!");
		(new Thread((new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(60000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.exit(0);
			}
		}))).start();
		return super.onArriving();
	}

	@Override
	public Signal getSignal(Instrument instrument, ITick tick, State actual) throws JFException {

		Signal challenge = new Signal(instrument, getAmount(), StateMachine.CLOSE);
		double max = 1.0;
		double act = 0;
		if ( actual instanceof PanicState ) {
			if ( ((PanicState)actual).canIExit() ) {
				act = max;
			}
		}
		challenge.setValue(act/max);
		return challenge;
	}

	@Override
	public Signal getSignal(Instrument instrument, Period period, IBar askBar, IBar bidBar, State actual)
			throws JFException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<State> getNextStates() {
		Set<State> nextstates = new HashSet<State>();
		return nextstates;
	}

	@Override
	public double getAmount() {
		// TODO Auto-generated method stub
		return 0;
	}

}
