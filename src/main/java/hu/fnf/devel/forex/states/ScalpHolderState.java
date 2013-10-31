package hu.fnf.devel.forex.states;

import hu.fnf.devel.forex.Signal;
import hu.fnf.devel.forex.StateMachine;
import hu.fnf.devel.forex.commands.CloseAllCommand;
import hu.fnf.devel.forex.commands.OpenCommand;

import java.util.HashSet;
import java.util.Set;

import com.dukascopy.api.IContext;
import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IIndicators;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;

public class ScalpHolderState extends State {
	private double range;
	private double amount;

	public ScalpHolderState() {
		super("ScalpHolderState");
		// config
		instruments = new HashSet<Instrument>();
		instruments.add(Instrument.GBPJPY);
		instruments.add(Instrument.USDJPY);

		range = 10;
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
		IContext context = StateMachine.getInstance().getContext();
		Signal ret = new Signal();
		ret.setValue(0);
		ret.setInstrument(instrument);
		ret.setAmount(amount);

		// TODO: in this point anomailes have to be handeled

		LOGGER.debug("signalStrength calculation myOrders.size: " + StateMachine.getInstance().getOrders().size());

		if (StateMachine.getInstance().getOrders().size() == 0) {
			ret.setTag(StateMachine.OPEN);
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
					LOGGER.debug("No buy red:\t" + red[red.length - 1] + " > ask: " + tick.getAsk());
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
					LOGGER.debug("No sell red:\t" + red[2] + " < ask: " + tick.getBid());
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
			ret.setTag(StateMachine.CLOSE);
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
	public boolean onArriving() {
		return true;
	}

	@Override
	public boolean onLeaving() {
		return true;
	}

	@Override
	public void prepareCommands(Signal signal) {
		switch (signal.getTag()) {
		case StateMachine.OPEN:
			commands.add(new OpenCommand(signal));
			break;
		case StateMachine.CLOSE:
			commands.add(new CloseAllCommand());
		default:
			break;
		}
	}

}
