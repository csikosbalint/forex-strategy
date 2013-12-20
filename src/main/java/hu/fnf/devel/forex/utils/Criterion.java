package hu.fnf.devel.forex.utils;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Period;

public interface Criterion {
	
	public void setExclusion();
	
	public abstract boolean isExcluded();

	public abstract double getMax();

	public abstract double calcProbability(Signal challenge, ITick tick, State actual);

	public abstract double calcProbability(Signal challenge, Period period, IBar askBar, IBar bidBar, State actual);

	public void reset();
}
