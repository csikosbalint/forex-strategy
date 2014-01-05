package hu.fnf.devel.forex.utils;

import hu.fnf.devel.forex.Main;
import hu.fnf.devel.forex.StateMachine;
import hu.fnf.devel.forex.commands.Command;
import hu.fnf.devel.forex.criteria.OneStrategyAtTheTimeExclusion;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;

public abstract class State {
	private static final Logger logger = Logger.getLogger(State.class);

	protected String name;
	protected Signal signal;

	protected Set<Instrument> instruments = new HashSet<Instrument>();
	protected Set<Period> periods = new HashSet<Period>();
	protected Criterion open;
	protected Criterion close;
	protected Set<Command> commands;
	private boolean panic;

	public abstract Signal getSignal(Instrument instrument, ITick tick, State actual) throws JFException;

	public abstract Signal getSignal(Instrument instrument, Period period, IBar askBar, IBar bidBar, State actual)
			throws JFException;
	
	public abstract double getAmount();

	public Set<State> getNextStates() {
		Set<State> nextstates = new HashSet<State>();
		String nexts = Main.getProperty("from." + getName());
		if ( nexts == null ) {
			/*
			 * not listed in config
			 */
			return nextstates;
		}
		for (String stateName : nexts.split(",")) {
			/*
			 * from.SignalSeeker = ScalpHolder7State,MACDSample452State,ThreeLittlePigsState,PanicState
			 */
			try {
				nextstates.add(StateMachine.getStateInstance(stateName));
			} catch (RobotException e) {
				e.printStackTrace();
			}
		}
		return nextstates;
	}
	
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
		logger.info(getName() + " onArriving");
		setPanic(false);
		this.commands = new HashSet<Command>();
		return true;
	}

	public boolean onLeaving() {
		logger.info(getName() + " onLeaving");
		// removing references
		this.signal = null;
		return true;
	}

	public State(String name) {
		this.name = name;
		
		instruments = new HashSet<Instrument>();
		periods = new HashSet<Period>();
		commands = new HashSet<Command>();
		
		open = new OpenCriterion();
		open = new OneStrategyAtTheTimeExclusion(open);
		
		close = new CloseCriterion();
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
