package hu.fnf.devel.forex.criteria;

import java.util.List;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.ITick;
import com.dukascopy.api.JFException;

import hu.fnf.devel.forex.StateMachine;
import hu.fnf.devel.forex.utils.CloseCriterionDecorator;
import hu.fnf.devel.forex.utils.Criterion;
import hu.fnf.devel.forex.utils.Signal;
import hu.fnf.devel.forex.utils.State;

public class ThreePigsClose extends CloseCriterionDecorator {

	public ThreePigsClose(Criterion criterion) {
		super(criterion);	
	}
	
	@Override
	protected double calc(Signal challenge, ITick tick, State actual) {
		List<IOrder> orders = null;
		try {
			orders = StateMachine.getInstance().getContext().getEngine().getOrders();
		} catch (JFException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if ( orders != null && orders.get(0).getProfitLossInPips() > 16 ) {
			return this.max;
		}
		return 0;
	}

}
