package hu.fnf.devel.forex.states;

import hu.fnf.devel.forex.Signal;
import hu.fnf.devel.forex.StateMachine;
import hu.fnf.devel.forex.strategies.TickStrategy;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IIndicators;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;

public class ScalpHolderState extends State {

	public ScalpHolderState() {
		super("ScalpHolderState");
		instruments = new HashSet<Instrument>();
		instruments.add(Instrument.GBPJPY);
		instruments.add(Instrument.USDJPY);
	}

	@Override
	public State transaction(Instrument instrument, ITick tick) {
		State bestState = null;
		Signal bestSignal = new Signal();
		for (State nextState : nextStates()) { // instantiate all states!!
			LOGGER.debug("checking " + nextState.getName());
			for (Instrument i : nextState.getInstruments()) {
				if (instrument.equals(i)) {
					Signal nextSignal = nextState.signalStrength(instrument, tick, this);
					LOGGER.debug(nextState.getName() + " signal strength: " + nextSignal.getValue());
					if (nextSignal.getValue() > bestSignal.getValue()) {
						bestState = nextState;
						bestSignal = nextSignal;
						LOGGER.debug(nextState.getName() + " is the new max with " + nextSignal.getValue());
					}
				}
			}
		}
		if (bestState != null) {
			LOGGER.info("selected strategy is " + bestState.getName() + " with " + bestSignal.getValue() + " strength("
					+ bestSignal.getType().name() + ")");
			if (bestState.onArriving(instrument, tick, bestSignal) && this.onLeaving(instrument, tick, bestSignal)) {
				StateMachine.changeState(bestState);
			}
		}
	}

