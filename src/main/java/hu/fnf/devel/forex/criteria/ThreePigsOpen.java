package hu.fnf.devel.forex.criteria;

import com.dukascopy.api.IBar;
import com.dukascopy.api.IIndicators;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IIndicators.AppliedPrice;
import com.dukascopy.api.impl.Indicators;
import com.dukascopy.api.indicators.IIndicator;
import com.dukascopy.charts.listener.ChartModeChangeListener.ChartMode;

import hu.fnf.devel.forex.StateMachine;
import hu.fnf.devel.forex.utils.Criterion;
import hu.fnf.devel.forex.utils.OpenCriterionDecorator;
import hu.fnf.devel.forex.utils.Signal;
import hu.fnf.devel.forex.utils.State;

public class ThreePigsOpen extends OpenCriterionDecorator {

	public ThreePigsOpen(Criterion criterion) {
		super(criterion);
	}

	@Override
	protected double calc(Signal challenge, Period period, IBar askBar, IBar bidBar, State actual) {
		IIndicators indicators = StateMachine.getInstance().getContext().getIndicators();
		try {
			/*
			 * ASK
			 */
			double sma55weekly = indicators.sma(challenge.getInstrument(), Period.WEEKLY, OfferSide.ASK, AppliedPrice.MEDIAN_PRICE, 55, 0);
			double sma21daily = indicators.sma(challenge.getInstrument(), Period.DAILY, OfferSide.ASK, AppliedPrice.MEDIAN_PRICE, 21, 0);
			double sma34fhours = indicators.sma(challenge.getInstrument(), Period.FOUR_HOURS, OfferSide.ASK, AppliedPrice.CLOSE, 34, 0);
			double price = askBar.getClose();
			if ( price > sma55weekly && price > sma21daily && price > sma34fhours ) {
				challenge.setType(OrderCommand.SELL);
				return this.max;
			}
			sma55weekly = indicators.sma(challenge.getInstrument(), Period.WEEKLY, OfferSide.BID, AppliedPrice.MEDIAN_PRICE, 55, 0);
			sma21daily = indicators.sma(challenge.getInstrument(), Period.DAILY, OfferSide.BID, AppliedPrice.MEDIAN_PRICE, 21, 0);
			sma34fhours = indicators.sma(challenge.getInstrument(), Period.FOUR_HOURS, OfferSide.BID, AppliedPrice.CLOSE, 34, 0);
			price = bidBar.getClose();
			if ( price < sma55weekly && price < sma21daily && price < sma34fhours ) {
				challenge.setType(OrderCommand.BUY);
				return this.max;
			}
		} catch (JFException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return 0;
	}

}
