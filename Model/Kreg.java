package left.Model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import java.util.concurrent.TimeUnit;

import left.book.Books;
import left.trades.MarketTrades;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;


public class Kreg extends Model implements Observer,Runnable {

	String modelName = "Kreg";
	String version = "0.1";
	String[] symbols = {"btccny"};

	Connection con = null;
	Statement st = null; 	// SQL statement configured with JDBC
	ResultSet rs;
	ResultSet rs1;//to execute the query

	String url = "url-to-db";
	String user = "db-user";
	String password = "db-pass";

	RConnection r;

	private static long lastTimestamp = 0;
	private static long latestTimestamp = 0;
	private static double ask0 = 0;
	private static double vol0 = 0;

	private static double profitDif1 = 0;

	ConcurrentNavigableMap<Long, Double> history = new ConcurrentSkipListMap<Long, Double>();
	ConcurrentNavigableMap<Double, Double> history_ask_bid = new ConcurrentSkipListMap<Double, Double>();

	private static String positions = " ";
	private final Logger log = LogManager.getLogger(Kreg.class);


	public Kreg()
	{

	}

	private void init(String symbol) {
		System.out.println("Initializing..");
		try {
			r = new RConnection();


			r.eval("library(Rcpp)");
			r.eval("library(inline)");
			r.eval("src <- 'Rcpp::NumericMatrix dataR(data);"
					+ "Rcpp::NumericVector weightsR(weights);"
					+ "int ncol = dataR.ncol();"
					+ "Rcpp::NumericVector sumR(ncol);"
					+ "for (int col = 0; col<ncol; col++){"
					+ "sumR[col] = Rcpp::sum(pow(weightsR-dataR( _, col),2));}"
					+ "return Rcpp::wrap(sumR);';"
					+ "weighted.colSums <- cxxfunction(signature(data='numeric', weights='numeric'), src, plugin='Rcpp');"
					+ "f.Rcpp <- cxxfunction( signature( x = 'matrix' ), 'NumericMatrix input( x ) ;"
					+ "NumericMatrix output  = clone<NumericMatrix>( input ) ;"
					+ "int nr = input.nrow(), nc = input.ncol() ;"
					+ "NumericVector tmp( nr );for( int i=0; i<nc; i++)"
					+ "{tmp = tmp + input.column(i) ;NumericMatrix::Column target( output, i );"
					+ "std::copy( tmp.begin(), tmp.end(), target.begin() ) ;}"
					+ "return output ;', plugin = 'Rcpp' );");
			System.out.println("Initialization Done!");
		} catch (RserveException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


		long start = System.currentTimeMillis();
		try{
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection(url, user, password);
			st = con.createStatement();

			Calendar cal = Calendar.getInstance();
			Date startDate = new Date(116,10,22);
			cal.setTime(startDate);
			Timestamp tm = new Timestamp(startDate.getTime());
			System.out.println(tm.toString());

			String query = "select time,avg from hourly";


			System.out.println("Loading data..");
			rs = st.executeQuery(query);


			while ( rs.next() )
			{
				history.put( rs.getTimestamp(1).getTime() , rs.getDouble(2));
				//history_ask_bid.put(rs.getDouble(3), rs.getDouble(4));
				if ( history.size() > 500 )
				{
					//history.remove(history.firstKey());
					latestTimestamp = rs.getTimestamp(1).getTime();
					System.out.println("LatestTimestamp = " + latestTimestamp);
					System.out.println("Calculating..");
					TimeUnit.SECONDS.sleep(10);

					calculatePosition();
					//System.out.println(rs.getTimestamp(1).getTime());
				}

			}
			System.out.println(positions);
			con.close();


			System.out.println("Initialization took " + (System.currentTimeMillis()-start )/60000 + " minutes" );

			String begintTime = "2016-12-22 00:00:00" ;


		}
		catch ( Exception e )
		{
			System.out.println("error@ reading from db " + e.getMessage());
		}

	}

	@Override
	public void update(Observable arg0, Object arg1)
	{

	}


	private void calculatePosition()
	{
		System.out.println("Calculating Position");

		double[] doubleArray = new double[ history.size() - 2 ];
		int count = 0;

		Double prev = (double) 0;
		for ( Double d : history.values() )
		{
			if ( count <= doubleArray.length )
			{
				if ( count > 0 )
				{
					doubleArray[count-1] = 100*(d - prev)/prev;
				}
				prev = d;
				count++;
			}
			else
			{
				System.out.println("Last monitored price: " + d);
			}
		}
		double lastPrice = doubleArray[doubleArray.length-1];
		System.out.println("Last price " + lastPrice);
		System.out.println("history size " + history.size());
		System.out.println("history size " + doubleArray.length);

		try {

			r.assign("dbtsAsk", doubleArray);
			r.eval("sizeOfData <- length(dbtsAsk)+1;"
					+ "firstDataSet1000 <- embed(dbtsAsk[seq(sizeOfData-1,1,-1)],sizeOfData-4);"
					+ "ymeans1000 <- colMeans(firstDataSet1000);"
					+ "j = (length(firstDataSet1000[1,])-1);"
					+ "norma1000 = weighted.colSums(firstDataSet1000[,(1):(j-1)],firstDataSet1000[,j]);"
					+ "Ax1000 <- exp(-0.25*norma1000);"
					+ "Zx1000 <- sum(exp( -0.25*norma1000 ));"
					+ "Xx1000 <- Ax1000/Zx1000;");
			double sig = r.eval("Xx1000%*%ymeans1000[2:j]").asDouble();
			System.out.println(  );
			System.out.println("Prediction Signal received... :" + sig);
		} catch (REngineException | REXPMismatchException e) {
			e.printStackTrace();
		}

		//DoubleMatrix squareDistance = Geometry.pairwiseSquaredDistances(matrix, matrix.getRow( matrix.rows-1 ));
		System.out.println("Last 3 prices: " + doubleArray[doubleArray.length-1] +"," + doubleArray[doubleArray.length-2]+"," + doubleArray[doubleArray.length-3]);
		System.out.println("last entry: " + history.lastEntry());
		//System.out.println("Matrix size: " + squareDistance.rows + " x " +squareDistance.columns);

	}


	class Position {
		int type; //long or short
		double openPrice;
		double closePrice;
		double amount;
		long timestamp;
		long closeTimestamp;

		public Position(int type, double openPos, double amount, long timestamp) {
			this.type = type;
			this.openPrice = openPos;
			this.amount = amount;
			this.timestamp = timestamp;
			this.closePrice = 0;
			this.closeTimestamp = 0;
		}

		protected void setClosePrice(double closePrice, long timestamp) {
			this.closePrice = closePrice;
			this.closeTimestamp = timestamp;
		}

		public String getJSON() throws JSONException {
			JSONObject obj = new JSONObject();
			obj.put("type", type == 1 ? "long" : "short");
			obj.put("openPrice", openPrice);
			if (closePrice != 0) {
				obj.put("closePrice",closePrice);
				obj.put("closeTimestamp", closeTimestamp);
			}
			obj.put("timestamp",timestamp);
			obj.put("result", openPrice - closePrice);
			return obj.toString();
		}

	}

	public String getPositions() {
		JSONArray arr = new JSONArray();
		for (Position p:pos) {
			try {
				arr.put(p.getJSON());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}
		return arr.toString();
	}


	Position currentPos = null;
	double openPos = 0;
	List<Position> pos = new ArrayList<Position>();
	List<Double> profit = new ArrayList<Double>();
	// -1	-> SELL
	// 0	-> NO POSITION
	// 1	-> BUY
	private void signal(double sig, double lastP) {



		if (currentPos == null) {
			double currentVwap = lastP;
			System.out.println("last entry: " + currentVwap);
			currentPos = new Position(sig<0 ? -1 : 1, currentVwap, 1, System.currentTimeMillis());
		} else {

			if (currentPos.type != -1 && sig < 0 ) {



				double currentVwap = history_ask_bid.lastEntry().getKey();
				System.out.println(currentVwap);
				//double currentVwap = Books.getInstance().getBooks().get( "btccny" ).getVwapBidPrice( 1 ).doubleValue();
				currentPos.setClosePrice(currentVwap, System.currentTimeMillis());
				pos.add(currentPos);
				currentPos = new Position(-1, currentVwap, 1, System.currentTimeMillis());

			} else if (currentPos.type != 1 && sig > 0 ) {
				double currentVwap = history_ask_bid.lastEntry().getValue();;
				//double currentVwap = Books.getInstance().getBooks().get( "btccny" ).getVwapAskPrice( 1 ).doubleValue();
				currentPos.setClosePrice(currentVwap, System.currentTimeMillis());
				pos.add(currentPos);
				currentPos = new Position(1, currentVwap, 1, System.currentTimeMillis());
			}

		}
		//positions += pos + "," + profit + "," + currentPos + "," + openPos + "/n";
		log.debug("pos: " + pos);
		log.debug("profit " + profit );
		log.debug("currentPos " + currentPos );
		log.debug("openPos "+ openPos );
		log.debug(" ");
	}

	int currentPosDif = 0;
	double openPosDif = 0;
	double proofit = 0;
	List<Integer> posDif = new ArrayList<Integer>();
	List<Double> profitDif = new ArrayList<Double>();
	private void signalDif(double sig) {
		if ( currentPosDif != -1  && sig < 0 )
		{
			//double currentVwap = Books.getInstance().getBooks().get( "btccny" ).getVwapBidPrice( 1 ).doubleValue();
			double currentVwap = history_ask_bid.lastEntry().getKey();
			if ( currentPosDif != 0 )
			{
				posDif.add( currentPosDif );
				profitDif.add( currentVwap - openPosDif );
				profitDif1 = openPosDif - currentVwap;
				proofit = currentVwap - openPosDif;
				//System.out.println("PROFITDIF =  " + proofit);
			}
			currentPosDif = -1;
			openPosDif = currentVwap;
		}
		else if ( currentPosDif != 1 && sig > 0 )
		{
			//double currentVwap = Books.getInstance().getBooks().get( "btccny" ).getVwapAskPrice( 1 ).doubleValue();
			double currentVwap = history_ask_bid.lastEntry().getValue();
			if ( currentPosDif != 0 )
			{
				posDif.add( currentPosDif );
				profitDif.add( openPosDif - currentVwap );
				profitDif1 = openPosDif - currentVwap;
			}
			currentPosDif = 1;
			openPosDif = currentVwap;
		}
		log.debug("posDif: " + posDif);
		log.debug("profitDif " + profitDif );
		log.debug("currentPosDif " + currentPosDif );
		log.debug("openPosDif "+ openPosDif );
		log.debug(" ");
		positions += currentPosDif + "," + profitDif1 + "," + currentPosDif + "," + openPosDif + "\n";

	}

	int currentPosDif1 = 0;
	double openPosDif1 = 0;
	List<Integer> posDif1 = new ArrayList<Integer>();


	@Override
	public void run() {
		for (String symbol : symbols)
		{
			//Books.getInstance().getBooks().get(symbol).addObserver( this );
			init(symbol);
			MarketTrades.getInstance().getMarketTrades().get(symbol).addObserver( this );
		}

	}



}
