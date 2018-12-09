package left.book;

import java.math.BigDecimal;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Books {


	private static Books instance;

	private static ConcurrentHashMap< String, Book > bookCollection = new ConcurrentHashMap< String, Book >();

	private final static String[] symbols = {"btccny"};

	static
	{
		instance = new Books();
	}

	private Books()
	{

	}

	public void init()
	{
		for ( String s : symbols )
		{
			Book bk = new Book(s);
			bookCollection.put( s, bk );
		}
	}

	public static Books getInstance()
	{
		return instance;
	}

	public ConcurrentHashMap< String, Book > getBooks()
	{
		return bookCollection;
	}

	public void updateBook(JSONObject msg) {

		try {
			JSONObject groupOrder = msg.getJSONObject("grouporder");

			String symbol = groupOrder.getString("market");

			Book bk;

			if (bookCollection.containsKey(symbol))
			{
				bk = bookCollection.get(symbol);
				JSONArray asksJSON = groupOrder.getJSONArray("ask");
				JSONArray bidsJSON = groupOrder.getJSONArray("bid");
				SortedMap<BigDecimal, BigDecimal> asks = bk.getAsks();
				SortedMap<BigDecimal, BigDecimal> bids = bk.getBids();
				bids.clear();
				asks.clear();

				for (int i=0; i< asksJSON.length(); i++)
				{
					JSONObject o = asksJSON.getJSONObject(i);
					BigDecimal price = new BigDecimal(o.getString( "price" )).setScale(2);
					BigDecimal qty = new BigDecimal(o.getString( "totalamount" )).setScale(6);

					asks.put(price, qty);

				}

				for (int i=0; i< bidsJSON.length(); i++)
				{
					JSONObject o = bidsJSON.getJSONObject(i);
					BigDecimal price = new BigDecimal(o.getString( "price" )).setScale(2);
					BigDecimal qty = new BigDecimal(o.getString( "totalamount" )).setScale(6);

					bids.put(price, qty);

				}
				bk.notifyObs();
			}


		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
