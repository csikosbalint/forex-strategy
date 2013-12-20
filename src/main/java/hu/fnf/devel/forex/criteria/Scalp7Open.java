package hu.fnf.devel.forex.criteria;

import hu.fnf.devel.forex.StateMachine;
import hu.fnf.devel.forex.utils.Criterion;
import hu.fnf.devel.forex.utils.OpenCriterionDecorator;
import hu.fnf.devel.forex.utils.Signal;
import hu.fnf.devel.forex.utils.State;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IIndicators;
import com.dukascopy.api.IIndicators.AppliedPrice;
import com.dukascopy.api.ITick;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;

public class Scalp7Open extends OpenCriterionDecorator {
	/*
	 * config
	 */
	private final double volativity = 5.5;
	private final int a = 50;
	private final int b = 100;
	private final double r1 = 2.0;
	private final double r2 = 2.2;
	private final int d = 2;
	private final int e = 8;
	private final int f = 5;

	public Scalp7Open(Criterion criterion) {
		super(criterion);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected double calc(Signal challenge, ITick tick, State actual) {
		IIndicators indicators = StateMachine.getInstance().getContext().getIndicators();
//		double actRange;
//		try {
//			actRange = indicators.stdDev(challenge.getInstrument(), challenge.getPeriod(), OfferSide.ASK,
//					AppliedPrice.MEDIAN_PRICE, a, b, 0)
//					+ indicators.stdDev(challenge.getInstrument(), challenge.getPeriod(), OfferSide.BID,
//							AppliedPrice.MEDIAN_PRICE, a, b, 0) / 2;
//		} catch (JFException e1) {
//			logger.fatal("cannot determine volativity", e1);
//			return 0;
//		}
		try {
			if (indicators.rsi(challenge.getInstrument(), challenge.getPeriod(), OfferSide.ASK, AppliedPrice.MEDIAN_PRICE, a, 1) > 55 || 
					indicators.rsi(challenge.getInstrument(), challenge.getPeriod(), OfferSide.ASK, AppliedPrice.MEDIAN_PRICE, a, 1) < 45 ) {
				return 0;
			}
		} catch (JFException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			if ((indicators.stdDev(challenge.getInstrument(), challenge.getPeriod(), OfferSide.ASK,
					AppliedPrice.MEDIAN_PRICE, a, b, 0) + indicators.stdDev(challenge.getInstrument(),
					challenge.getPeriod(), OfferSide.BID, AppliedPrice.MEDIAN_PRICE, a, b, 0) / 2) < volativity) {
				return 0;
			}
		} catch (JFException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		

		/*
		 * buy strategy
		 */
		try {
			double red[] = indicators.bbands(challenge.getInstrument(), challenge.getPeriod(), OfferSide.ASK,
					IIndicators.AppliedPrice.MEDIAN_PRICE, a, r1, r1, IIndicators.MaType.SMA, 0);
			double yellow[] = indicators.bbands(challenge.getInstrument(), challenge.getPeriod(), OfferSide.ASK,
					IIndicators.AppliedPrice.MEDIAN_PRICE, a, r2, r2, IIndicators.MaType.SMA, 0);
			double macd[] = indicators.macd(challenge.getInstrument(), challenge.getPeriod(), OfferSide.ASK,
					AppliedPrice.MEDIAN_PRICE, d, e, f, 0);
			if (tick.getAsk() < red[2] && tick.getAsk() > yellow[2] && macd[2] > 0) {
				logger.info("it is a good sign to buy " + challenge.getInstrument().name());
				logger.debug("\tred: " + red[2] + "\t>\task: " + tick.getAsk());
				challenge.setType(OrderCommand.BUY);
				return this.max;
			}
		} catch (JFException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		 * sell strategy
		 */
		try {
			double red[] = indicators.bbands(challenge.getInstrument(), challenge.getPeriod(), OfferSide.BID,
					IIndicators.AppliedPrice.MEDIAN_PRICE, a, r1, r1, IIndicators.MaType.SMA, 0);
			double yellow[] = indicators.bbands(challenge.getInstrument(), challenge.getPeriod(), OfferSide.BID,
					IIndicators.AppliedPrice.MEDIAN_PRICE, a, r2, r2, IIndicators.MaType.SMA, 0);
			double macd[] = indicators.macd(challenge.getInstrument(), challenge.getPeriod(), OfferSide.BID,
					AppliedPrice.MEDIAN_PRICE, d, e, f, 0);
			if (tick.getBid() > red[0] && tick.getBid() < yellow[0] && macd[2] < 0) {
				logger.info("it is a good sign to sell " + challenge.getInstrument().name());
				logger.debug("\tred: " + red[0] + " < ask: " + tick.getBid());
				challenge.setType(OrderCommand.SELL);
				return this.max;
			}
		} catch (JFException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
}
