package hu.fnf.devel.forex.utils;

import org.apache.log4j.Logger;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Period;

public abstract class CloseExclusionDecorator implements Criterion {
	/*
	 * flow through the max and the cacl if not exluded
	 * break the chain if exluded
	 */
	protected static final Logger logger = Logger.getLogger(CloseExclusionDecorator.class);
	protected Criterion criterion;

	public CloseExclusionDecorator(Criterion criterion) {
		this.criterion = criterion;
	}

	@Override
	public double getMax() {
		return criterion.getMax();
	}

	@Override
	public double calcProbability(Signal challenge, ITick tick, State actual) {
		check(challenge);
		if (isExcluded()) {
			return 0;
		}
		return this.criterion.calcProbability(challenge, tick, actual);
	}

	@Override
	public double calcProbability(Signal challenge, Period period, IBar askBar, IBar bidBar, State actual) {
		check(challenge);
		if (isExcluded()) {
			return 0;
		}
		return this.criterion.calcProbability(challenge, period, askBar, bidBar, actual);
	}

	@Override
	public void setExclusion() {
		logger.info("CloseExclusion set");
		this.criterion.setExclusion();
	}

	@Override
	public boolean isExcluded() {
		return this.criterion.isExcluded();
	}

	@Override
	public void reset() {
		this.criterion.reset();
	}

	protected abstract void check(Signal challenge);
}
