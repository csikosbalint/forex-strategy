package utils;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.Instrument;

public class Signal {
	
	private int value;
	private OrderCommand type;
	private Instrument instrument;
	private double amount;
	private int tag;

	public void setTag(int tag) {
		this.tag = tag;
	}

	public int getTag() {
		return tag;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public OrderCommand getType() {
		return type;
	}

	public void setType(OrderCommand type) {
		this.type = type;
	}

	public Instrument getInstrument() {
		return instrument;
	}

	public void setInstrument(Instrument instrument) {
		this.instrument = instrument;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

}
