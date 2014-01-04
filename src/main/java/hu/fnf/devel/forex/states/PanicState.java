package hu.fnf.devel.forex.states;

import java.util.HashSet;
import java.util.Set;

import com.dukascopy.api.IBar;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;

import hu.fnf.devel.forex.Main;
import hu.fnf.devel.forex.StateMachine;
import hu.fnf.devel.forex.utils.RobotException;
import hu.fnf.devel.forex.utils.Signal;
import hu.fnf.devel.forex.utils.State;

public class PanicState extends State {

	public PanicState() {
		super("PanicState");
	}
	
	@Override
	public boolean onArriving() {
		try {
			Main.sendMail("PANIC!", "I am in Panic state!");
		} catch (RobotException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return super.onArriving();
	}
	
	@Override
	public boolean onLeaving() {
		try {
			Main.sendMail("Information during Panic", gatherInformation());
		} catch (RobotException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return super.onLeaving();
	}

	private String gatherInformation() {
		return "no information";
	}

	@Override
	public Signal getSignal(Instrument instrument, ITick tick, State actual) throws JFException {
		if (actual.getInstruments().contains(instrument)) {
			if (StateMachine.getInstance().getContext().getEngine().getOrders().size() > 0
					&& StateMachine.getInstance().getContext().getEngine().getOrders().get(0).getInstrument()
							.equals(instrument)) {
				Signal challenge = new Signal(instrument, StateMachine.getInstance().getContext().getEngine()
						.getOrders().get(0).getAmount(), StateMachine.CLOSE);
				challenge.setPeriod(StateMachine.getInstance().getPeriod(
						StateMachine.getInstance().getContext().getEngine().getOrders().get(0)));

				double max = actual.getClose().getMax();
				double act = 0;
				if ( actual.isPanic() ) {
					act = max;
				}
				challenge.setValue(act / max);
				return challenge;
			}
		}
		return null;
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
		nextstates.add(StateMachine.getStateInstance("ExitState"));
		return nextstates;
	}

	@Override
	public double getAmount() {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean canIExit() throws JFException {
		if ( StateMachine.getInstance().getContext().getEngine().getOrders().size() == 0 ) {
			logger.info("Manual close has happened ... I guess!");
			return true;
		}
		double sum = 0;
		double com = 0;
		for ( IOrder order: StateMachine.getInstance().getContext().getEngine().getOrders() ) {
			sum += order.getProfitLossInUSD();
			com += order.getCommissionInUSD();
		}
		if ( sum > com ) {
			return true;
		}
		return false;
	}

}
