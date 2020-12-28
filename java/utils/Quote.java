package utils;

public class Quote
{
	private String id, quote;
	
	public Quote(String id, String quote)
	{
		this.id = id;
		this.quote = quote;
	}
	
	public String getId()
	{
		return this.id;
	}
	
	public String getQuote()
	{
		return this.quote;
	}
	
	@Override
	public String toString()
	{
		return this.id + "\t" + this.quote + "\n";
	}
}
