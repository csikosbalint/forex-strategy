package hu.fnf.devel.forex.utils;

import org.apache.log4j.Logger;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Period;


public class CloseCriterion implements Criterion {
	protected static final Logger logger = Logger.getLogger(CloseCriterion.class);
	// TODO: this is an error! a field variable but many method uses!!
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
		if ( period.equals(Period.ONE_HOUR ) ) {
			logger.info("Searching for promising state(s)...");
		}
		return 0;
	}
	
	@Override
	public void reset() {
		excluded = false;
	}
}
