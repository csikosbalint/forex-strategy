package hu.fnf.devel.forex;

import com.dukascopy.api.IEngine.OrderCommand;

public class Signal {
	private int value;
	private OrderCommand type;

	public Signal() {
		value = 0;
		type = null;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int signal) {
		this.value = signal;
	}

	public OrderCommand getType() {
		return type;
	}

	public void setType(OrderCommand type) {
		this.type = type;
	}

}
