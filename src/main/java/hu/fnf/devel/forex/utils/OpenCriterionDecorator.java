package hu.fnf.devel.forex.utils;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Period;

public abstract class OpenCriterionDecorator extends CriterionDecorator {
	/*
	 * ret 0 if exluded
	 */

	public OpenCriterionDecorator(Criterion criterion) {
		super(criterion);
	}

	/**
	 * close exlusion returns maximum
	 */
	@Override
	public double calcProbability(Signal challenge, ITick tick, State actual) {
		double ret = this.criterion.calcProbability(challenge, tick, actual);
		if (isExcluded()) {
			return 0;
		}
		return ret + calc(challenge, tick, actual);
	}

	@Override
	public double calcProbability(Signal challenge, Period period, IBar askBar, IBar bidBar, State actual) {
		double ret = this.criterion.calcProbability(challenge, period, askBar, bidBar, actual);
		if (isExcluded()) {
			return 0;
		}
		return ret + calc(challenge, period, askBar, bidBar, actual);
	}
}
