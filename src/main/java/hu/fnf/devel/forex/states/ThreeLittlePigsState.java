package hu.fnf.devel.forex.states;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;

import hu.fnf.devel.forex.StateMachine;
import hu.fnf.devel.forex.criteria.MarketOpenTimeExclusion;
import hu.fnf.devel.forex.criteria.MoneyManagementExclusion;
import hu.fnf.devel.forex.criteria.ThreePigsClose;
import hu.fnf.devel.forex.criteria.ThreePigsOpen;
import hu.fnf.devel.forex.utils.CloseCriterion;
import hu.fnf.devel.forex.utils.OpenCriterion;
import hu.fnf.devel.forex.utils.Signal;
import hu.fnf.devel.forex.utils.State;

public class ThreeLittlePigsState extends State {
	/*
	 * http://forums.babypips.com/free-forex-trading-systems/55216-3-little-pigs-trading-system.html
	 */
	/*
	 * singleton
	 */
	private final double amount = 0.1;
	private static ThreeLittlePigsState instance;

	public synchronized static ThreeLittlePigsState getInstance() {
		if (instance == null) {
			instance = new ThreeLittlePigsState();
		}
		return instance;
	}


	private ThreeLittlePigsState() {
		super("ThreeLittlePigsState");
		/*
		 * config
		 */
		this.instruments.add(Instrument.GBPUSD);
		this.instruments.add(Instrument.USDJPY);
		this.instruments.add(Instrument.EURGBP);
		this.instruments.add(Instrument.EURUSD);

		this.periods	.add(Period.FOUR_HOURS);

		open = new OpenCriterion();
		open = new MarketOpenTimeExclusion(open);
		open = new ThreePigsOpen(open);

		close = new CloseCriterion();
		close = new MoneyManagementExclusion(close);
		close = new ThreePigsClose(close);
	}

	@Override
	public Signal getSignal(Instrument instrument, ITick tick, State actual) throws JFException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
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
