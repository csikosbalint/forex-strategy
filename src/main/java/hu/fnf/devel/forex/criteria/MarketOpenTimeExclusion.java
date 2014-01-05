package hu.fnf.devel.forex.criteria;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import hu.fnf.devel.forex.Main;
import hu.fnf.devel.forex.utils.Criterion;
import hu.fnf.devel.forex.utils.OpenExclusionDecorator;
import hu.fnf.devel.forex.utils.Signal;

public class MarketOpenTimeExclusion extends OpenExclusionDecorator {
	String lastd;
	String lasti;
	/*
	 * config
	 */
	private final Map<String, String> instStockMap = new HashMap<String, String>();
	{
		instStockMap.put("EUR", "London");
		instStockMap.put("GBP", "London");
		instStockMap.put("USD", "New York");
		instStockMap.put("JPY", "Tokyo");
	}

	public MarketOpenTimeExclusion(Criterion criterions) {
		super(criterions);
	}

	@Override
	protected void check(Signal challenge) {
		// no trade out of market session time
		StringBuilder closedMarkets = new StringBuilder();
		ArrayList<String> markets = new ArrayList<String>();
		markets.add(instStockMap.get(challenge.getInstrument().getPrimaryCurrency().getCurrencyCode()));
		markets.add(instStockMap.get(challenge.getInstrument().getSecondaryCurrency().getCurrencyCode()));
		for (String market : markets) {
			if (!Main.isMarketOpen(market)) {
				closedMarkets.append(market + ",");
			}
		}

		if (closedMarkets.toString().split(",").length > markets.size() - 1) {
			massInfo("Market closed.");
			massDebug(closedMarkets.toString() + " market(s) is/are closed for " +challenge.getInstrument()+ ".");
			setExclusion();
		}
	}

	private void massInfo(String string) {
		if ( lasti != null && !lasti.equalsIgnoreCase(string) ) {
			logger.info(string);
			lasti = string;
		}
	}

	private void massDebug(String string) {
		if ( lastd != null && !lastd.equalsIgnoreCase(string) ) {
			logger.debug(string);
			lastd = string;
		}
	}
}
