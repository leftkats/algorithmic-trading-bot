package left.book;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.SortedMap;
import java.util.TreeMap;

public class Book extends Observable {
	
	private final String name;
	private SortedMap< BigDecimal, BigDecimal > bids;
	private SortedMap< BigDecimal, BigDecimal > asks;
			
	public Book(String name)
	{
		this.name = name;
		this.bids = Collections.synchronizedSortedMap( new TreeMap< BigDecimal, BigDecimal >(Collections.reverseOrder()));
		this.asks = Collections.synchronizedSortedMap( new TreeMap< BigDecimal, BigDecimal >());
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public SortedMap< BigDecimal, BigDecimal > getBids()
	{
		return bids;
	}
	
	public SortedMap< BigDecimal, BigDecimal > getAsks()
	{
		return asks;
	}

	public void setAsks(TreeMap<BigDecimal, BigDecimal> asks1) {
		asks = asks1;
	}
	
	public void setBids(TreeMap<BigDecimal, BigDecimal> bids1) {
		bids = bids1;
	}

	public void notifyObs() {
		setChanged();
		notifyObservers();
		
	}
	
	public BigDecimal getVwapAskPrice(double vwap)
	{
		
		Iterator<BigDecimal> askKeys = asks.keySet().iterator();
		
		BigDecimal vwapAsk = new BigDecimal(0);
		
		BigDecimal volAsk = new BigDecimal(0);
		
		
		
		while ( askKeys.hasNext() && volAsk.doubleValue() < vwap )
		{
			BigDecimal ask = askKeys.next();
			
			BigDecimal va = asks.get(ask);
			
			volAsk = volAsk.add(va);
			
			vwapAsk = vwapAsk.add(ask.multiply(va));
			
		}
		return vwapAsk.divide(volAsk,2,RoundingMode.HALF_UP);
			
	}
	
	public BigDecimal getVwapBidPrice(double vwap)
	{
		int count = 0;
		List<BigDecimal> vwaps = new ArrayList<BigDecimal>();
		
		Iterator<BigDecimal> askKeys = bids.keySet().iterator();
		
		BigDecimal vwapAsk = new BigDecimal(0);
		
		BigDecimal volAsk = new BigDecimal(0);
		
		
		
		while ( askKeys.hasNext() && volAsk.doubleValue() < vwap )
		{
			BigDecimal ask = askKeys.next();
			
			BigDecimal va = bids.get(ask);
			
			volAsk = volAsk.add(va);
			
			vwapAsk = vwapAsk.add(ask.multiply(va));
			
		}
		return vwapAsk.divide(volAsk,2,RoundingMode.HALF_UP);
	}
	
	
	
}
