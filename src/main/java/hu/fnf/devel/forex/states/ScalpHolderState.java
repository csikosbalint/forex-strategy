package hu.fnf.devel.forex.states;

import hu.fnf.devel.forex.Signal;
import hu.fnf.devel.forex.StateMachine;
import java.util.HashSet;
import java.util.Set;

import com.dukascopy.api.IContext;
import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IIndicators.AppliedPrice;
import com.dukascopy.api.IIndicators;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;

public class ScalpHolderState extends State {
	private double range;
	private double loss;
	private double profit;
	private double amount;

	public ScalpHolderState() {
		super("ScalpHolderState");
		// config
		instruments = new HashSet<Instrument>();
		instruments.add(Instrument.GBPJPY);
		instruments.add(Instrument.USDJPY);
		instruments.add(Instrument.EURJPY);

		amount = 0.1;
	}

	@Override
	public Set<State> nextStates() {
		Set<State> ret = new HashSet<State>();
		ret.add(new SignalSeekerState());

		return ret;
	}

	@Override
	public Signal signalStrength(Instrument instrument, ITick tick, State actual) {
		return signalStrength(instrument, tick);
	}

	private Signal signalStrength(Instrument instrument, ITick tick) {
		IContext context = StateMachine.getInstance().getContext();
		Signal ret = new Signal();
		ret.setValue(0);
		ret.setInstrument(instrument);
		ret.setAmount(amount);

		// TODO: in this point anomailes have to be handeled

		LOGGER.debug(getName() + " signalStrength calculation myOrders.size: "
				+ StateMachine.getInstance().getOrders().size());

		if (StateMachine.getInstance().getOrders().size() == 0) {
			try {
				range = StateMachine.getInstance().getContext().getIndicators()
						.stdDev(instrument, Period.ONE_MIN, OfferSide.ASK, AppliedPrice.MEDIAN_PRICE, 50, 100, 0)
						+ StateMachine
								.getInstance()
								.getContext()
								.getIndicators()
								.stdDev(instrument, Period.ONE_MIN, OfferSide.BID, AppliedPrice.MEDIAN_PRICE, 50, 100,
										0) / 2;
			} catch (JFException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if (range < 4) {
				LOGGER.debug("volativity is too low for " + instrument.name() + " (" + range + ")");
				return ret;
			} else {
				LOGGER.debug("volativity is for " + instrument.name() + " (" + range + ")");
			}
			if (!instruments.contains(instrument)) {
				return ret;
			}
			/*
			 * buy strategy
			 */
			ret.setTag(StateMachine.OPEN);
			try {
				double red[] = context.getIndicators().bbands(instrument, Period.ONE_MIN, OfferSide.ASK,
						IIndicators.AppliedPrice.MEDIAN_PRICE, 50, 1, 1, IIndicators.MaType.SMA, 0);
				double orange[] = context.getIndicators().bbands(instrument, Period.ONE_MIN, OfferSide.ASK,
						IIndicators.AppliedPrice.MEDIAN_PRICE, 50, 1.5, 1.5, IIndicators.MaType.SMA, 0);
				double yellow[] = context.getIndicators().bbands(instrument, Period.ONE_MIN, OfferSide.ASK,
						IIndicators.AppliedPrice.MEDIAN_PRICE, 50, 2, 2, IIndicators.MaType.SMA, 0);
				if (tick.getAsk() < red[2] - ((red[2] - orange[2]) / 2) && tick.getAsk() > yellow[2]) {
					LOGGER.info("it is a good sign to buy " + instrument.name());
					LOGGER.info("\tred: " + red[2] + "\t>\task: " + tick.getAsk());
					loss = red[2] - orange[2] / instrument.getPipValue();
					profit = 3;
					ret.setType(OrderCommand.BUY);
					if (tick.getAsk() < orange[2]) {
						ret.setValue(2);
					} else {
						ret.setValue(1);
					}
				} else {
					LOGGER.debug("No buy");
					LOGGER.debug("    orange:\t" + (red[2] - ((red[2] - orange[2]) / 2)) + " < ask: " + (tick.getAsk()));
					LOGGER.debug("    yellow:\t" + (yellow[2]) + " > ask: " + (tick.getAsk()));
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
				double yellow[] = context.getIndicators().bbands(instrument, Period.ONE_MIN, OfferSide.BID,
						IIndicators.AppliedPrice.MEDIAN_PRICE, 50, 2, 2, IIndicators.MaType.SMA, 0);
				if (tick.getBid() > red[0] + ((orange[0] - red[0]) / 2) && yellow[0] > tick.getBid()) {
					LOGGER.info("it is a good sign to sell " + instrument.name());
					LOGGER.info("\tred: " + red[0] + " < ask: " + tick.getBid());
					loss = orange[0] - red[0] / instrument.getPipValue();
					profit = 3;
					ret.setType(OrderCommand.SELL);
					if (tick.getBid() > orange[0]) {
						ret.setValue(2);
					} else {
						ret.setValue(1);
					}
				} else {
					LOGGER.debug("No sell");
					LOGGER.debug("    r+(o-r/2):\t" + (red[0] + ((orange[0] - red[0]) / 2)) + " > bid: "
							+ (tick.getBid()));
					LOGGER.debug("    yellow:\t" + (yellow[0]) + " < bid: " + (tick.getBid()));
				}
			} catch (JFException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			LOGGER.debug("retval is " + ret.getType() + "(" + ret.getValue() + ")");
			return ret;
		} else {
			/*
			 * close strategy
			 */
			range -= 0.005;
			if (instrument != StateMachine.getInstance().getOrders().get(0).getInstrument()) {
				return ret;
			}
			ret.setTag(StateMachine.CLOSE);
			if (StateMachine.getInstance().getOrders().get(0).getProfitLossInPips() > profit) {
				LOGGER.info("close!\tpip for " + StateMachine.getInstance().getOrders().get(0).getId() + ": "
						+ StateMachine.getInstance().getOrders().get(0).getProfitLossInPips() + "/" + Math.ceil(profit));
				ret.setValue(2);
			} else if (StateMachine.getInstance().getOrders().get(0).getProfitLossInPips() < -1 * loss) {
				LOGGER.info("close!\tpip for " + StateMachine.getInstance().getOrders().get(0).getId() + ": "
						+ StateMachine.getInstance().getOrders().get(0).getProfitLossInPips() + "/"
						+ Math.ceil(-1 * loss));
				ret.setValue(2);
			} else {
				LOGGER.debug("No close:\tpip for " + StateMachine.getInstance().getOrders().get(0).getId() + ": "
						+ StateMachine.getInstance().getOrders().get(0).getProfitLossInPips() + "/" + loss + " - " + -1
						* profit);
			}
		}
		return ret;
	}

	@Override
	public boolean onArriving() {
		return true;
	}

	@Override
	public boolean onLeaving() {
		// removing references
		this.commands = null;
		this.instruments = null;
		this.periods = null;
		this.signal = null;
		return true;
	}
}
