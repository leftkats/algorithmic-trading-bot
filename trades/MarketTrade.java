package left.trades;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Observable;
import java.util.SortedMap;
import java.util.TreeMap;

public class MarketTrade extends Observable {
	private final String name;
	private BigDecimal amount;
	private BigDecimal price;
	private String type;
	private String tradeId;
	private Long timestamp;
			
	MarketTrade(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public Long getTimestamp()
	{
		return this.timestamp;
	}
	
	public BigDecimal getAmount()
	{
		return amount;
	}
	
	public BigDecimal getPrice()
	{
		return price;
	}
	
	public String getType()
	{
		return type;
	}
	
	public String getId()
	{
		return tradeId;
	}
	
	public void setVars(BigDecimal amount, BigDecimal price, String type, String tradeId, Long date)
	{
		this.amount = amount;
		this.price = price;
		this.type = type;
		this.tradeId = tradeId;
		this.timestamp = date;
		
	}
	public void notifyObs() {
		setChanged();
		notifyObservers();
		
	}

	
}
