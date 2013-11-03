package hu.fnf.devel.forex.states;

import hu.fnf.devel.forex.StateMachine;
import hu.fnf.devel.forex.commands.CloseAllCommand;
import hu.fnf.devel.forex.commands.Command;
import hu.fnf.devel.forex.commands.OpenCommand;

import java.util.HashSet;
import java.util.Set;

import utils.Signal;

import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;

public abstract class State {

	protected String name;
	protected Signal signal;

	protected Set<Instrument> instruments;
	protected Set<Period> periods;
	protected Set<Command> commands;

	public abstract Set<State> nextStates();

	public abstract Signal signalStrength(Instrument instrument, ITick tick, State actual) throws JFException;

	public abstract boolean onArriving();

	public abstract boolean onLeaving();

	public void prepareCommands(Signal signal) {
		switch (signal.getTag()) {
		case StateMachine.OPEN:
			commands.add(new OpenCommand(signal));
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
}
