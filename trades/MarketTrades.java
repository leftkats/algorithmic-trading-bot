package left.trades;

import java.math.BigDecimal;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MarketTrades {
		//static SortedMap< BigDecimal, BigDecimal > bids = Collections.synchronizedSortedMap( new TreeMap< BigDecimal, BigDecimal >(Collections.reverseOrder()));
		//static SortedMap< BigDecimal, BigDecimal > asks = Collections.synchronizedSortedMap( new TreeMap< BigDecimal, BigDecimal >());
		private static MarketTrades instance;

		private static ConcurrentHashMap< String, MarketTrade > marketTradesCollection = new ConcurrentHashMap< String, MarketTrade >();

		private final static String[] symbols = {"btccny"};

		static 
		{
			instance = new MarketTrades();
		}

		private MarketTrades()
		{

		}

		public void init()
		{
			for ( String s : symbols )
			{
				MarketTrade mt = new MarketTrade(s);
				marketTradesCollection.put( s, mt );
			}
		}

		public static MarketTrades getInstance()
		{
			return instance;
		}
		
		public ConcurrentHashMap< String, MarketTrade > getMarketTrades()
		{
			return marketTradesCollection;
		}

		public void updateMarketTrade(JSONObject msg) {

			try {
				
				String symbol = msg.getString("market");
				
				MarketTrade mt;
				
				if (marketTradesCollection.containsKey(symbol))
				{
					mt = marketTradesCollection.get(symbol);
					
					BigDecimal amount = new BigDecimal(msg.getString("amount"));
					BigDecimal price = new BigDecimal(msg.getString("price"));
					String tradeId = msg.getString("trade_id");
					String type = msg.getString("type");
					long date = msg.getLong("date");
					
					mt.setVars( amount, price, type, tradeId, date);
					mt.notifyObs();
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
}
