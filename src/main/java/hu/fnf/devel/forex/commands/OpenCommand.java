package hu.fnf.devel.forex.commands;

import org.apache.log4j.Logger;

import utils.Signal;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;

import hu.fnf.devel.forex.StateMachine;

public class OpenCommand implements Command {
	private static final Logger logger = Logger.getLogger(OpenCommand.class);
	Signal signal;

	public OpenCommand(Signal signal) {
		this.signal = signal;
	}

	@Override
	public void execute() throws JFException {
		IOrder order = StateMachine.getInstance().getContext().getEngine()
				.submitOrder("uuid", signal.getInstrument(), signal.getType(), signal.getAmount());
		logger.info("Order #" + order.getId() + " has been submitted.");
		logger.debug("Current state for #" + order.getId() + " is " + order.getState().name());
	}
}
