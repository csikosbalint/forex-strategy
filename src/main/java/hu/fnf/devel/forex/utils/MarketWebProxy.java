package hu.fnf.devel.forex.utils;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MarketWebProxy implements Info {
	private static final Logger logger = Logger.getLogger(MarketWebProxy.class);
	private final String marketOpenTimeUri = "http://forex.timezoneconverter.com";
	private Elements market_open_row;

	public MarketWebProxy() throws IOException {
		Document doc = Jsoup.connect(marketOpenTimeUri).get();
		logger.debug("Downloading market open info from " + marketOpenTimeUri + " ..");
		market_open_row = doc.getElementsByClass("market_open_row");
		logger.debug("Download completed. Info is now fresh!");
	}

	@Override
	public boolean isMarketOpen(String market) {
		for (Element e : market_open_row) {
			if (e.text().contains(market)) {
				return true;
			}
		}
		return false;
	}

}
