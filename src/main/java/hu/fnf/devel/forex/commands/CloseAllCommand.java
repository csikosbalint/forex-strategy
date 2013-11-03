package hu.fnf.devel.forex.commands;

import org.apache.log4j.Logger;

import com.dukascopy.api.JFException;

import hu.fnf.devel.forex.StateMachine;

public class CloseAllCommand implements Command {
	private static final Logger logger = Logger.getLogger(CloseAllCommand.class);

	@Override
	public void execute() throws JFException {
		logger.info("closing all("+ StateMachine.getInstance().getOrders().size()+") orders");
		StateMachine.getInstance().getContext().getEngine().closeOrders(StateMachine.getInstance().getOrders());
	}

}
