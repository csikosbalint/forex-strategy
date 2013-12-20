package hu.fnf.devel.forex.criteria;

import com.dukascopy.api.IBar;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;

import hu.fnf.devel.forex.StateMachine;
import hu.fnf.devel.forex.utils.Criterion;
import hu.fnf.devel.forex.utils.OpenCriterionDecorator;
import hu.fnf.devel.forex.utils.Signal;
import hu.fnf.devel.forex.utils.State;

public class WaitAfterClose extends OpenCriterionDecorator {

	public WaitAfterClose(Criterion criterion) {
		super(criterion);
	}

	@Override
	public double calcProbability(Signal challenge, ITick tick, State actual) {
		if (isExcluded()) {
			return 0;
		}
		check(challenge.getInstrument());
		return super.calcProbability(challenge, tick, actual);
	}

	@Override
	public double calcProbability(Signal challenge, Period period, IBar askBar, IBar bidBar, State actual) {
		if (isExcluded()) {
			return 0;
		}
		check(challenge.getInstrument());
		return super.calcProbability(challenge, period, askBar, bidBar, actual);
	}

	private void check(Instrument instrument) {
		IHistory history = StateMachine.getInstance().getContext().getHistory();
		try {
			long closetime = StateMachine.getInstance().getContext().getEngine().getOrders()
					.get(StateMachine.getInstance().getContext().getEngine().getOrders().size() - 1).getCloseTime();
			if (history.getLastTick(instrument).getTime() - closetime < 6000000) {
				logger.debug("closetime is ok: "
						+ String.valueOf(history.getLastTick(instrument).getTime() - closetime));
				setExclusion();
			}
		} catch (JFException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
