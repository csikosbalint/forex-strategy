package hu.fnf.devel.forex.commands;

import org.apache.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;

import hu.fnf.devel.forex.StateMachine;

public class CloseAllCommand implements Command {
	private static final Logger logger = Logger.getLogger(CloseAllCommand.class);

	@Override
	public void execute() throws JFException {
		if (StateMachine.getInstance().getContext().getEngine().getOrders().size() != 0) {
			for (IOrder o : StateMachine.getInstance().getContext().getEngine().getOrders()) {
				if ( !o.getLabel().contains("START") ) {
					logger.info("Order #" + o.getId() + " closing with " + o.getProfitLossInUSD() + "$ / "
							+ o.getProfitLossInPips());
					o.close();
				}
			}
		}
	}

}