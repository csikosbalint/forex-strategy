package hu.fnf.devel.forex.states;

import java.util.HashSet;
import java.util.Set;

import hu.fnf.devel.forex.StateMachine;
import hu.fnf.devel.forex.criteria.MACDClose;
import hu.fnf.devel.forex.criteria.MACDOpen;
import hu.fnf.devel.forex.criteria.MarketCriterion;
import hu.fnf.devel.forex.criteria.WaitAfterClose;
import hu.fnf.devel.forex.utils.CloseCriterion;
import hu.fnf.devel.forex.utils.OpenCriterion;
import hu.fnf.devel.forex.utils.Signal;
import hu.fnf.devel.forex.utils.State;

import org.apache.log4j.Logger;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;

public class MACDSample452State extends State {
	private static final Logger logger = Logger.getLogger(MACDSample452State.class);
	private double amount = 0.1;

	// private int MATrendPeriod = 26;

	public MACDSample452State() {
		super("MACDSample452State");
		String[] markets = { "London", "New York" };
		/*
		 * config
		 */
		this.instruments.add(Instrument.GBPJPY);
		this.instruments.add(Instrument.EURUSD);

		this.periods.add(Period.ONE_HOUR);
		
		open = new OpenCriterion();
		open = new MarketCriterion(open, markets, markets.length-1);
		open = new MACDOpen(open);
		
		close = new CloseCriterion();
		close = new MACDClose(close);
		
	}


	@Override
	public Signal getSignal(Instrument instrument, ITick tick, State actual) throws JFException {
		if (instruments.contains(instrument)) {
			/*
			 * close strategy
			 */
			Signal challenge = new Signal(instrument, amount, StateMachine.CLOSE);
			double max = close.getMax();
			double act = close.calcProbability(challenge, tick, actual);
			challenge.setValue(act/max);
			return challenge;
		}
		return null;
	}

	@Override
	public Signal getSignal(Instrument instrument, Period period, IBar askBar, IBar bidBar, State actual)
			throws JFException {
		if (instruments.contains(instrument) && periods.contains(period)) {
			/*
			 * open strategy
			 */
			Signal challenge = new Signal(instrument, amount, StateMachine.OPEN);
			double max = open.getMax();
			double act = open.calcProbability(challenge, period, askBar, bidBar, actual);
			challenge.setValue(act/max);
			return challenge;
		}
		return null;
	}


	@Override
	public Set<State> getNextStates() {
		Set<State> nextstates = new HashSet<State>();
		nextstates.add(new SignalSeekerState());
		return nextstates;
	}
}
