package hu.fnf.devel.forex;

import com.dukascopy.api.*;
import com.dukascopy.api.feed.FeedDescriptor;
import com.dukascopy.api.feed.IFeedDescriptor;
import com.dukascopy.api.system.*;
import com.dukascopy.api.system.ITesterClient.DataLoadingMethod;
import com.dukascopy.api.system.tester.ITesterExecution;
import com.dukascopy.api.system.tester.ITesterExecutionControl;
import com.dukascopy.api.system.tester.ITesterGui;
import com.dukascopy.api.system.tester.ITesterUserInterface;
import hu.fnf.devel.forex.utils.Info;
import hu.fnf.devel.forex.utils.RobotException;
import hu.fnf.devel.forex.utils.WebInfo;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.Future;

/*
@author: johnnym
 */
public class Main {
        private static final String JARFILE_PATH = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        private static final String JARFILE =
                JARFILE_PATH.split( System.getProperty( "file.separator" ) )[JARFILE_PATH.split( System.getProperty( "file.separator" ) ).length - 1];
        public static final String VERSION = JARFILE.replaceAll( "[a-z-]*", "" ).trim();
        /**
         * global variables
         */
        private static final Logger logger = Logger.getLogger( Main.class );
        private static Properties prop = new Properties();
        private static IClient client;
        private static long processId;
        private static Info info;

        private static String phase;

        public static String getPhase() {
                return phase + " ";
        }

        public static void setPhase( String phase ) {
                if ( Main.phase != null ) {
                        logger.info( "--- Stopping " + getPhase() + "phase ---" );
                }
                Main.phase = phase;
                if ( Main.phase != null ) {
                        logger.info( "----------------------------------------" );
                        logger.info( "--- Starting " + getPhase() + "phase ---" );
                }
        }

        public static String getProperty( String key ) {
                if ( key.contains( "assword" ) ) {
                        return null;
                }
                return prop.getProperty( key );
        }

