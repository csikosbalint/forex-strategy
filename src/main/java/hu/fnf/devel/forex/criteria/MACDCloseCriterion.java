package hu.fnf.devel.forex.criteria;

import hu.fnf.devel.forex.StateMachine;
import hu.fnf.devel.forex.utils.CloseCriterionDecorator;
import hu.fnf.devel.forex.utils.Criterion;
import hu.fnf.devel.forex.utils.Signal;
import hu.fnf.devel.forex.utils.State;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IIndicators;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;

public class MACDCloseCriterion extends CloseCriterionDecorator {
	/*
	 * config
	 */
	private double MACDCloseLevel = 2.0;
	int a = 12;
	int b = 26;
	int c = 9;

	public MACDCloseCriterion(Criterion criterion) {
		super(criterion);
	}

	@Override
	protected double calc(Signal challenge, ITick tick, State actual) {
		int c = 2; // the START#ID order and this
		if ( StateMachine.getInstance().getContext().getEngine().getType().equals(IEngine.Type.TEST) ) {
			c--;
		}
		IOrder order = null;
		try {
			if (StateMachine.getInstance().getContext().getEngine().getOrders().size() < c) {
				return 0;
			}
			for (IOrder oIOrder : StateMachine.getInstance().getContext().getEngine().getOrders() ) {
				if ( oIOrder.getLabel().split("AND")[0].equalsIgnoreCase("MACDSample452State") ) {
					order = oIOrder;
				}
			}
		} catch (JFException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if ( order == null ) {
			// not our order
			return 0;
		}
		
		IIndicators indicators = StateMachine.getInstance().getContext().getIndicators();
		try {
			Period p = Period.valueOf(order.getLabel().split("AND")[1]);
			Instrument i = order.getInstrument();

			if (order.isLong()) {
				/*
				 * long Long exit – by execution of the take profit limit, by
				 * execution of the trailing stop or when MACD crosses its
				 * Signal Line (MACD is above zero, goes downwards and is
				 * crossed by the Signal Line going upwards).
				 */
				double MacdCurrent[];
				double MacdPrevious[];
				double MacdPrevPrev[];
				double SignalCurrent;
				double SignalPrevious;
				double SignalPrevPrev;

				try {
					MacdCurrent  = indicators.macd(i, p, OfferSide.BID, IIndicators.AppliedPrice.CLOSE, a, b, c, 0);
					MacdPrevious = indicators.macd(i, p, OfferSide.BID, IIndicators.AppliedPrice.CLOSE, a, b, c, 1);
					MacdPrevPrev = indicators.macd(i, p, OfferSide.BID, IIndicators.AppliedPrice.CLOSE, a, b, c, 2);

					SignalCurrent = MacdCurrent[1];
					SignalPrevious = MacdPrevious[1];
					SignalPrevPrev = MacdPrevPrev[1];

				} catch (JFException e) {
					throw new JFException("Error during buy calculation.", e);
				}

				if (MacdCurrent[0] > 0 && MacdPrevPrev[0] > MacdPrevious[0] && SignalPrevPrev < SignalPrevious
						&& Math.signum(MacdCurrent[2]) != Math.signum(MacdPrevious[2])) {
					if (SignalCurrent > 0 && MacdCurrent[0] > (MACDCloseLevel * challenge.getInstrument().getPipValue())) {
						return this.max;
					}
				}
			} else {
				/*
				 * short
				 */
				double MacdCurrent[];
				double MacdPrevious[];
				double MacdPrevPrev[];
				double SignalCurrent;
				double SignalPrevious;
				double SignalPrevPrev;
				try {
					MacdCurrent = indicators.macd(i, p, OfferSide.BID, IIndicators.AppliedPrice.CLOSE, a, b, c, 0);
					MacdPrevious = indicators.macd(i, p, OfferSide.BID, IIndicators.AppliedPrice.CLOSE, a, b, c, 1);
					MacdPrevPrev = indicators.macd(i, p, OfferSide.BID, IIndicators.AppliedPrice.CLOSE, a, b, c, 2);

					SignalCurrent = MacdCurrent[1];
					SignalPrevious = MacdPrevious[1];
					SignalPrevPrev = MacdPrevPrev[1];

				} catch (JFException e) {
					throw new JFException("Error during buy calculation.", e);
				}

				if (MacdCurrent[0] < 0) {
					if (MacdPrevPrev[0] < MacdPrevious[0]) {
						// logger.debug(MacdCurrent[2]);
						if (SignalPrevPrev > SignalPrevious) {
							// logger.debug(SignalPrevPrev + " > " +
							// SignalPrevious);
							if (Math.signum(MacdCurrent[2]) != Math.signum(MacdPrevious[2])) {
								// logger.debug("Math.signum(MacdCurrent[2]) != Math.signum(MacdPrevious[2]");
								if (SignalCurrent < 0 && MacdCurrent[0] < -1 * (MACDCloseLevel * challenge.getInstrument().getPipValue())) {
									// logger.debug("5");
									return this.max;
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

}
