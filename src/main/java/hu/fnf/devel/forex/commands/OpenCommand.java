package hu.fnf.devel.forex.commands;

import org.apache.log4j.Logger;

import com.dukascopy.api.JFException;

import hu.fnf.devel.forex.Signal;
import hu.fnf.devel.forex.StateMachine;

public class OpenCommand implements Command {
	private static final Logger LOGGER = Logger.getLogger(OpenCommand.class);
	Signal signal;

	public OpenCommand(Signal signal) {
		this.signal = signal;
	}

	@Override
	public void execute() throws JFException {
		StateMachine.getInstance().getContext().getEngine()
				.submitOrder("uuid", signal.getInstrument(), signal.getType(), signal.getAmount());
	}
}