        public static void main( String[] args ) throws Exception {
        /*
         * SIGTERM signal catch
		 */
                Runtime.getRuntime().addShutdownHook( new Thread( new Runnable() {
                                                              public void run() {
                                                                      Main.setPhase( "Interrupted" );
                                                                      if ( client != null && processId != 0 ) {
                                                                              client.stopStrategy( processId );
                                                                      }
                                                                      logger.info( "------------------------- Done. ----------------------------" );
                                                              }
                                                      } )
                );
        /*
         * config parse
		 */
                if ( args.length > 0 ) {
                        try {
                                prop.load( new FileInputStream( args[0] ) );
                                PropertyConfigurator.configure( args[0] );
                        } catch ( IOException ex ) {
                                ex.printStackTrace();
                        }
                } else {
                        System.out.println( "Config parameter is necessary!" );
                        System.exit( -1 );
                }
                logger.info( "-------- Forex robot v" + VERSION + " written by johnnym --------" );

                setPhase( "Configuration" );
                logger.info( "Using config file: " + args[0] );
                for ( Object key : prop.keySet() ) {
                        if ( key.toString().contains( "assword" ) ) {
                                continue;
                        }
                        logger.debug( "\t" + key.toString() + "\t=\t" + prop.get( key ) );
                }

                logger.info( "Account: " + prop.getProperty( "account.user" ) );
                setPhase( "Initalization" );
        /*
         * init web data cache
		 */
                info = new WebInfo();
        /*
         * LIVE, DEMO, TEST mode
		 */
                if ( prop.getProperty( "test.mode" ).equalsIgnoreCase( "true" ) ) {
            /*
             * tester instance
			 */
                        try {
                                client = TesterFactory.getDefaultInstance();
                        } catch ( Exception e ) {
                                e.printStackTrace();
                        }
            /*
             * subscribe
			 */
                        StateMachine.getInstance().subscribe( client );
            /*
             * connect client
			 */
                        connectClient();
            /*
             * tester initialization
			 */
                        ((ITesterClient)client).setInitialDeposit( Instrument.EURUSD.getSecondaryCurrency(), 500 );
                        logger.info( "ITesterClient client has been initalized with deposit " + 500 + " USD" );
                        final SimpleDateFormat dateFormat = new SimpleDateFormat( "MM/dd/yyyy HH:mm:ss" );
                        dateFormat.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
                        try {
                                Date dateFrom = dateFormat.parse( prop.getProperty( "test.from" ) );
                                Date dateTo = dateFormat.parse( prop.getProperty( "test.till" ) );

                                ((ITesterClient)client).setDataInterval( DataLoadingMethod.TICKS_WITH_TIME_INTERVAL, dateFrom.getTime(),
                                                                         dateTo.getTime() );
                                // client.setDataInterval(Period.FIFTEEN_MINS, OfferSide.BID,
                                // InterpolationMethod.CLOSE_TICK, dateFrom.getTime(),
                                // dateTo.getTime());
                                // load data
                                logger.info( "Downloading data" );
                                Future<?> future = ((ITesterClient)client).downloadData( null );
                                // wait for downloading to complete
                                future.get();
                        } catch ( Exception e ) {
                                logger.fatal( "Cannot retreive data for testing!" );
                                logger.fatal( "Config error: test.from and test.till must be configure for testing in the format of MM/dd/yyyy HH:mm:ss" );
                                closing();
                                throw new Exception( "Config error: test.from and test.till must be configure for testing in the format of MM/dd/yyyy HH:mm:ss", e );
                        }
                } else {
            /*
             * live/demo
			 */
                        try {
                                client = ClientFactory.getDefaultInstance();
                        } catch ( Exception e ) {
                                logger.fatal( "Cannot instanciate client!", e );
                                return;
                        }
                }
                client.setSystemListener( new ISystemListener() {

                        public void onStop( long arg0 ) {
                                logger.info( "Client(" + arg0 + ") has been stopped." );
                                setPhase( null );
                                System.exit( 0 );
                        }

                        public void onStart( long arg0 ) {
                                logger.info( "Client(" + arg0 + ") has been started." );
                                processId = arg0;
                        }

                        public void onDisconnect() {
                                logger.info( "Client has been disconnected.Trying to reconnect..." );
                                client.reconnect();
                        }

                        public void onConnect() {
                                logger.info( "Client has been connected..." );
                        }
                } );

                if ( prop.getProperty( "account.gui" ).equalsIgnoreCase( "true" ) ) {
                        final TesterMainGUI gui = new TesterMainGUI();
                        StateMachine.getInstance().setGui( gui );
                        try {
                /*
                 * GUI will start strategy
				 */
                                client.setSystemListener( new ISystemListener() {

                                        @Override
                                        public void onStart( long processId ) {
                                                logger.info( "Strategy started: " + processId );
                                                gui.updateButtons();
                                        }

                                        @Override
                                        public void onStop( long processId ) {
                                                logger.info( "Strategy stopped: " + processId );
                                                gui.resetButtons();
                                                if ( client instanceof ITesterClient ) {
                                                        File reportFile = new File( "/tmp/report.html" );
                                                        try {
                                                                ((ITesterClient)client).createReport( processId, reportFile );
                                                        } catch ( Exception e ) {
                                                                logger.error( e.getMessage(), e );
                                                        }
                                                }

                                                if ( client.getStartedStrategies().size() == 0 ) {
                                                        // Do nothing
                                                }
                                        }

                                        @Override
                                        public void onConnect() {
                                                logger.info( "Connected" );
                                        }

                                        @Override
                                        public void onDisconnect() {
                                                // tester doesn't disconnect
                                        }
                                } );
                                gui.showChartFrame();
                        } catch ( Exception e ) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                        }
                        if ( client instanceof ITesterClient ) {
                                ((ITesterClient)client).startStrategy( StateMachine.getInstance(), new LoadingProgressListener() {
                                        @Override
                                        public void dataLoaded( long startTime, long endTime, long currentTime, String information ) {
                                        }

                                        @Override
                                        public void loadingFinished( boolean allDataLoaded, long startTime, long endTime, long currentTime ) {
                                        }

                                        @Override
                                        public boolean stopJob() {
                                                return false;
                                        }
                                }, gui, gui );
                        } else {
                                startStrategy();
                        }
                } else {
                        connectClient();
                        try {
                                startStrategy();
                        } catch ( Exception e ) {
                                closing();
                                throw new Exception( e );
                        }
                }
        }

        public static void connectClient() throws Exception {
                setPhase( "Connection" );
                client.connect( prop.getProperty( "network.jnpl" ), prop.getProperty( "account.user" ),
                                prop.getProperty( "account.password" ) );

                // wait for it to connect
                int i = 50; // wait max ten seconds
                while ( i > 0 && !client.isConnected() ) {
                        logger.debug( "waiting for connection ..." );
                        try {
                                Thread.sleep( 1000 );
                        } catch ( InterruptedException e ) {
                                logger.fatal( "Connection process has been aborted!", e );
                                closing();
                                return;
                        }
                        i--;
                }
                logger.info( "Number of candles loaded." );
        }

        public static void startStrategy() {
                setPhase( "Strategy starting" );

                client.startStrategy( StateMachine.getInstance(), new IStrategyExceptionHandler() {

                        @Override
                        public void onException( long strategyId, Source source, Throwable t ) {
                                // throw t
                                closing();
                        }

                } );

                setPhase( "Running" );
        }

        private static void closing() {
                setPhase( "Closing" );
                if ( client != null && client.isConnected() ) {
                        client.disconnect(); // listener onStop called (?)
                }
        }

        public static boolean isMarketOpen( String market ) {
                return info.isMarketOpen( market );
        }

        public static void sendMail( String subject, String body ) throws RobotException {
                if ( !StateMachine.getInstance().getContext().getEngine().getType().equals( IEngine.Type.TEST ) ) {
                        String to = prop.getProperty( "account.email" );
                        String from = "fxrobot@fnf.hu";
                        String host = "mail.fnf.hu";
                        Properties properties = System.getProperties();
                        properties.setProperty( "mail.smtp.host", host );

                        Session session = Session.getDefaultInstance( properties );

                        try {
                                MimeMessage message = new MimeMessage( session );
                                message.setFrom( new InternetAddress( from ) );
                                message.addRecipient( Message.RecipientType.TO, new InternetAddress( to ) );
                                message.setSubject( subject );
                                message.setText( body );
                                Transport.send( message );
                        } catch ( Exception err ) {
                                throw new RobotException( "Cannot send mail!", err );
                        }
                }
                logger.info( "Mail \"" + subject + "\" has been sent to " + prop.getProperty( "account.email" ) );
        }

        public static void printDetails( IOrder iOrder ) {
                logger.info( "IOrder \"" + iOrder.getLabel() + "\" #" + iOrder.getId() );
                logger.debug( "\tid:            " + iOrder.getId() );
                logger.debug( "\tctime:         " + iOrder.getCreationTime() );
                logger.debug( "\tcurrency:      " + iOrder.getInstrument() );
                logger.debug( "\tlabel:       " + iOrder.getLabel() );
                logger.debug( "\tprofit/loss:   " + iOrder.getProfitLossInUSD() );
        }

}

