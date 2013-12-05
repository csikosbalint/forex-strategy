package hu.fnf.devel.forex.utils;

import hu.fnf.devel.forex.StateMachine;
import hu.fnf.devel.forex.commands.CloseAllCommand;
import hu.fnf.devel.forex.commands.Command;
import hu.fnf.devel.forex.commands.OpenCommand;

import java.util.HashSet;
import java.util.Set;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;

public abstract class State {

	protected String name;
	protected Signal signal;

	protected Set<Instrument> instruments;
	protected Set<Period> periods;
	protected Criterion open;
	protected Criterion close;
	protected Set<Command> commands;

	public abstract Signal getSignal(Instrument instrument, ITick tick, State actual) throws JFException;

	public abstract Signal getSignal(Instrument instrument, Period period, IBar askBar, IBar bidBar, State actual)
			throws JFException;

	public abstract Set<State> getNextStates();

	public void prepareCommands(Signal signal) {
		switch (signal.getTag()) {
		case StateMachine.OPEN:
			commands.add(new OpenCommand(signal, getName()));
			break;
		case StateMachine.CLOSE:
			commands.add(new CloseAllCommand());
		default:
			break;
		}
	}

	public boolean executeCommands() throws JFException {
		for (Command c : commands) {
			try {
				c.execute();
			} catch (JFException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	public boolean onArriving() {
		return true;
	}

	public boolean onLeaving() {
		// removing references
		this.commands = null;
		this.instruments = null;
		this.periods = null;
		this.signal = null;
		return true;
	}

	public State(String name) {
		this.name = name;
		instruments = new HashSet<Instrument>();
		periods = new HashSet<Period>();
		commands = new HashSet<Command>();
	}

	public String getName() {
		return name;
	}

	public Set<Period> getPeriods() {
		return periods;
	}

	public Set<Instrument> getInstruments() {
		return instruments;
	}

	public Criterion getClose() {
		return close;
	}

	public Criterion getOpen() {
		return open;
	}
}