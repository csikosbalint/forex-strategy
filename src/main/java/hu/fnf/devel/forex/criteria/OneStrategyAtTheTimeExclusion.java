package hu.fnf.devel.forex.criteria;

import hu.fnf.devel.forex.StateMachine;
import hu.fnf.devel.forex.utils.Criterion;
import hu.fnf.devel.forex.utils.OpenExclusionDecorator;
import hu.fnf.devel.forex.utils.Signal;

import com.dukascopy.api.JFException;

public class OneStrategyAtTheTimeExclusion extends OpenExclusionDecorator {

	public OneStrategyAtTheTimeExclusion(Criterion criterion) {
		super(criterion);
	}

	@Override
	protected void check(Signal challenge) {
		// no trade if another strategy done an order
		try {
			if ( StateMachine.getInstance().getContext().getEngine().getOrders().size() > 0 ) { // TODO
					setExclusion();
				}
		} catch (JFException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
