package hu.fnf.devel.forex.states;

import hu.fnf.devel.forex.StateMachine;
import hu.fnf.devel.forex.criteria.MoneyManagement;
import hu.fnf.devel.forex.criteria.Scalp7Close;
import hu.fnf.devel.forex.criteria.Scalp7Open;
import hu.fnf.devel.forex.utils.CloseCriterion;
import hu.fnf.devel.forex.utils.OpenCriterion;
import hu.fnf.devel.forex.utils.Signal;
import hu.fnf.devel.forex.utils.State;

import java.util.HashSet;
import java.util.Set;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;

public class ScalpHolder7State extends State {

	private final double amount = 0.01;

	public ScalpHolder7State() {
		super("ScalpHolder7State");
		/*
		 * config
		 */
		this.instruments.add(Instrument.GBPJPY);
		this.instruments.add(Instrument.USDJPY);
		this.instruments.add(Instrument.EURJPY);
		this.instruments.add(Instrument.EURUSD);
		
		this.periods	.add(Period.ONE_MIN);

		open = new OpenCriterion();
		//open = new MarketCriterion(open);
//		int days = 3;
//		int trades = 2;
		//open = new BadLuckPanic(open, days, trades);
		open = new Scalp7Open(open);

		close = new CloseCriterion();
		close = new MoneyManagement(close);
		close = new Scalp7Close(close);
	}
	
	public Signal getSignal(Instrument instrument, ITick tick, State actual) throws JFException {
		if (instruments.contains(instrument)) {
			/*
			 * open strategy
			 */
			open.reset();
			Signal challenge = new Signal(instrument, getAmount(), StateMachine.OPEN);
			challenge.setPeriod(periods.iterator().next());
			double max = open.getMax();
			double act = open.calcProbability(challenge, tick, actual);
			challenge.setValue(act/max);
			return challenge;
		}
		return null;
	}

	public Signal getSignal(Instrument instrument, Period period, IBar askBar, IBar bidBar, State actual)
			throws JFException {
		return null;
	}

	@Override
	public double getAmount() {
		return this.amount;
	}

	@Override
	public Set<State> getNextStates() {
		Set<State> nextstates = new HashSet<State>();
		nextstates.add(StateMachine.getInstanceOf("SignalSeekerState"));
		nextstates.add(StateMachine.getInstanceOf("PanicState"));
		return nextstates;
	}
}
