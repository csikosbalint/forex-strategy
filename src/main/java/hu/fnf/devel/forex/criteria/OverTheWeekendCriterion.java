package hu.fnf.devel.forex.criteria;

import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.Period;

import hu.fnf.devel.forex.Main;
import hu.fnf.devel.forex.StateMachine;
import hu.fnf.devel.forex.utils.Criterion;
import hu.fnf.devel.forex.utils.OpenCriterionDecorator;
import hu.fnf.devel.forex.utils.Signal;
import hu.fnf.devel.forex.utils.State;

public class OverTheWeekendCriterion extends OpenCriterionDecorator {

	public OverTheWeekendCriterion(Criterion criterion) {
		super(criterion);
	}

	@Override
	public double calcProbability(Signal challenge, ITick tick, State actual) {
		double ret = super.calcProbability(challenge, tick, actual);
		if ( isExcluded() ) {
			return 0;
		}
		check(challenge.getInstrument());
		return ret;
	}

	@Override
	public double calcProbability(Signal challenge, Period period, IBar askBar, IBar bidBar, State actual) {
		double ret = super.calcProbability(challenge, period, askBar, bidBar, actual);
		if ( isExcluded() ) {
			return 0;
		}
		check(challenge.getInstrument());
		return ret;
	}
	
	private void check(Instrument instrument) {
		IContext context = StateMachine.getInstance().getContext();

		// no trade during weekend time
		try {
			if (context.getDataService().isOfflineTime(context.getHistory().getLastTick(instrument).getTime())) {
				Main.massDebug(logger, "Over-weekend trade is not supported!");
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
