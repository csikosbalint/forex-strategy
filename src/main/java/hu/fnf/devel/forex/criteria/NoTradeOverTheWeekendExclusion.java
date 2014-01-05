package hu.fnf.devel.forex.criteria;

import hu.fnf.devel.forex.StateMachine;
import hu.fnf.devel.forex.utils.Criterion;
import hu.fnf.devel.forex.utils.OpenExclusionDecorator;
import hu.fnf.devel.forex.utils.Signal;

import com.dukascopy.api.IContext;
import com.dukascopy.api.IOrder;

public class NoTradeOverTheWeekendExclusion extends OpenExclusionDecorator {

	public NoTradeOverTheWeekendExclusion(Criterion criterion) {
		super(criterion);
	}

	@Override
	protected void check(Signal challenge) {
		IContext context = StateMachine.getInstance().getContext();
		try {
			if (context.getDataService().isOfflineTime(
					context.getHistory().getLastTick(challenge.getInstrument()).getTime())) {
				if (context.getEngine().getOrders().size() != 0) {
					logger.warn("It is not advides to have orders during weekend period.");
					for (IOrder o : context.getEngine().getOrders()) {
						logger.debug("#" + o.getId() + " - " + o.getInstrument() + " - $" + o.getProfitLossInUSD());
					}
				}
				setExclusion();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
