package hu.fnf.devel.forex.commands;

import com.dukascopy.api.JFException;

import hu.fnf.devel.forex.Signal;
import hu.fnf.devel.forex.StateMachine;

public class OpenCommand implements Command {
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
