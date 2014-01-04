package hu.fnf.devel.forex.utils;

import hu.fnf.devel.forex.commands.Command;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;

public abstract class State {
	protected static final Logger logger = Logger.getLogger(State.class);

	protected String name;
	protected Signal signal;

	protected Set<Instrument> instruments;
	protected Set<Period> periods;
	protected Criterion open;
	protected Criterion close;
	protected Set<Command> commands;
	private boolean panic;

	public abstract Signal getSignal(Instrument instrument, ITick tick, State actual) throws JFException;

	public abstract Signal getSignal(Instrument instrument, Period period, IBar askBar, IBar bidBar, State actual)
			throws JFException;

	public abstract Set<State> getNextStates();

	public abstract double getAmount();

	public void prepareCommands(Signal signal) {
		commands.add(signal.getCommand());
		logger.info("Command " + signal.getCommand().getClass().getSimpleName() + " is prepared.");
	}

	public boolean executeCommands() {
		for (Command c : commands) {
			try {
				logger.info("Command " + c.getClass().getSimpleName() + " is executing...");
				c.execute();
			} catch (JFException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			return true;
		}
		return true;
	}

	public boolean onArriving() {
		setPanic(false);
		this.commands = new HashSet<Command>();
		this.instruments = new HashSet<Instrument>();
		this.periods = new HashSet<Period>();
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
	
	public boolean isPanic() {
		return panic;
	}
	
	public void setPanic(boolean panic) {
		this.panic = panic;
	}
}
