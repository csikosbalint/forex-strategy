package hu.fnf.devel.forex.states;

import java.util.HashSet;

import org.apache.log4j.Logger;

import hu.fnf.devel.forex.Main;
import hu.fnf.devel.forex.StateMachine;
import hu.fnf.devel.forex.utils.Signal;

import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IIndicators;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.dukascopy.api.IEngine.OrderCommand;

public class MACDSample452State extends State {
	private static final Logger logger = Logger.getLogger(MACDSample452State.class);
	private double amount = 0.1;
	// private int buyTP = 50;
	// private int sellTP = 75;
	// private int buySL = 80;
	// private int sellSL = 50;

	private double MACDOpenLevel = 3.0;
	private double MACDCloseLevel = 2.0;
	private int MATrendPeriod = 26;

	public MACDSample452State() {
		super("MACDSample452State");
		// config
		instruments = new HashSet<Instrument>();
		// instruments.add(Instrument.GBPJPY);
		instruments.add(Instrument.EURUSD);

		periods = new HashSet<Period>();
		periods.add(Period.ONE_HOUR);
	}

	@Override
	public Signal signalStrength(Instrument instrument, ITick tick, State actual) throws JFException {
		if (instruments.contains(instrument) && StateMachine.getInstance().getOrders().size() != 0) {
			/*
			 * close strategy
			 */
			double MacdCurrent[] = null;
			double MacdPrevious[] = null;
			double SignalCurrent = 0;
			double SignalPrevious = 0;
			Signal ret = new Signal();

			IIndicators indicators = StateMachine.getInstance().getContext().getIndicators();
			// logger.debug("closing?");
			if (StateMachine.getInstance().getOrders().get(0).getProfitLossInPips() < -15
					|| StateMachine.getInstance().getOrders().get(0).getProfitLossInPips() > 30) {
				ret.setTag(StateMachine.CLOSE);
				ret.setValue(2);
			}
			if (StateMachine.getInstance().getOrders().get(0).isLong()) {
				/*
				 * long Long exit – by execution of the take profit limit, by
				 * execution of the trailing stop or when MACD crosses its
				 * Signal Line (MACD is above zero, goes downwards and is
				 * crossed by the Signal Line going upwards).
				 */
				try {
					// iMACD(NULL,0,12,26,9,PRICE_CLOSE,MODE_MAIN,0);
					MacdCurrent = indicators.macd(instrument, Period.ONE_HOUR, OfferSide.BID,
							IIndicators.AppliedPrice.CLOSE, 12, 26, 9, 1);
					MacdPrevious = indicators.macd(instrument, Period.ONE_HOUR, OfferSide.BID,
							IIndicators.AppliedPrice.CLOSE, 12, 26, 9, 2);
					SignalCurrent = MacdCurrent[1];
					SignalPrevious = MacdPrevious[1];
				} catch (JFException e) {
					throw new JFException("Error during buy calculation.", e);
				}
				if (MacdCurrent[2] > 0 && MacdCurrent[2] < MacdPrevious[2] && crossed(MacdCurrent[2], SignalCurrent)
						&& SignalPrevious < SignalCurrent) {
					logger.debug("yes!");
					if (MacdCurrent[0] > (MACDCloseLevel * (instrument.getPipValue() * amount * 100000))) {
						ret.setTag(StateMachine.CLOSE);
						ret.setValue(2);
					}
				}
			} else {
				/*
				 * short
				 */
				try {
					// iMACD(NULL,0,12,26,9,PRICE_CLOSE,MODE_MAIN,0);
					MacdCurrent = indicators.macd(instrument, Period.ONE_HOUR, OfferSide.ASK,
							IIndicators.AppliedPrice.CLOSE, 12, 26, 9, 1);
					MacdPrevious = indicators.macd(instrument, Period.ONE_HOUR, OfferSide.ASK,
							IIndicators.AppliedPrice.CLOSE, 12, 26, 9, 2);
					SignalCurrent = MacdCurrent[1];
					SignalPrevious = MacdPrevious[1];
				} catch (JFException e) {
					throw new JFException("Error during buy calculation.", e);
				}
				/*
				 * when MACD crosses its Signal Line (MACD is below zero, goes
				 * upwards and is crossed by the Signal Line going downwards).
				 */
				if (MacdCurrent[2] < 0 && MacdCurrent[2] > MacdPrevious[2] && crossed(MacdCurrent[2], SignalCurrent)
						&& SignalPrevious > SignalCurrent) {
					if (MacdPrevious[0] < SignalPrevious
							&& Math.abs(MacdCurrent[0]) > (MACDCloseLevel * (instrument.getPipValue() * amount * 100000))) {
						logger.debug("yes!");
						ret.setTag(StateMachine.CLOSE);
						ret.setValue(2);
					}
				}

			}
		}

		Signal ret = new Signal();
		ret.setValue(0);
		return ret;
	}

