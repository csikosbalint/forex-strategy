package hu.fnf.devel.forex.criteria;

import hu.fnf.devel.forex.StateMachine;
import hu.fnf.devel.forex.utils.Criterion;
import hu.fnf.devel.forex.utils.ExclusionDecorator;
import hu.fnf.devel.forex.utils.Signal;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;

public class MoneyManagement extends ExclusionDecorator {
	/*
	 * config
	 */
	private final double revard = 0.3;
	private final double risk = -0.1;

	/*
	 * variables
	 */
	double u_limit;
	double l_limit;

	public MoneyManagement(Criterion criterion) {
		super(criterion);
	}

	@Override
	protected void check(Signal challenge) {
		if (StateMachine.BRAVE_VALUE < 1.0) {
			u_limit = StateMachine.getInstance().getContext().getAccount().getBalance() * StateMachine.BRAVE_VALUE
					* revard;
			l_limit = StateMachine.getInstance().getContext().getAccount().getBalance() * StateMachine.BRAVE_VALUE
					* risk;
		}
		if (l_limit < StateMachine.getInstance().getStartBalance() * StateMachine.BRAVE_VALUE * risk * 1.25) {
			l_limit = StateMachine.getInstance().getStartBalance() * StateMachine.BRAVE_VALUE * risk * 1.25;
		}
		try {
			for (IOrder order : StateMachine.getInstance().getContext().getEngine().getOrders()) {
				double profit = order.getProfitLossInUSD();
				if (profit < l_limit || profit > u_limit) {
					logger.info("limit ($" + l_limit + "/" + u_limit + ") reached with " + order.getProfitLossInUSD()
							+ "$");
					setExclusion();
					break;
				}
			}
		} catch (JFException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
