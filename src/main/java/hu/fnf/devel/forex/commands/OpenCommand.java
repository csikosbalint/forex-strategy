package hu.fnf.devel.forex.commands;

import org.apache.log4j.Logger;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;

import hu.fnf.devel.forex.Main;
import hu.fnf.devel.forex.StateMachine;
import hu.fnf.devel.forex.utils.Signal;

public class OpenCommand implements Command {
	/**
	 * Resubmit order_ http://www.dukascopy.com/wiki/#Manage_Order_State
	 */
	private static final Logger logger = Logger.getLogger(OpenCommand.class);
	private Signal signal;
	private String label;

	public OpenCommand(Signal signal, String label) {
		this.signal = signal;
		this.label = label;
	}

	@Override
	public void execute() throws JFException {
		IOrder order = StateMachine.getInstance().getContext().getEngine()
				.submitOrder(label, signal.getInstrument(), signal.getType(), signal.getAmount());
		logger.info("Order " + order.getInstrument().name() + "/" + order.getOrderCommand().name()
				+ " has been submitted with amount " + order.getAmount());
		if (!StateMachine.getInstance().getContext().getEngine().getType().equals(IEngine.Type.TEST)) {
			while (!order.getState().equals(IOrder.State.FILLED)) {
				logger.debug("state: " + order.getState().toString() + " ... waiting.");
			}
		}
		logger.info("Order #" + order.getId() + " filled!");
		Main.setLastOrder(order);
	}
}
