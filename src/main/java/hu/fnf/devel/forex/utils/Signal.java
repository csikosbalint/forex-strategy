package hu.fnf.devel.forex.utils;

import hu.fnf.devel.forex.commands.Command;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.Period;

public class Signal {
	
	private double 		value;
	private OrderCommand type;
	private Instrument 	instrument;
	private double 		amount;
	private int 		tag;
	private Period		period;
	private Command		command;
	
	public void setCommand(Command command) {
		this.command = command;
	}
	
	public Command getCommand() {
		return command;
	}
	
	public Signal() {
		// TODO Auto-generated constructor stub
	}
	
	public void setPeriod(Period period) {
		this.period = period;
	}
	
	public Period getPeriod() {
		return period;
	}

	public Signal(Instrument instrument, double amount, int tag) {
		super();
		this.instrument = instrument;
		this.amount = amount;
		this.tag = tag;
	}

	public void setTag(int tag) {
		this.tag = tag;
	}

	public int getTag() {
		return tag;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
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

	void add(Signal b) {
		double aval = getValue();
		setValue(aval+b.getValue());
	}
} 
