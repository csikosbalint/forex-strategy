package hu.fnf.devel.forex.commands;

import org.apache.log4j.Logger;

import hu.fnf.devel.forex.StateMachine;

import com.dukascopy.api.JFException;

public class CloseAllCommand implements Command {
	private static final Logger LOGGER = Logger.getLogger(CloseAllCommand.class);

	@Override
	public void execute() throws JFException {
		StateMachine.getInstance().getContext().getEngine().closeOrders(StateMachine.getInstance().getOrders());
	}

}
