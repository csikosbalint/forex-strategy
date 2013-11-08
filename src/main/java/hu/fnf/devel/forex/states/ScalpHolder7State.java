package hu.fnf.devel.forex.states;

import hu.fnf.devel.forex.Main;
import hu.fnf.devel.forex.StateMachine;
import hu.fnf.devel.forex.utils.Signal;

import java.util.HashSet;

import org.apache.log4j.Logger;

import com.dukascopy.api.IContext;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IIndicators;
import com.dukascopy.api.IIndicators.AppliedPrice;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;

public class ScalpHolder7State extends State {
	private static final Logger logger = Logger.getLogger(ScalpHolder7State.class);
	private double range = 8;
	private double amount = 0.1;
	private double volativity = 2.5;

	public ScalpHolder7State() {
		super("ScalpHolderState");
		// config
		instruments = new HashSet<Instrument>();
		// instruments.add(Instrument.GBPJPY);
		instruments.add(Instrument.USDJPY);
		instruments.add(Instrument.EURJPY);
	}

	@Override
	public Signal signalStrength(Instrument instrument, ITick tick, State actual) throws JFException {
		return signalStrength(instrument, tick);
	}

	private Signal signalStrength(Instrument instrument, ITick tick) throws JFException {
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

		if (closedMarkets.toString().split(",").length > 1) { // from T opens till NY closes
			Main.massDebug(logger, closedMarkets.toString() + " market is closed.");
			return ret;
		}

		// TODO: in this point order anomailes have to be handeled

		if (StateMachine.getInstance().getOrders().size() == 0) {
			double actRange;
			try {
				actRange = StateMachine.getInstance().getContext().getIndicators()
						.stdDev(instrument, Period.ONE_MIN, OfferSide.ASK, AppliedPrice.MEDIAN_PRICE, 50, 100, 0)
						+ StateMachine
								.getInstance()
								.getContext()
								.getIndicators()
								.stdDev(instrument, Period.ONE_MIN, OfferSide.BID, AppliedPrice.MEDIAN_PRICE, 50, 100,
										0) / 2;
			} catch (JFException e1) {
				logger.fatal("cannot determine volativity", e1);
				return ret;
			}
			if ((StateMachine.getInstance().getContext().getIndicators()
					.stdDev(instrument, Period.ONE_MIN, OfferSide.ASK, AppliedPrice.MEDIAN_PRICE, 50, 100, 0) + StateMachine
					.getInstance().getContext().getIndicators()
					.stdDev(instrument, Period.ONE_MIN, OfferSide.BID, AppliedPrice.MEDIAN_PRICE, 50, 100, 0) / 2) < volativity) {
				logger.debug("volativity is too low for " + instrument.name() + " (" + actRange + ")");
				return ret;
			}

			/*
			 * buy strategy
			 */
			ret.setTag(StateMachine.OPEN);
			try {
				double red[] = context.getIndicators().bbands(instrument, Period.ONE_MIN, OfferSide.ASK,
						IIndicators.AppliedPrice.MEDIAN_PRICE, 50, 2, 2, IIndicators.MaType.SMA, 0);
				double yellow[] = context.getIndicators().bbands(instrument, Period.ONE_MIN, OfferSide.ASK,
						IIndicators.AppliedPrice.MEDIAN_PRICE, 50, 2.2, 2.2, IIndicators.MaType.SMA, 0);
				double macd[] = context.getIndicators().macd(instrument, Period.ONE_MIN, OfferSide.ASK,
						AppliedPrice.MEDIAN_PRICE, 2, 8, 5, 0);
				if (tick.getAsk() < red[2] && tick.getAsk() > yellow[2] && macd[2] > 0) {
					logger.info("it is a good sign to buy " + instrument.name());
					logger.info("\tred: " + red[2] + "\t>\task: " + tick.getAsk());

					ret.setType(OrderCommand.BUY);
					ret.setValue(2);

				} else {
					logger.debug("No buy");
					logger.debug("    red:  \t" + red[2] + " < ask: " + (tick.getAsk()));
					logger.debug("    yellow:  \t" + (yellow[2]) + " > ask: " + (tick.getAsk()));
				}
			} catch (JFException e) {
				throw new JFException("Error during buy calculation.", e);
			}
			/*
			 * sell strategy
			 */
			try {
				double red[] = context.getIndicators().bbands(instrument, Period.ONE_MIN, OfferSide.BID,
						IIndicators.AppliedPrice.MEDIAN_PRICE, 50, 2, 2, IIndicators.MaType.SMA, 0);
				double yellow[] = context.getIndicators().bbands(instrument, Period.ONE_MIN, OfferSide.BID,
						IIndicators.AppliedPrice.MEDIAN_PRICE, 50, 2.2, 2.2, IIndicators.MaType.SMA, 0);
				double macd[] = context.getIndicators().macd(instrument, Period.ONE_MIN, OfferSide.BID,
						AppliedPrice.MEDIAN_PRICE, 2, 8, 5, 0);
				if (tick.getBid() > red[0] && tick.getBid() < yellow[0] && macd[2] < 0) {
					logger.info("it is a good sign to sell " + instrument.name());
					logger.info("\tred: " + red[0] + " < ask: " + tick.getBid());

					ret.setType(OrderCommand.SELL);

					ret.setValue(2);

				} else {
					logger.debug("No sell");
					logger.debug("    red:\t" + red[0] + " > bid: "
							+ (tick.getBid()));
					logger.debug("    yellow:  \t" + (yellow[0]) + " < bid: " + (tick.getBid()));
				}
			} catch (JFException e) {
				throw new JFException("Error during sell calculation.", e);
			}
			logger.debug("retval is " + ret.getType() + "(" + ret.getValue() + ")");
			return ret;
		} else {
			/*
			 * close strategy
			 */
			// logger.info("Too long open position for a scalp. Unfortunatelly it is a loss $"
			// +
			// (StateMachine.getInstance().getContext().getHistory().getLastTick(instrument).getTime()
			// - StateMachine
			// .getInstance().getOrders().get(0).getCreationTime()) + ".");
			if (instrument != StateMachine.getInstance().getOrders().get(0).getInstrument()) {
				return ret;
			}
			// TODO: recognize range when there is scalp at starting
			double center[];
			if (StateMachine.getInstance().getOrders().get(0).getOrderCommand() == IEngine.OrderCommand.BUY) {
				center = context.getIndicators().bbands(instrument, Period.ONE_MIN, OfferSide.ASK,
						IIndicators.AppliedPrice.MEDIAN_PRICE, 50, 1, 1, IIndicators.MaType.SMA, 0);
			} else {
				center = context.getIndicators().bbands(instrument, Period.ONE_MIN, OfferSide.BID,
						IIndicators.AppliedPrice.MEDIAN_PRICE, 50, 1, 1, IIndicators.MaType.SMA, 0);
			}

			ret.setTag(StateMachine.CLOSE);
			if (StateMachine.getInstance().getOrders().get(0).getProfitLossInPips() > range * 0.6) {
				logger.info("Good!\tpip for " + StateMachine.getInstance().getOrders().get(0).getId() + ": "
						+ StateMachine.getInstance().getOrders().get(0).getProfitLossInPips() + "/"
						+ Math.ceil(range * 0.6));
				ret.setValue(2);
			} else if (StateMachine.getInstance().getOrders().get(0).getProfitLossInPips() < -1 * range * 0.4) {
				logger.info("Bad!\tpip for " + StateMachine.getInstance().getOrders().get(0).getId() + ": "
						+ StateMachine.getInstance().getOrders().get(0).getProfitLossInPips() + "/"
						+ Math.ceil(-1 * range * 0.4));
				ret.setValue(2);
			} else if (Math.abs((Math.abs((tick.getAsk() + tick.getBid()) / 2) - center[1])
					/ StateMachine.getInstance().getOrders().get(0).getInstrument().getPipValue()) < 1) {
				logger.info("Too close to the center line: "
						+ Math.abs((Math.abs((tick.getAsk() + tick.getBid()) / 2) - center[1])
								/ StateMachine.getInstance().getOrders().get(0).getInstrument().getPipValue()));
				ret.setValue(1);
			} else if (StateMachine.getInstance().getContext().getHistory().getLastTick(instrument).getTime()
					- StateMachine.getInstance().getOrders().get(0).getCreationTime() > 180000) { // 3
																									// min
				if (StateMachine.getInstance().getOrders().get(0).getProfitLossInUSD() < 0) {
					Main.massDebug(logger, "It is a long(3min) scalp but it is in a loss...waiting.");
					return ret;
				}
				Main.massDebug(logger, "It is a long(3min) scalp. Closing it with profit!");
				ret.setValue(1);
			} else if (StateMachine.getInstance().getContext().getHistory().getLastTick(instrument).getTime()
					- StateMachine.getInstance().getOrders().get(0).getCreationTime() > 300000) { // 5
																									// min
				logger.info("Too long open position for a scalp."
						+ StateMachine.getInstance().getOrders().get(0).getProfitLossInUSD() + ".");
				ret.setValue(2);
			} else {
				logger.debug("No close:\tpip for "
						+ StateMachine.getInstance().getOrders().get(0).getId()
						+ ": "
						+ StateMachine.getInstance().getOrders().get(0).getProfitLossInPips()
						+ "/"
						+ range
						* 0.6
						+ "/-"
						+ range
						* 0.4
						+ " center/price: "
						+ Math.ceil(center[1])
						+ "/"
						+ Math.ceil(Math.abs((tick.getAsk() + tick.getBid()) / 2))
						+ " distance: "
						+ Math.abs((Math.abs((tick.getAsk() + tick.getBid()) / 2) - center[1])
								/ StateMachine.getInstance().getOrders().get(0).getInstrument().getPipValue()));
			}
		}
		return ret;
	}
}
