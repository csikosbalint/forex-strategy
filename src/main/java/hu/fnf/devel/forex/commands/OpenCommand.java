package hu.fnf.devel.forex.commands;

import hu.fnf.devel.forex.StateMachine;
import hu.fnf.devel.forex.database.Database;
import hu.fnf.devel.forex.utils.RobotException;
import hu.fnf.devel.forex.utils.Signal;

import org.apache.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;

public class OpenCommand implements Command {
	/**
	 * Resubmit order_ http://www.dukascopy.com/wiki/#Manage_Order_State
	 */
	private static final Logger logger = Logger.getLogger(OpenCommand.class);
	private Signal signal;

	public OpenCommand(Signal signal) {
		this.signal = signal;
	}

	@Override
	public void execute() throws JFException {
		IOrder order = StateMachine.getInstance().getContext().getEngine()
				.submitOrder(StateMachine.getNextState().getName(), signal.getInstrument(), signal.getType(), signal.getAmount());
		logger.info("Order " + order.getInstrument().name() + "/" + order.getOrderCommand().name()
				+ " has been submitted with amount " + order.getAmount());
		try {
			Database.add(order, signal.getPeriod());
		} catch (RobotException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		StateMachine.resubmitAttempts.put(order, 1);
	}
}