@SuppressWarnings ( "serial" ) class TesterMainGUI extends JFrame implements ITesterUserInterface, ITesterExecution {
        private final Logger logger = Logger.getLogger( TesterMainGUI.class );

        private final int frameWidth = 1000;
        private final int frameHeight = 600;
        private final int controlPanelHeight = 40;

        private ITesterExecutionControl executionControl = null;

        private JPanel controlPanel = null;
        private JButton startStrategyButton = null;
        private JButton pauseButton = null;
        private JButton continueButton = null;
        private JButton cancelButton = null;

        public TesterMainGUI() {
                setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
                getContentPane().setLayout( new BoxLayout( getContentPane(), BoxLayout.Y_AXIS ) );
        }

        @Override
        public void setChartPanels( Map<IChart, ITesterGui> chartPanels ) {
                if ( chartPanels != null && chartPanels.size() > 0 ) {
                        for ( IChart chart : chartPanels.keySet() ) {

                                StateMachine.getInstance().addChart( chart );
                                logger.debug( "Chart for instrument " + chart.getInstrument().toString() );

                                // show ticks for EURUSD and 10 min bars for other instruments
                                IFeedDescriptor feedDescriptor = new FeedDescriptor();

                                feedDescriptor.setPeriod( Period.ONE_MIN );
                                feedDescriptor.setDataType( DataType.TIME_PERIOD_AGGREGATION );
                                feedDescriptor.setInstrument( chart.getInstrument() );
                                //				feedDescriptor.setOfferSide(OfferSide.BID);
                                //				feedDescriptor.setFilter(Filter.WEEKENDS);

                                chartPanels.get( chart ).getTesterChartController().setFeedDescriptor( feedDescriptor );
                                chartPanels.get( chart ).getTesterChartController().setChartAutoShift();
                                JPanel chartPanel = chartPanels.get( chart ).getChartPanel();
                                addChartPanel( chartPanel );
                        }
                }
                return;
        }

        @Override
        public void setExecutionControl( ITesterExecutionControl executionControl ) {
                this.executionControl = executionControl;
        }

        /**
         * Center a frame on the screen
         */
        private void centerFrame() {
                Toolkit tk = Toolkit.getDefaultToolkit();
                Dimension screenSize = tk.getScreenSize();
                int screenHeight = screenSize.height;
                int screenWidth = screenSize.width;
                setSize( screenWidth / 2, screenHeight / 2 );
                setLocation( screenWidth / 4, screenHeight / 4 );
        }

        /**
         * Add chart panel to the frame
         *
         * @param panel
         */
        private void addChartPanel( JPanel chartPanel ) {
                // removecurrentChartPanel();

                // this.currentChartPanel = chartPanel;
                chartPanel.setPreferredSize( new Dimension( frameWidth, frameHeight - controlPanelHeight ) );
                chartPanel.setMinimumSize( new Dimension( frameWidth, 150 ) );
                chartPanel.setMaximumSize( new Dimension( Short.MAX_VALUE, Short.MAX_VALUE ) );
                getContentPane().add( chartPanel );
                this.validate();
                chartPanel.repaint();
        }

        /**
         * Add buttons to start/pause/continue/cancel actions
         */
        private void addControlPanel() {

                controlPanel = new JPanel();
                FlowLayout flowLayout = new FlowLayout( FlowLayout.LEFT );
                controlPanel.setLayout( flowLayout );
                controlPanel.setPreferredSize( new Dimension( frameWidth, controlPanelHeight ) );
                controlPanel.setMinimumSize( new Dimension( frameWidth, controlPanelHeight ) );
                controlPanel.setMaximumSize( new Dimension( Short.MAX_VALUE, controlPanelHeight ) );

                startStrategyButton = new JButton( "Start strategy" );
                startStrategyButton.addActionListener( new ActionListener() {
                        @Override
                        public void actionPerformed( ActionEvent e ) {
                                startStrategyButton.setEnabled( false );
                                Runnable r = new Runnable() {
                                        public void run() {
                                                try {
                                                        Main.connectClient();
                                                } catch ( Exception e2 ) {
                                                        logger.error( e2.getMessage(), e2 );
                                                        e2.printStackTrace();
                                                        resetButtons();
                                                }
                                        }
                                };
                                Thread t = new Thread( r );
                                t.start();
                        }
                } );

                pauseButton = new JButton( "Pause" );
                pauseButton.addActionListener( new ActionListener() {
                        @Override
                        public void actionPerformed( ActionEvent e ) {
                                if ( executionControl != null ) {
                                        executionControl.pauseExecution();
                                        updateButtons();
                                }
                        }
                } );

                continueButton = new JButton( "Continue" );
                continueButton.addActionListener( new ActionListener() {
                        @Override
                        public void actionPerformed( ActionEvent e ) {
                                if ( executionControl != null ) {
                                        executionControl.continueExecution();
                                        updateButtons();
                                }
                        }
                } );

                cancelButton = new JButton( "Cancel" );
                cancelButton.addActionListener( new ActionListener() {
                        @Override
                        public void actionPerformed( ActionEvent e ) {
                                if ( executionControl != null ) {
                                        executionControl.cancelExecution();
                                        updateButtons();
                                }
                        }
                } );

                controlPanel.add( startStrategyButton );
                controlPanel.add( pauseButton );
                controlPanel.add( continueButton );
                controlPanel.add( cancelButton );
                getContentPane().add( controlPanel );

                pauseButton.setEnabled( false );
                continueButton.setEnabled( false );
                cancelButton.setEnabled( false );
        }

        public void updateButtons() {
                if ( executionControl != null ) {
                        startStrategyButton.setEnabled( executionControl.isExecutionCanceled() );
                        pauseButton.setEnabled( !executionControl.isExecutionPaused() && !executionControl.isExecutionCanceled() );
                        cancelButton.setEnabled( !executionControl.isExecutionCanceled() );
                        continueButton.setEnabled( executionControl.isExecutionPaused() );
                }
        }

        public void resetButtons() {
                startStrategyButton.setEnabled( true );
                pauseButton.setEnabled( false );
                continueButton.setEnabled( false );
                cancelButton.setEnabled( false );
        }

        public void showChartFrame() {
                setSize( frameWidth, frameHeight );
                centerFrame();
                addControlPanel();
                setVisible( true );
        }
}
