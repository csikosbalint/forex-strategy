package hu.fnf.devel.forex.strategies;

import hu.fnf.devel.forex.Signal;
import hu.fnf.devel.forex.StateStrategy;
import hu.fnf.devel.forex.states.ScalpHolderState;
import hu.fnf.devel.forex.states.SignalSeekerState;
import hu.fnf.devel.forex.states.State;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IIndicators;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;

public class Scalping7Strategy extends BarStrategy {
	List<IOrder> myOrders = new ArrayList<IOrder>();
	/*
	 * http://forex-strategies-revealed.com/gbp-jpy-scalping
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(Scalping7Strategy.class);

	@Override
	public Signal signalStrength(Instrument instrument, Period period, IBar askBar, IBar bidBar) {
		IContext context = StateStrategy.getInstance().getContext();
		Signal ret = new Signal();
		ret.setSignal(0);
		
		if (myOrders.size() == 0 && totalOrders(null) == 0) {
			/*
			 * buy strategy
			 */
			try {
				double red[] = context.getIndicators().bbands(instrument, period, OfferSide.ASK,
						IIndicators.AppliedPrice.MEDIAN_PRICE, 50, 1, 1, IIndicators.MaType.SMA, 0);
				double orange[] = context.getIndicators().bbands(instrument, period, OfferSide.ASK,
						IIndicators.AppliedPrice.MEDIAN_PRICE, 50, 1.5, 1.5, IIndicators.MaType.SMA, 0);
				double yellow[] = context.getIndicators().bbands(instrument, period, OfferSide.ASK,
						IIndicators.AppliedPrice.MEDIAN_PRICE, 50, 2, 2, IIndicators.MaType.SMA, 0);
				if (askBar.getHigh() < red[red.length - 1] && askBar.getHigh() < orange[orange.length - 1] / 2) {
					LOGGER.info("it is a good sign to buy");
					ret.setType(OrderCommand.BUY);
					if (askBar.getHigh() < orange[orange.length - 1]) {
						ret.setSignal(2);
					} else {
						ret.setSignal(1);
					}

				}
			} catch (JFException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			/*
			 * sell strategy
			 */
			try {
				double red[] = context.getIndicators().bbands(instrument, period, OfferSide.BID,
						IIndicators.AppliedPrice.MEDIAN_PRICE, 50, 1, 1, IIndicators.MaType.SMA, 0);
				double orange[] = context.getIndicators().bbands(instrument, period, OfferSide.BID,
						IIndicators.AppliedPrice.MEDIAN_PRICE, 50, 1.5, 1.5, IIndicators.MaType.SMA, 0);
				double yellow[] = context.getIndicators().bbands(instrument, period, OfferSide.BID,
						IIndicators.AppliedPrice.MEDIAN_PRICE, 50, 2, 2, IIndicators.MaType.SMA, 0);
				if (bidBar.getHigh() > red[red.length - 1] && bidBar.getHigh() > orange[orange.length - 1] / 2) {
					LOGGER.info("it is a good sign to sell");
					ret.setType(OrderCommand.SELL);
					if (bidBar.getHigh() > orange[orange.length - 1]) {
						ret.setSignal(2);
					} else {
						ret.setSignal(1);
					}
				}
			} catch (JFException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			LOGGER.debug("retval is " + ret.getType().name() + "(" + ret.getSignal() + ")");
			return ret;
		} else {
			/*
			 * ERROR check
			 */
			/*
			 * close strategy
			 */
			if ( myOrders.get(0).getProfitLossInPips() > 5 ) {
				ret.setSignal(1);
				// TODO: self type for openning and closing
				ret.setType(null);
			}
		}
		return ret;
	}

	@Override
	public State onStart(Instrument instrument, Period period, IBar askBar, IBar bidBar, Signal signal) {
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
			IOrder order = StateStrategy.getInstance().getContext().getEngine()
					.submitOrder(getName() + signal.getSignal(), instrument, signal.getType(), 0.1);
			myOrders.add(order);
			ScalpHolderState scalpState = new ScalpHolderState();
			scalpState.addStrategy(this);
			scalpState.setInstrument(instrument);
			scalpState.setPeriod(period);
			return scalpState;
		} catch (JFException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO: return NullState or QuitState
		return null;
	}

	@Override
	public State onStop() {
		try {
			StateStrategy.getInstance().getContext().getEngine().closeOrders(myOrders);
		} catch (JFException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new SignalSeekerState();
	}

	@Override
	public String getName() {
		return "ScalpingStrategy";
	}

}
