package left.Model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import left.trades.MarketTrade;
import left.trades.MarketTrades;

public class TradesArchiver extends Model implements Observer {

	String[] symbols = {"btccny"};


	public TradesArchiver()
	{
		for (String symbol : symbols)
		{
			//Books.getInstance().getBooks().get(symbol).addObserver( this );
			MarketTrades.getInstance().getMarketTrades().get(symbol).addObserver( this );
		}
	}

	@Override
	public void update(Observable arg0, Object arg1)
	{
		MarketTrade mt = (MarketTrade)arg0;

		storeToDB(mt);

	}

	private void storeToDB(MarketTrade bk) {
		Connection con = null;
		Statement st = null;
		int rs = 0;

		String url = "url-to-db";
		String user = "db-user";
		String password = "db-pass";

		try{
			con = DriverManager.getConnection(url, user, password);
			st = con.createStatement();
			String query = "INSERT INTO MarketTrades ( timestamp,price,amount,type,tradeId) Values('"
					+ new Timestamp(System.currentTimeMillis()).toString() +
					"'," 	+ bk.getPrice() +
					"," 	+ bk.getAmount() +
					",'" 	+ bk.getType() +
					"','" 	+ bk.getId() + "')";

			rs = st.executeUpdate(query);
			con.close();

		}
		catch ( Exception e)
		{
			System.out.println("error writing to db " + e.getMessage());
		}

	}

	@Override
	public String getPositions() {
		// TODO Auto-generated method stub
		return null;
	}

}
