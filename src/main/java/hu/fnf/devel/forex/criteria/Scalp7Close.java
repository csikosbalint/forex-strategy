package hu.fnf.devel.forex.criteria;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IIndicators;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.ITick;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;

import hu.fnf.devel.forex.StateMachine;
import hu.fnf.devel.forex.utils.CloseCriterionDecorator;
import hu.fnf.devel.forex.utils.Criterion;
import hu.fnf.devel.forex.utils.Signal;
import hu.fnf.devel.forex.utils.State;

public class Scalp7Close extends CloseCriterionDecorator {
	/*
	 * config
	 */
	private final double range = 8;
	private final double range_good = 20;
	private final double range_ugly = 2;
	private final int a = 50;
	private final double r1 = 1.0;
	private final double pip_close = 0.05;
	private final int mins15 = 900000;
	public Scalp7Close(Criterion criterion) {
		super(criterion);
	}

	@Override
	protected double calc(Signal challenge, ITick tick, State actual) {
		int c = 2; // the START#ID order and this
		if ( StateMachine.getInstance().getContext().getEngine().getType().equals(IEngine.Type.TEST) ) {
			c--;
		}
		try {
			if (StateMachine.getInstance().getContext().getEngine().getOrders().size() < c) {
				return 0;
			}
		} catch (JFException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// TODO: recognize range when there is scalp at starting
		double center[] = null;
		IOrder order = null;
		try {
			order = StateMachine.getInstance().getContext().getEngine().getOrders().get(0);
		} catch (JFException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		IIndicators indicators = StateMachine.getInstance().getContext().getIndicators();
		if (order.getOrderCommand() == IEngine.OrderCommand.BUY) {
			try {
				center = indicators.bbands(challenge.getInstrument(), challenge.getPeriod(), OfferSide.ASK,
						IIndicators.AppliedPrice.MEDIAN_PRICE, a, r1, r1, IIndicators.MaType.SMA, 0);
			} catch (JFException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				center = indicators.bbands(challenge.getInstrument(), challenge.getPeriod(), OfferSide.BID,
						IIndicators.AppliedPrice.MEDIAN_PRICE, a, r1, r1, IIndicators.MaType.SMA, 0);
			} catch (JFException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (order.getProfitLossInPips() > range * range_good) {
			logger.info("Good!\tpip for " + order.getId() + ": "
					+ order.getProfitLossInPips() + "/"
					+ Math.ceil(range * range_good));
			return this.max;
		} else if (order.getProfitLossInPips() < -1 * range * range_ugly) {
			logger.info("Bad!\tpip for " + order.getId() + ": "
					+ order.getProfitLossInPips() + "/"
					+ Math.ceil(-1 * range * range_ugly));
			return this.max;
		} else if (Math.abs((Math.abs((tick.getAsk() + tick.getBid()) / 2) - center[1])
				/ order.getInstrument().getPipValue()) < pip_close && order.getProfitLossInUSD() > order.getCommissionInUSD()) {
			logger.info("Too close to the center line: "
					+ Math.abs((Math.abs((tick.getAsk() + tick.getBid()) / 2) - center[1])
							/ order.getInstrument().getPipValue()));
			return this.max;
		} else
			try {
				if (StateMachine.getInstance().getContext().getHistory().getLastTick(challenge.getInstrument())
						.getTime()
						- order.getCreationTime() > mins15) {
																										// min
					if (order.getProfitLossInUSD() < order.getCommissionInUSD() ) {
						//Main.massDebug(logger, "It is a long(10min) scalp but it is in a loss...waiting.");
						return 0;
					} else {
						//Main.massDebug(logger, "It is a long(15min) scalp. Closing it with profit!");
						return this.max;	
					}
				} else if (StateMachine.getInstance().getContext().getHistory().getLastTick(challenge.getInstrument())
						.getTime()
						- order.getCreationTime() > mins15) {
																										// min
					logger.info("Too long open position for a scalp."
							+ order.getProfitLossInUSD() + ".");
					return this.max;
				}
			} catch (JFException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return 0;
	}

}
