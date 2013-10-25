package hu.fnf.devel.forex;

import hu.fnf.devel.forex.states.State;
import hu.fnf.devel.forex.states.SignalSeekerState;
import hu.fnf.devel.forex.strategies.Strategy;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;

public class StateStrategy extends State implements IStrategy{
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	private static StateStrategy instance = null;

	private hu.fnf.devel.forex.states.State state;	
	private Set<Strategy> strategies ;

	private StateStrategy(Set<Strategy> strategies) {
		this.strategies = strategies;
	}
	
	public static synchronized StateStrategy getInstance(Set<Strategy> strategies) {
		if ( instance == null ) {
			instance = new StateStrategy(strategies);
		}
		return instance;
	}

	public void setState(hu.fnf.devel.forex.states.State state) {
		if ( this.state != state ) {
			this.state = state;
		}
	}
	
	public hu.fnf.devel.forex.states.State getState() {
		return state;
	}

	private State recignizeState(Set<Strategy> strategies) {
		// TODO: recognize states
		/*
		 * For now every start assumed a new beginning
		 */
		/*
		 * return new ScalpHolderState(new ScalpingStrategy());
		 */
		return new SignalSeekerState(strategies, this);
	}

	@Override
	public void onStart(IContext context) throws JFException {
		LOGGER.info("onStart");
		this.state = recignizeState(strategies);

		Set<Instrument> instruments = new HashSet<Instrument>();
		for ( Strategy s: strategies ) {
			for ( Instrument i: s.getInstruments() ) {
				instruments.add(i);
			}
		}
		context.setSubscribedInstruments(instruments);
	}
	
	/*
	 * Action
	 */
	
	
	@Override
	public void onTick(Instrument instrument, ITick tick) throws JFException {
		//this.state.transaction(instrument, tick);
	}

	@Override
	public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
		this.state.transaction(instrument, period, askBar, bidBar);
	}

	@Override
	public void transaction(Instrument instrument, ITick tick) {
		setState(new SignalSeekerState(strategies, this));
	}

	@Override
	public void transaction(Instrument instrument, Period period, IBar askBar, IBar bidBar) {
		setState(new SignalSeekerState(strategies, this));
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@Override
	public void onMessage(IMessage message) throws JFException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAccount(IAccount account) throws JFException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStop() throws JFException {
		// TODO Auto-generated method stub

	}



}
