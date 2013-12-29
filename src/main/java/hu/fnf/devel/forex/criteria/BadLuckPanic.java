package hu.fnf.devel.forex.criteria;

import hu.fnf.devel.forex.StateMachine;
import hu.fnf.devel.forex.utils.Criterion;
import hu.fnf.devel.forex.utils.ExclusionDecorator;
import hu.fnf.devel.forex.utils.Signal;

import com.dukascopy.api.IHistory;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;

public class BadLuckPanic extends ExclusionDecorator {
	/*
	 * OPEN exlusion
	 */

	private int trades;
	private int days;

	public BadLuckPanic(Criterion criterion, int days, int trades) {
		super(criterion);
		this.days = days;
		this.trades = trades;
	}
	
	@Override
	protected void check(Signal challenge) {
		IHistory history = StateMachine.getInstance().getContext().getHistory();

		long dayss = 86400000 * days; // 3days
		long nows;
		try {
			nows = history.getLastTick(challenge.getInstrument()).getTime();
			if ( history.getOrdersHistory(challenge.getInstrument(), nows - dayss, nows).size() > 3 ) {
				double total = 0;
				for ( int i = 0; i < trades; i++ ) {
					IOrder order = history.getOrdersHistory(challenge.getInstrument(), nows - dayss, nows).get(i);
					total += order.getProfitLossInUSD(); 
				}
				if ( total < 0 ) {
					setExclusion();
					logger.fatal("Last 3 trade couldn't make profit!");
					StateMachine.getInstance().getState().setPanic(true);
				}
			}
				
		} catch (JFException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
