package hu.fnf.devel.forex;

import com.dukascopy.api.IEngine.OrderCommand;

public class Signal {
	private int strength;
	private OrderCommand type;

	public Signal() {
		strength = 0;
		type = null;
	}

	public int getStrength() {
		return strength;
	}

	public void setStrength(int signal) {
		this.strength = signal;
	}

	public OrderCommand getType() {
		return type;
	}

	public void setType(OrderCommand type) {
		this.type = type;
	}

}
