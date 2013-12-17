package hu.fnf.devel.forex.states;

import hu.fnf.devel.forex.StateMachine;
import hu.fnf.devel.forex.criteria.BadLuckPanic;
import hu.fnf.devel.forex.criteria.MACDClose;
import hu.fnf.devel.forex.criteria.MACDOpen;
import hu.fnf.devel.forex.criteria.MarketCriterion;
import hu.fnf.devel.forex.criteria.MoneyManagement;
import hu.fnf.devel.forex.criteria.TrendADXCriterion;
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

public class MACDSample452State extends State {
	/*
	 * 
	 */
	private final double amount = 0.01;

	// private int MATrendPeriod = 26;

	public MACDSample452State() {
		super("MACDSample452State");
		/*
		 * config
		 */
		this.instruments.add(Instrument.GBPJPY);
		this.instruments.add(Instrument.EURUSD);

		this.periods.add(Period.ONE_HOUR);

		open = new OpenCriterion();
		open = new MarketCriterion(open);
		int days = 3;
		int trades = 2;
		open = new BadLuckPanic(open, days, trades);
		open = new TrendADXCriterion(open, StateMachine.TREND);
		open = new MACDOpen(open);

		close = new CloseCriterion();
		close = new MoneyManagement(close);
		close = new MACDClose(close);

	}

	public Signal getSignal(Instrument instrument, ITick tick, State actual) throws JFException {
//		if (instruments.contains(instrument) ) {
//			/*
//			 * close strategy
//			 */
//			close.reset();
//			Signal challenge = new Signal(instrument, getAmount(), StateMachine.CLOSE);
//			double max = close.getMax();
//			double act = close.calcProbability(challenge, tick, actual);
//			challenge.setValue(act / max);
//			return challenge;
//		}
		return null;
	}

	public Signal getSignal(Instrument instrument, Period period, IBar askBar, IBar bidBar, State actual)
			throws JFException {
		if (instruments.contains(instrument) && periods.contains(period)) {
			/*
			 * open strategy
			 */
			open.reset();
			Signal challenge = new Signal(instrument, getAmount(), StateMachine.OPEN);
			challenge.setPeriod(period);
			double max = open.getMax();
			double act = open.calcProbability(challenge, period, askBar, bidBar, actual);
			challenge.setValue(act/max);
			return challenge;
		}
		return null;
	}

	@Override
	public double getAmount() {
		return this.amount;
	}

	@Override
	public Set<State> getNextStates() {
		Set<State> nextstates = new HashSet<State>();
		nextstates.add(new SignalSeekerState());
		return nextstates;
	}
}
