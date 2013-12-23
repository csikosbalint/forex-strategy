package hu.fnf.devel.forex.commands;

import hu.fnf.devel.forex.StateMachine;
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
	private String label;

	public OpenCommand(Signal signal, String label) {
		this.signal = signal;
		this.label = label;
	}

	@Override
	public void execute() throws JFException {
		IOrder order = StateMachine.getInstance().getContext().getEngine()
				.submitOrder(StateMachine.getInstance().getNextState().getName(), signal.getInstrument(), signal.getType(), signal.getAmount());
		logger.info("Order " + order.getInstrument().name() + "/" + order.getOrderCommand().name()
				+ " has been submitted with amount " + order.getAmount());
		StateMachine.getInstance().pushPosition(order, signal.getPeriod());
		
		// TODO: bug, if I ask for .getState() I often get NullPointerEx printed but no real Ex
		// Workaround is to wait a sec
//		try {
//			logger.debug("sleeping..15s");
//			Thread.sleep(15000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		if (!order.getState().equals(IOrder.State.OPENED)) {
//		logger.info("oder state: " + order.getState());
//			throw new JFException(JFException.Error.ORDER_INCORRECT);
//		}

	}
}