	@Override
	public void transaction(Instrument instrument, Period period, IBar askBar, IBar bidBar) {
		// if (!instrument.equals(this.getInstrument()) ||
		// !period.equals(this.getPeriod())) {
		// return;
		// }
		// LOGGER.info(strategy.getName() + " bar transaction " +
		// instrument.name() + " " + period.getInterval());
		// /*
		// * is close signal?
		// */
		// if (((BarStrategy) strategy).signalStrength(instrument, period,
		// askBar, bidBar).getValue() > 0) {
		// StateMachine.setState(((BarStrategy) strategy).onStop());
		// } else {
		// LOGGER.debug("still in state " + getName());
		// }
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Set<State> nextStates() {
		Set<State> ret = new HashSet<State>();
		State signalSeeker = new SignalSeekerState();
		ret.add(signalSeeker);
		
		return ret;
	}

	@Override
	public Set<Instrument> getInstruments() {
		Set<Instrument> ret = new HashSet<Instrument>();
		ret.add(instrument);
		return ret;
	}

	@Override
	public Signal signalStrength(Instrument instrument, ITick tick, State actual) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onArriving(Instrument instrument, Period period, IBar askBar, IBar bidBar, Signal signal) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onArriving(Instrument instrument, ITick tick, Signal signal) {
		// TODO: this is ugly
		new Scalping7Strategy().onStart(instrument, tick, signal);
		return false;
	}

	@Override
	public boolean onLeaving(Instrument instrument, Period period, IBar askBar, IBar bidBar, Signal signal) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onLeaving(Instrument instrument, ITick tick, Signal signal) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Instrument getInstrument() {
		// TODO Auto-generated method stub
		return null;
	}
}

class Scalping7Strategy extends TickStrategy {
	Set<Instrument> instruments = new HashSet<Instrument>();
	private double range = 7.0;
	/*
	 * http://forex-strategies-revealed.com/gbp-jpy-scalping
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(Scalping7Strategy.class);

	public Scalping7Strategy() {
		this.name = "Scalping7Strategy";
	}

	@Override
	public Signal signalStrength(Instrument instrument, ITick tick) {
		IContext context = StateMachine.getInstance().getContext();
		Signal ret = new Signal();
		ret.setValue(0);
		// TODO: in this point anomailes have to be handeled

		LOGGER.debug("signalStrength calculation myOrders.size: " + StateMachine.getInstance().getOrders().size()
				+ " totalOrders: " + totalOrders(IOrder.State.FILLED));

		if (totalOrders(null) == 0) {
			/*
			 * buy strategy
			 */
			try {
				double red[] = context.getIndicators().bbands(instrument, Period.ONE_MIN, OfferSide.ASK,
						IIndicators.AppliedPrice.MEDIAN_PRICE, 50, 1, 1, IIndicators.MaType.SMA, 0);
				double orange[] = context.getIndicators().bbands(instrument, Period.ONE_MIN, OfferSide.ASK,
						IIndicators.AppliedPrice.MEDIAN_PRICE, 50, 1.5, 1.5, IIndicators.MaType.SMA, 0);
				// double yellow[] = context.getIndicators().bbands(instrument,
				// period, OfferSide.ASK,
				// IIndicators.AppliedPrice.MEDIAN_PRICE, 50, 2, 2,
				// IIndicators.MaType.SMA, 0);
				if (tick.getAsk() < red[red.length - 1] && tick.getAsk() < orange[orange.length - 1] / 2) {
					LOGGER.info("it is a good sign to buy " + instrument.name());
					LOGGER.info("\tred: " + red[red.length - 1] + "\t>\task: " + tick.getAsk());
					ret.setType(OrderCommand.BUY);
					if (tick.getAsk() < orange[orange.length - 1]) {
						ret.setValue(2);
					} else {
						ret.setValue(1);
					}
				} else {
					LOGGER.debug("No buy:\tred: " + red[red.length - 1] + " > ask: " + tick.getAsk());
				}
			} catch (JFException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			/*
			 * sell strategy
			 */
			try {
				double red[] = context.getIndicators().bbands(instrument, Period.ONE_MIN, OfferSide.BID,
						IIndicators.AppliedPrice.MEDIAN_PRICE, 50, 1, 1, IIndicators.MaType.SMA, 0);
				double orange[] = context.getIndicators().bbands(instrument, Period.ONE_MIN, OfferSide.BID,
						IIndicators.AppliedPrice.MEDIAN_PRICE, 50, 1.5, 1.5, IIndicators.MaType.SMA, 0);
				// double yellow[] = context.getIndicators().bbands(instrument,
				// period, OfferSide.BID,
				// IIndicators.AppliedPrice.MEDIAN_PRICE, 50, 2, 2,
				// IIndicators.MaType.SMA, 0);
				if (tick.getBid() > red[2] && tick.getBid() > orange[2] / 2) {
					LOGGER.info("it is a good sign to sell " + instrument.name());
					LOGGER.info("\tred: " + red[2] + " < ask: " + tick.getBid());
					ret.setType(OrderCommand.SELL);
					if (tick.getBid() > orange[orange.length - 1]) {
						ret.setValue(2);
					} else {
						ret.setValue(1);
					}
				} else {
					LOGGER.debug("No sell:\tred: " + red[2] + " < ask: " + tick.getBid());
				}
			} catch (JFException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			LOGGER.debug("retval is " + ret.getType() + "(" + ret.getValue() + ")");
			return ret;
		} else {
			/*
			 * ERROR check
			 */
			/*
			 * close strategy
			 */
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
		return ret;
	}

	@Override
	public synchronized boolean onStart(Instrument instrument, ITick tick, Signal signal) {
		/*
		 * calculation
		 */
		LOGGER.info("...calculation...");
		/*
		 * open order
		 */
		LOGGER.info("...operations...");
		// TODO: this has no SL or TP! This is dangerous!
		// TODO: define method for this
		try {
			IOrder order = StateMachine.getInstance().getContext().getEngine()
					.submitOrder(getName() + signal.getValue(), instrument, signal.getType(), 0.1);
			StateMachine.getInstance().addOrders(order);

			LOGGER.debug("returning " + getName() + " onStart from " + getName() + " and orders "
					+ StateMachine.getInstance().getOrders().size() + " onString "
					+ StateMachine.getInstance().getOrders().toString());
			return true;
		} catch (JFException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO: return NullState or QuitState
		LOGGER.debug("returning null onStart from " + getName());
		return false;
	}

	@Override
	public boolean onStop() {
		try {
			StateMachine.getInstance().getContext().getEngine().closeOrders(StateMachine.getInstance().getOrders());
			StateMachine.getInstance().getOrders().clear();
			return true;
		} catch (JFException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

}
