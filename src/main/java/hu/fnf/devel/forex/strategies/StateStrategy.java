package hu.fnf.devel.forex.strategies;

import java.util.HashSet;
import java.util.Set;

//import org.apache.lucene.index.FieldInfo.IndexOptions;

import com.dukascopy.api.*;

public class StateStrategy implements IStrategy {

    private IContext context = null;
    private IEngine engine = null;
    private IChart chart = null;
    private IHistory history = null;
    private IIndicators indicators = null;
    private IConsole console = null;
    private double bidPrice = 0;
    private double askPrice = 0;
    private double volume = 5.5;
    private int profitLimit = 10;
    private int lossLimit = 250;
    private double accountEquity = 0;

    @Override
    public void onStart(IContext context) throws JFException {
        this.context = context;
        engine = context.getEngine();
        indicators = context.getIndicators();
        history = context.getHistory();
        console = context.getConsole();
        indicators = context.getIndicators();
        Set<Instrument> subscribedInstruments = new HashSet<Instrument>();
        subscribedInstruments.add(Instrument.GBPJPY);
        subscribedInstruments.add(Instrument.USDJPY);

        context.setSubscribedInstruments(subscribedInstruments);
    }

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {
        // TODO Auto-generated method stub

    }

    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        
        if ( period == Period.FIVE_MINS ) {
            double red[] = context.getIndicators().bbands(instrument, period, 
                    OfferSide.BID, IIndicators.AppliedPrice.CLOSE, 50    , 1, 1, IIndicators.MaType.SMA, 0);
            double orange[] = context.getIndicators().bbands(instrument, period, 
                    OfferSide.BID, IIndicators.AppliedPrice.CLOSE, 50    , 1.5, 1.5, IIndicators.MaType.SMA, 0);
            double yellow[] = context.getIndicators().bbands(instrument, period, 
                    OfferSide.BID, IIndicators.AppliedPrice.CLOSE, 50    , 2, 2, IIndicators.MaType.SMA, 0);
            if ( totalOrders() != 0 ) {
                // close ?
                engine.closeOrders(engine.getOrders());
            } else {
                sell(instrument, engine, profitLimit, lossLimit, volume);
                // open  ?
                // sell ?
//                if ( bidBar.getClose() > orange[0] ) {
//                    sell(instrument, engine, profitLimit, lossLimit, volume);
//                } else if ( askBar.getOpen() < orange[1] ) {
//                    buy(instrument, engine, profitLimit, lossLimit, volume);
//                }
            }
        }
    }

    private void buy(Instrument instrument, IEngine engine2, int profitLimit2, int lossLimit2, double volume2) {
        try {
            engine.submitOrder("buy", instrument, IEngine.OrderCommand.BUY, volume2, 0, 3, askPrice
                    - instrument.getPipValue() * lossLimit2, askPrice + instrument.getPipValue() * profitLimit2);
        } catch (JFException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void sell(Instrument instrument, IEngine engine2, int profitLimit2, int lossLimit2, double volume2) {
        try {
            engine.submitOrder("sell", instrument, IEngine.OrderCommand.SELL, volume2, 0, 3, bidPrice
                    + instrument.getPipValue() *lossLimit2, bidPrice - instrument.getPipValue() * profitLimit2);
        } catch (JFException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private int totalOrders() {
        int retval = 0;
        try {
            for ( IOrder o : engine.getOrders() ) {
                if ( o.getState() == IOrder.State.OPENED ) {
                    retval++;
                }
            }
        } catch (JFException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0;
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