	@Override
	public Signal signalStrength(Instrument instrument, Period period, IBar askBar, IBar bidBar, State actual)
			throws JFException {
		if (periods.contains(period) && instruments.contains(instrument)
				&& StateMachine.getInstance().getOrders().size() == 0) {

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
			// no trade out of london, new york session time
			String[] markets = { "London", "New York" };
			StringBuilder closedMarkets = new StringBuilder();
			for (int i = 0; i < markets.length; i++) {
				if (!Main.isMarketOpen(markets[i])) {
					closedMarkets.append(markets[i] + ",");
				}
			}

			// if (closedMarkets.toString().split(",").length > 1) { // from L
			// till
			// // NY
			// // closes
			// Main.massDebug(logger, closedMarkets.toString() +
			// " market(s) is/are closed.");
			// return ret;
			// }

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
			logger.debug(getName() + " searching to open position - " + period.name());
			try {
				// iMACD(NULL,0,12,26,9,PRICE_CLOSE,MODE_MAIN,0);
				MacdCurrent = indicators.macd(instrument, Period.ONE_HOUR, OfferSide.ASK,
						IIndicators.AppliedPrice.CLOSE, 12, 26, 9, 1);
				MacdPrevious = indicators.macd(instrument, Period.ONE_HOUR, OfferSide.ASK,
						IIndicators.AppliedPrice.CLOSE, 12, 26, 9, 2);
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
			logger.debug("MacdCurrent[2] < 0: " + MacdCurrent[2]);
			logger.debug("MacdCurrent[0] > SignalCurrent: " + SignalCurrent);
			logger.debug("MacdPrevious[2] < SignalCurrent: " + MacdCurrent[2] + "<" + SignalCurrent);
			// logger.debug("Math.abs(MacdCurrent[0]) > (MACDOpenLevel * (instrument.getPipValue() * amount * 100000)): "
			// + (MACDOpenLevel * (instrument.getPipValue() * amount *
			// 100000)));
			// logger.debug("MaCurrent > MaPrevious: " + MaCurrent + ">" +
			// MaPrevious);
			/*
			 * Long (BUY) entry – MACD indicator is below zero, goes upwards and
			 * is crossed by the Signal Line going downwards.
			 */
			if (MacdCurrent[2] < 0 && MacdCurrent[2] > MacdPrevious[2] && crossed(MacdCurrent[2], SignalCurrent)
					&& SignalCurrent < SignalPrevious) {
				// if (Math.abs(MacdCurrent[0]) > (MACDOpenLevel *
				// (instrument.getPipValue() * amount * 100000))
				// && MaCurrent > MaPrevious) {
				logger.debug("yes!");
				ret.setType(OrderCommand.BUY);
				ret.setValue(2);
				return ret;
				// }
			}
			/*
			 * sell strategy
			 */
			try {
				// iMACD(NULL,0,12,26,9,PRICE_CLOSE,MODE_MAIN,0);
				MacdCurrent = indicators.macd(instrument, Period.ONE_HOUR, OfferSide.BID,
						IIndicators.AppliedPrice.CLOSE, 12, 26, 9, 1);
				MacdPrevious = indicators.macd(instrument, Period.ONE_HOUR, OfferSide.BID,
						IIndicators.AppliedPrice.CLOSE, 12, 26, 9, 2);
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
			/*
			 * Short (SELL) entry – MACD indicator is above zero, goes downwards
			 * and is crossed by the Signal Line going upwards.
			 */
			if (MacdCurrent[2] > 0 && MacdCurrent[2] < MacdPrevious[2] && crossed(MacdCurrent[2], SignalCurrent)
					&& SignalCurrent > SignalPrevious) {
				// if (MacdCurrent[0] > (MACDOpenLevel *
				// (instrument.getPipValue() * amount * 100000))
				// && MaCurrent < MaPrevious) {
				logger.debug("yes!");
				ret.setType(OrderCommand.SELL);
				ret.setValue(2);
				return ret;
				// }
			}
			return ret;
		}
		Signal ret = new Signal();
		ret.setValue(0);
		return ret;
	}

	private boolean crossed(double a, double b) {
		double diff = Math.abs(a - b);
		// logger.debug("a: " + a);
		// logger.debug("b: " + b);
		// logger.debug("diff: " + diff);
		// // diff is less than %5
		double small = 0.05;
		if (diff < 0) {
			// logger.debug("diff/b: " + diff/b);
			return (Math.abs(diff / b) < small);
		} else {
			// logger.debug("diff/a: " + diff/a);
			return (Math.abs(diff / a) < small);
		}

	}
}
