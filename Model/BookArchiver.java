package left.Model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.SortedMap;

import left.book.Book;
import left.book.Books;
import left.trades.MarketTrades;

import java.sql.*;


public class BookArchiver extends Model implements Observer {

	String[] symbols = {"btccny"};


	public BookArchiver()
	{
		for (String symbol : symbols)
		{
			Books.getInstance().getBooks().get(symbol).addObserver( this );
		}
	}

	@Override
	public void update(Observable arg0, Object arg1)
	{
		Book bk = (Book)arg0;

		storeToDB(bk);

	}

	private void storeToDB(Book bk) {
		Connection con = null;
		Statement st = null;
		int rs = 0;

		String url = "url-to-db";
		String user = "db-user";
		String password = "db-pass";

		try{
			con = DriverManager.getConnection(url, user, password);
			st = con.createStatement();

			String query = "INSERT INTO BtcCnyBook ( timestamp,";

			for ( int i=0; i<bk.getAsks().size(); i++ )
			{
				if (i>0) query+=",";
				query += "bid" + i + ",volBid"+ i + ",ask" + i +",volAsk"+ i;
			}
			query += ") VALUES ( '" + new Timestamp(System.currentTimeMillis()).toString() + "',";

			Iterator iterAsk = bk.getAsks().keySet().iterator();
			Iterator iterBid = bk.getBids().keySet().iterator();

			for ( int i=0; i<bk.getAsks().size(); i++ )
			{
				BigDecimal ask = (BigDecimal) iterAsk.next();
				BigDecimal bid = (BigDecimal) iterBid.next();

				if (i>0) query+=",";
				query += ask + ","+ bk.getAsks().get(ask) + "," + bid +","+ bk.getBids().get(bid);

			}
			query += ");";

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
