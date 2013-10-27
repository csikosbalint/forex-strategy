package hu.fnf.devel.forex;

import com.dukascopy.api.IEngine.OrderCommand;

public class Signal {
	private int signal;
	private OrderCommand type;

	public Signal() {
		signal = 0;
		type = null;
	}

	public int getSignal() {
		return signal;
	}

	public void setSignal(int signal) {
		this.signal = signal;
	}

	public OrderCommand getType() {
		return type;
	}

	public void setType(OrderCommand type) {
		this.type = type;
	}

}
