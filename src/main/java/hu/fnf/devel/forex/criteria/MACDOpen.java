package hu.fnf.devel.forex.criteria;

import hu.fnf.devel.forex.StateMachine;
import hu.fnf.devel.forex.utils.Criterion;
import hu.fnf.devel.forex.utils.OpenCriterionDecorator;
import hu.fnf.devel.forex.utils.Signal;
import hu.fnf.devel.forex.utils.State;

import com.dukascopy.api.IBar;
import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IIndicators;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;

public class MACDOpen extends OpenCriterionDecorator {
	/*
	 * config
	 */
	private final double max = 1.0;
	private final double MACDOpenLevel = 5.0;
	int a = 12;
	int b = 26;
	int c = 9;

	public MACDOpen(Criterion criterion) {
		super(criterion);
	}

	@Override
	protected double calc(Signal challenge, Period period, IBar askBar, IBar bidBar, State actual) {
		double MacdCurrent[] = null;
		double MacdPrevious[] = null;
		double MacdPrevPrev[] = null;
		double SignalPrevious = 0;
		double SignalPrevPrev = 0;

		IIndicators indicators = StateMachine.getInstance().getContext().getIndicators();

		try {
			MacdCurrent = indicators.macd(challenge.getInstrument(), period, OfferSide.ASK,
					IIndicators.AppliedPrice.CLOSE, a, b, c, 0);
			MacdPrevious = indicators.macd(challenge.getInstrument(), period, OfferSide.ASK,
					IIndicators.AppliedPrice.CLOSE, a, b, c, 1);
			MacdPrevPrev = indicators.macd(challenge.getInstrument(), period, OfferSide.BID,
					IIndicators.AppliedPrice.CLOSE, a, b, c, 2);

			SignalPrevious = MacdPrevious[1];
			SignalPrevPrev = MacdPrevPrev[1];

		} catch (JFException e) {
			// throw new JFException("Error during buy calculation.", e);
		}
		/*
		 * Long (BUY) entry – MACD indicator is below zero, goes upwards and is
		 * crossed by the Signal Line going downwards.
		 */
		if (MacdCurrent[0] < 0 && MacdPrevious[0] > MacdPrevPrev[0]
				&& Math.signum(MacdCurrent[2]) != Math.signum(MacdPrevious[2]) && SignalPrevious < SignalPrevPrev) {
			if (MacdCurrent[0] < -1 * (MACDOpenLevel * (challenge.getInstrument().getPipValue()))) {
				challenge.setType(OrderCommand.BUY);
				logger.debug("MACDCur  < 0         :\t" + MacdCurrent[0] + " < " + "0");
				logger.debug("MACDPre  > MACDPrePre:\t" + MacdPrevious[0] + " > " + MacdPrevPrev[0]);
				logger.debug("MACDSigPre < MACDSigPrePre:\t" + SignalPrevious + " < " + SignalPrevPrev);
				logger.debug("sign(MACDHistCur) != sign(MACDHistPre)");
				return this.max;
			}
		}
		/*
		 * sell strategy
		 */
		try {
			MacdCurrent = indicators.macd(challenge.getInstrument(), period, OfferSide.BID,
					IIndicators.AppliedPrice.CLOSE, a, b, c, 0);
			MacdPrevious = indicators.macd(challenge.getInstrument(), period, OfferSide.BID,
					IIndicators.AppliedPrice.CLOSE, a, b, c, 1);
			MacdPrevPrev = indicators.macd(challenge.getInstrument(), period, OfferSide.BID,
					IIndicators.AppliedPrice.CLOSE, a, b, c, 2);

			SignalPrevious = MacdPrevious[1];
			SignalPrevPrev = MacdPrevPrev[1];

		} catch (JFException e) {
			// throw new JFException("Error during buy calculation.", e);
		}
		/*
		 * Short (SELL) entry – MACD indicator is above zero, goes downwards and
		 * is crossed by the Signal Line going upwards.
		 */
		if (MacdCurrent[0] > 0 && MacdPrevious[0] < MacdPrevPrev[0]
				&& Math.signum(MacdCurrent[2]) != Math.signum(MacdPrevious[2]) && SignalPrevious > SignalPrevPrev) {
			if (MacdCurrent[0] > (MACDOpenLevel * (challenge.getInstrument().getPipValue()))) {
				challenge.setType(OrderCommand.SELL);
				logger.debug("MACDCur  > 0         :\t" + MacdCurrent[0] + " > " + "0");
				logger.debug("MACDPre  < MACDPrePre:\t" + MacdPrevious[0] + " < " + MacdPrevPrev[0]);
				logger.debug("MACDSigPre > MACDSigPrePre:\t" + SignalPrevious + " > " + SignalPrevPrev);
				logger.debug("sign(MACDHistCur) != sign(MACDHistPre)");
				return this.max;
			}
		}
		return 0;
	}

}
