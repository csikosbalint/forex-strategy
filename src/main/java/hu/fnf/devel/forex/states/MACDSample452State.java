package hu.fnf.devel.forex.states;

import hu.fnf.devel.forex.StateMachine;
import hu.fnf.devel.forex.criteria.BadLuckPanicExclusion;
import hu.fnf.devel.forex.criteria.MACDCloseCriterion;
import hu.fnf.devel.forex.criteria.MACDOpenCriterion;
import hu.fnf.devel.forex.criteria.MarketOpenTimeExclusion;
import hu.fnf.devel.forex.criteria.MoneyManagementExclusion;
import hu.fnf.devel.forex.criteria.TrendADXExclusion;
import hu.fnf.devel.forex.utils.Signal;
import hu.fnf.devel.forex.utils.State;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;

public class MACDSample452State extends State {
	/*
	 * singleton
	 */
	private final double amount = 0.01;
	private static MACDSample452State instance;

	public synchronized static MACDSample452State getInstance() {
		if (instance == null) {
			instance = new MACDSample452State();
		}
		return instance;
	}

	// private int MATrendPeriod = 26;

	private MACDSample452State() {
		super("MACDSample452State");
		/*
		 * config
		 */
		this.instruments.add(Instrument.GBPJPY);
		this.instruments.add(Instrument.EURUSD);

		this.periods.add(Period.ONE_HOUR);

		open = new MarketOpenTimeExclusion(open);
		int days = 3;
		int trades = 2;
		open = new BadLuckPanicExclusion(open, days, trades);
		open = new TrendADXExclusion(open, StateMachine.TREND);
		open = new MACDOpenCriterion(open);

		close = new MoneyManagementExclusion(close);
		close = new MACDCloseCriterion(close);

	}

	public Signal getSignal(Instrument instrument, ITick tick, State actual) throws JFException {
		if (instruments.contains(instrument) ) {
			/*
			 * close strategy
			 */
			close.reset();
			Signal challenge = new Signal(instrument, getAmount(), StateMachine.CLOSE);
			double max = close.getMax();
			double act = close.calcProbability(challenge, tick, actual);
			challenge.setValue(act / max);
			return challenge;
		}
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
}
