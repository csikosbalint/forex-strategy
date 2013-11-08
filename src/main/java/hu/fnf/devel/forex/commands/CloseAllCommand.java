package hu.fnf.devel.forex.commands;

import org.apache.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;

import hu.fnf.devel.forex.StateMachine;

public class CloseAllCommand implements Command {
	private static final Logger logger = Logger.getLogger(CloseAllCommand.class);

	@Override
	public void execute() throws JFException {
		if (StateMachine.getInstance().getOrders().size() != 0) {
			logger.info("closing all(" + StateMachine.getInstance().getOrders().size() + ") orders");

			for (IOrder o : StateMachine.getInstance().getOrders()) {
				if (o.getState() == IOrder.State.FILLED ) {
				logger.info("profit for #" + o.getId() + " is " + o.getProfitLossInUSD() + "$");
				} else {
					logger.error("There are orders which are not FILLED!");
					return;
				}
			}
			StateMachine.getInstance().getContext().getEngine().closeOrders(StateMachine.getInstance().getOrders());
		}
	}

}
