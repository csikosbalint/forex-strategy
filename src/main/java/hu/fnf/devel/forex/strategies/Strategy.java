package hu.fnf.devel.forex.strategies;

import java.util.HashSet;
import java.util.Set;

import com.dukascopy.api.IContext;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.Period;

public abstract class Strategy {
	protected Set<Instrument> instruments = new HashSet<Instrument>();
	protected Set<Period> periods = new HashSet<Period>();
	protected IContext context;
	
	abstract public String getName();

	public void addInstrument(Instrument instrument) {
		instruments.add(instrument);
	}
	
	public void addPeriod(Period period) {
		periods.add(period);
	}
	
	public void setContext(IContext context) {
		this.context = context;
	}

	public IContext getContext() {
		return context;
	}

	public Set<Instrument> getInstruments() {
		return instruments;
	}

	public Set<Period> getPeriods() {
		return periods;
	}

	/*
	 * common calculations
	 */
	protected int countOrders(IOrder.State ostate) {
		return 0;

	}

	

}
