package hu.fnf.devel.forex.criteria;

import hu.fnf.devel.forex.StateMachine;
import hu.fnf.devel.forex.utils.CloseCriterionDecorator;
import hu.fnf.devel.forex.utils.Criterion;
import hu.fnf.devel.forex.utils.Signal;
import hu.fnf.devel.forex.utils.State;

import com.dukascopy.api.IIndicators;
import com.dukascopy.api.ITick;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;

public class MACDClose extends CloseCriterionDecorator {
	/*
	 * config
	 */
	private double MACDCloseLevel = 2.0;
	int a = 12;
	int b = 26;
	int c = 9;

	public MACDClose(Criterion criterion) {
		super(criterion);
	}

	@Override
	protected double calc(Signal challenge, ITick tick, State actual) {

		try {
			if (StateMachine.getInstance().getContext().getEngine().getOrders().size() < 2) {
				return 0;
			}
		} catch (JFException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		IIndicators indicators = StateMachine.getInstance().getContext().getIndicators();
		try {
			if (StateMachine.getInstance().getContext().getEngine().getOrders().get(0).isLong()) {
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
					MacdCurrent = indicators.macd(challenge.getInstrument(), challenge.getPeriod(), OfferSide.BID,
							IIndicators.AppliedPrice.CLOSE, a, b, c, 0);
					MacdPrevious = indicators.macd(challenge.getInstrument(), challenge.getPeriod(), OfferSide.BID,
							IIndicators.AppliedPrice.CLOSE, a, b, c, 1);
					MacdPrevPrev = indicators.macd(challenge.getInstrument(), challenge.getPeriod(), OfferSide.BID,
							IIndicators.AppliedPrice.CLOSE, a, b, c, 2);

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
					// iMACD(NULL,0,12,26,9,PRICE_CLOSE,MODE_MAIN,0);
					MacdCurrent = indicators.macd(challenge.getInstrument(), challenge.getPeriod(), OfferSide.BID,
							IIndicators.AppliedPrice.CLOSE, a, b, c, 0);
					MacdPrevious = indicators.macd(challenge.getInstrument(), challenge.getPeriod(), OfferSide.BID,
							IIndicators.AppliedPrice.CLOSE, a, b, c, 1);
					MacdPrevPrev = indicators.macd(challenge.getInstrument(), challenge.getPeriod(), OfferSide.BID,
							IIndicators.AppliedPrice.CLOSE, a, b, c, 2);

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
