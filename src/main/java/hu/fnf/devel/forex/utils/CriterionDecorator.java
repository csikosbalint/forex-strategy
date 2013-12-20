package hu.fnf.devel.forex.utils;

import org.apache.log4j.Logger;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Period;

public abstract class CriterionDecorator implements Criterion {
	protected static final Logger logger = Logger.getLogger(Criterion.class);
	protected Criterion criterion;
	protected final double max = 1.0;

	public CriterionDecorator(Criterion criterion) {
		this.criterion = criterion;
	}

	protected double calc(Signal challenge, ITick tick, State actual) {
		// TODO Auto-generated method stub
		return 0;
	}

	protected double calc(Signal challenge, Period period, IBar askBar, IBar bidBar, State actual) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getMax() {
		return criterion.getMax() + max;
	}

	/**
	 * just let it flow through the decoration chain
	 */
	@Override
	public void setExclusion() {
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

}
