package hu.fnf.devel.forex.strategies;

import hu.fnf.devel.forex.StateStrategy;
import hu.fnf.devel.forex.states.State;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;

public abstract class Strategy {
	protected Set<Instrument> instruments = new HashSet<Instrument>();
	protected Set<Period> periods = new HashSet<Period>();

	abstract public String getName();
	abstract public State onStop(List<IOrder> orders);

	public void addInstrument(Instrument instrument) {
		instruments.add(instrument);
	}

	public void addPeriod(Period period) {
		periods.add(period);
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
	protected int totalOrders(IOrder.State ostate) {
		/*
		 * all orders
		 */
		if (ostate == null) {
			try {

				return StateStrategy.getInstance().getContext().getEngine().getOrders().size();
			} catch (JFException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			int count = 0;
			try {
				for ( IOrder o: StateStrategy.getInstance().getContext().getEngine().getOrders() ) {
					if ( o.getState() == ostate ) {
						count++;
					}
				}
				return count;
			} catch (JFException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return 0;
	}

}
