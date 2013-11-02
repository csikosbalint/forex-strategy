package hu.fnf.devel.forex.states;

import hu.fnf.devel.forex.Signal;
import hu.fnf.devel.forex.StateMachine;
import hu.fnf.devel.forex.commands.CloseAllCommand;
import hu.fnf.devel.forex.commands.Command;
import hu.fnf.devel.forex.commands.OpenCommand;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;

public abstract class State {
	protected static final Logger LOGGER = LoggerFactory.getLogger(State.class);

	protected String name;
	protected Signal signal;
	
	protected Set<Instrument> instruments;
	protected Set<Period> periods;
	protected Set<Command> commands;

	// TODO: Iterator pattern
	public abstract Set<State> nextStates();

	public abstract Signal signalStrength(Instrument instrument, ITick tick, State actual);

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
	
	public boolean executeCommands() {
		for ( Command c: commands ) {
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
		LOGGER.debug("new " + getName());
		
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
