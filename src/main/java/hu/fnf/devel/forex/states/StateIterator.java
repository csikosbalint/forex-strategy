package hu.fnf.devel.forex.states;

public interface StateIterator {
	public boolean hasNext();

	public void next();

	public State currentItem();
}
