package hu.fnf.devel.forex.commands;

import org.apache.log4j.Logger;

import hu.fnf.devel.forex.StateMachine;
import hu.fnf.devel.forex.utils.State;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;

public class CloseCommand implements Command {
	private static final Logger logger = Logger.getLogger(CloseAllCommand.class);
	private State withState;
	private IOrder iOrder;

	public CloseCommand(State withState) {
		super();
		this.withState = withState;
	}

	public CloseCommand(IOrder iOrder) {
		super();
		this.iOrder = iOrder;
	}

	@Override
	public void execute() throws JFException {
		if ( iOrder != null ) {
			logger.info("Order #" + iOrder.getId() + " closing with " + iOrder.getProfitLossInUSD() + "$ / "
					+ iOrder.getProfitLossInPips());
			iOrder.close();
		} else if ( withState != null ) {
			for (IOrder iOrder : StateMachine.getInstance().getContext().getEngine().getOrders()) {
				if ( iOrder.getLabel().split("AND")[0].equalsIgnoreCase(withState.getName()) ) {
					logger.info("Order #" + iOrder.getId() + " closing with " + iOrder.getProfitLossInUSD() + "$ / "
							+ iOrder.getProfitLossInPips());
					iOrder.close();
				}
			}
		}
	}

}
