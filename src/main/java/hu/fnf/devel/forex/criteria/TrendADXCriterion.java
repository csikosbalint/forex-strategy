package hu.fnf.devel.forex.criteria;

import com.dukascopy.api.IIndicators;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;

import hu.fnf.devel.forex.StateMachine;
import hu.fnf.devel.forex.utils.Criterion;
import hu.fnf.devel.forex.utils.ExclusionDecorator;
import hu.fnf.devel.forex.utils.Signal;

public class TrendADXCriterion extends ExclusionDecorator {

	private int trend;
	private int limit = 25;

	public TrendADXCriterion(Criterion criterion, int trend) {
		super(criterion);

		this.trend = trend;
	}

	@Override
	protected void check(Signal challenge) {
		IIndicators indicators = StateMachine.getInstance().getContext().getIndicators();
		double adx = 0;
		try {
			adx = indicators.adx(challenge.getInstrument(), challenge.getPeriod(), OfferSide.ASK, 13, 1);
		} catch (JFException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		switch (trend) {
		case StateMachine.TREND:
			if (adx < limit) {
				setExclusion();
			}
			break;
		case StateMachine.CHAOS:
			if (adx > limit) {
				setExclusion();
			}
		default:
			break;
		}

	}

}
