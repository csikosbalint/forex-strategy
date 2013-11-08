package hu.fnf.devel.forex.states;

import org.apache.log4j.Logger;

import hu.fnf.devel.forex.Main;
import hu.fnf.devel.forex.StateMachine;
import hu.fnf.devel.forex.utils.Signal;

import com.dukascopy.api.IContext;
import com.dukascopy.api.IIndicators;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.dukascopy.api.IIndicators.AppliedPrice;

public class MACDSample452State extends State {
	private static final Logger logger = Logger.getLogger(MACDSample452State.class);
	private double amount = 0.1;
	private int buyTP = 50;
	private int sellTP = 75;
	private int buySL = 80;
	private int sellSL = 50;

	private double MACDOpenLevel = 3.0;
	private double MACDCloseLevel = 2.0;
	private int MATrendPeriod = 26;

	public MACDSample452State(String name) {
		super("MACDSample452State");
	}

	@Override
	public Signal signalStrength(Instrument instrument, ITick tick, State actual) throws JFException {
		if (!instruments.contains(instrument)) {
			Signal ret = new Signal();
			ret.setValue(0);
			return ret;
		}

		IContext context = StateMachine.getInstance().getContext();
		Signal ret = new Signal();
		ret.setValue(0);
		ret.setInstrument(instrument);
		ret.setAmount(amount);

		// no trade during weekend time
		if (context.getDataService().isOfflineTime(context.getHistory().getLastTick(instrument).getTime())) {
			Main.massDebug(logger, "Over-weekend trade is not supported!");
			if (context.getEngine().getOrders().size() != 0) {
				logger.warn("It is not advides to have orders during weekend period.");
				for (IOrder o : context.getEngine().getOrders()) {
					logger.debug("#" + o.getId() + " - " + o.getInstrument() + " - $" + o.getProfitLossInUSD());
				}
			}
			return ret;
		}
		// no trade out of london, new york, tokyo session time
		String[] markets = { "London", "New York", "Tokyo" };
		StringBuilder closedMarkets = new StringBuilder();
		for (int i = 0; i < markets.length; i++) {
			if (!Main.isMarketOpen(markets[i])) {
				closedMarkets.append(markets[i] + ",");
			}
		}

		if (closedMarkets.toString().split(",").length > 2) { // from T opens
																// till NY
																// closes
			Main.massDebug(logger, closedMarkets.toString() + " market is closed.");
			return ret;
		}

		// TODO: in this point order anomailes have to be handeled

		if (StateMachine.getInstance().getOrders().size() == 0) {
			/*
			 * volativity check
			 */

			/*
			 * buy strategy
			 */
			double MacdCurrent[];
			double MacdPrevious[];
			double SignalCurrent;
			double SignalPrevious;
			double MaCurrent;
			double MaPrevious;
			IIndicators indicators = StateMachine.getInstance().getContext().getIndicators();
			try {
				// iMACD(NULL,0,12,26,9,PRICE_CLOSE,MODE_MAIN,0);
				MacdCurrent = indicators.macd(instrument, Period.ONE_HOUR, OfferSide.ASK,
						IIndicators.AppliedPrice.CLOSE, 12, 26, 9, 0);
				MacdPrevious = indicators.macd(instrument, Period.ONE_HOUR, OfferSide.ASK,
						IIndicators.AppliedPrice.CLOSE, 12, 26, 9, 1);
				SignalCurrent = MacdCurrent[1];
				SignalPrevious = MacdPrevious[1];
				// iMA(NULL,0,MATrendPeriod,0,MODE_EMA,PRICE_CLOSE,0);
				MaCurrent = indicators.ma(instrument, Period.ONE_HOUR, OfferSide.ASK, IIndicators.AppliedPrice.CLOSE,
						MATrendPeriod, IIndicators.MaType.EMA, 0);
				MaPrevious = indicators.ma(instrument, Period.ONE_HOUR, OfferSide.ASK, IIndicators.AppliedPrice.CLOSE,
						MATrendPeriod, IIndicators.MaType.EMA, 1);
			} catch (JFException e) {
				throw new JFException("Error during buy calculation.", e);
			}
			if (MacdCurrent[0] < 0 && MacdCurrent[0] > SignalCurrent && MacdPrevious[0] < SignalPrevious) {
				if (Math.abs(MacdCurrent[0]) > (MACDOpenLevel * sdPoint ) && MaCurrent > MaPrevious) {
					// ??? sdPoint = Point*10.0000;
				}
			}
			/*
			 * sell strategy
			 */
			try {
				// iMACD(NULL,0,12,26,9,PRICE_CLOSE,MODE_MAIN,0);
				MacdCurrent = indicators.macd(instrument, Period.ONE_HOUR, OfferSide.BID,
						IIndicators.AppliedPrice.CLOSE, 12, 26, 9, 0);
				MacdPrevious = indicators.macd(instrument, Period.ONE_HOUR, OfferSide.BID,
						IIndicators.AppliedPrice.CLOSE, 12, 26, 9, 1);
				SignalCurrent = MacdCurrent[1];
				SignalPrevious = MacdPrevious[1];
				// iMA(NULL,0,MATrendPeriod,0,MODE_EMA,PRICE_CLOSE,0);
				MaCurrent = indicators.ma(instrument, Period.ONE_HOUR, OfferSide.BID, IIndicators.AppliedPrice.CLOSE,
						MATrendPeriod, IIndicators.MaType.EMA, 0);
				MaPrevious = indicators.ma(instrument, Period.ONE_HOUR, OfferSide.BID, IIndicators.AppliedPrice.CLOSE,
						MATrendPeriod, IIndicators.MaType.EMA, 1);
			} catch (JFException e) {
				throw new JFException("Error during buy calculation.", e);
			}
		} else {
			/*
			 * close strategy
			 */

		}

		return ret;
	}
}
