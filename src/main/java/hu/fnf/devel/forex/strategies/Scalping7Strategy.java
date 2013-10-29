package hu.fnf.devel.forex.strategies;

import hu.fnf.devel.forex.Signal;
import hu.fnf.devel.forex.StateMachine;
import hu.fnf.devel.forex.states.ScalpHolderState;
import hu.fnf.devel.forex.states.SignalSeekerState;
import hu.fnf.devel.forex.states.State;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dukascopy.api.IContext;
import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IIndicators;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;

public class Scalping7Strategy extends TickStrategy {

	private double range = 7.0;
	/*
	 * http://forex-strategies-revealed.com/gbp-jpy-scalping
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(Scalping7Strategy.class);

	@Override
	public String getName() {
		return "ScalpingStrategy";
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
	public synchronized State onStart(Instrument instrument, ITick tick, Signal signal) {
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
			StateMachine.getInstance().getOrders().add(order);
			ScalpHolderState scalpState = new ScalpHolderState();
			scalpState.addStrategy(this);
			scalpState.setInstrument(instrument);
			scalpState.setPeriod(Period.ONE_MIN);
			LOGGER.debug("returning " + scalpState.getName() + " onStart from " + getName() + " and orders "
					+ StateMachine.getInstance().getOrders().size() + " onString "
					+ StateMachine.getInstance().getOrders().toString());
			return scalpState;
		} catch (JFException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO: return NullState or QuitState
		LOGGER.debug("returning null onStart from " + getName());
		return null;
	}

	@Override
	public State onStop() {
		try {
			StateMachine.getInstance().getContext().getEngine().closeOrders(StateMachine.getInstance().getOrders());
			StateMachine.getInstance().getOrders().clear();
		} catch (JFException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SignalSeekerState signalState = new SignalSeekerState();
		for (Strategy s : StateMachine.getInstance().getStrategies()) {
			signalState.addStrategy(s);
		}
		return signalState;
	}

}
