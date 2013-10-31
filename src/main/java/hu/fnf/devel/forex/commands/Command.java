package hu.fnf.devel.forex.commands;

import com.dukascopy.api.JFException;

public interface Command {
	public void execute() throws JFException;
}
