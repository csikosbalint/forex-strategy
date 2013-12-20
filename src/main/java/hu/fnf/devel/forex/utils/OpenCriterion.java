package hu.fnf.devel.forex.utils;

import org.apache.log4j.Logger;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Period;

public class OpenCriterion implements Criterion {
	protected static final Logger logger = Logger.getLogger(OpenCriterion.class);
	private boolean excluded = false;

	@Override
	public void setExclusion() {
		this.excluded = true;
	}

	@Override
	public boolean isExcluded() {
		return this.excluded;
	}

	@Override
	public double getMax() {
		return 0;
	}

	@Override
	public double calcProbability(Signal challenge, ITick tick, State actual) {
		return 0;
	}

	@Override
	public double calcProbability(Signal challenge, Period period, IBar askBar, IBar bidBar, State actual) {
		return 0;
	}

	@Override
	public void reset() {
		excluded = false;
	}

}
