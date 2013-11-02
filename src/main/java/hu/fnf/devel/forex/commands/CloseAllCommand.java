package hu.fnf.devel.forex.commands;

import hu.fnf.devel.forex.StateMachine;

import com.dukascopy.api.JFException;

public class CloseAllCommand implements Command {

	@Override
	public void execute() throws JFException {
		StateMachine.getInstance().getContext().getEngine().closeOrders(StateMachine.getInstance().getOrders());
	}

}
