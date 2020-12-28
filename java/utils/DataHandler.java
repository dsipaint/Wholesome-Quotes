package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import main.Main;

public class DataHandler
{
	static final String FILE_LOC = "./quotes.json";
	
	@SuppressWarnings("unchecked")
	public static void readData() throws FileNotFoundException, IOException, ParseException
	{
		JSONParser jsp = new JSONParser();
		JSONObject file_obj = (JSONObject) jsp.parse(new BufferedReader(new FileReader(new File(FILE_LOC))));
		
		//add unapproved quotes
		JSONArray unapproved = (JSONArray) file_obj.get("unapproved");
		unapproved.forEach((quote) ->
		{
			JSONObject quote_obj = (JSONObject) quote;
			Main.uncheckedquotes.add(new Quote((String) quote_obj.get("userid"), (String) quote_obj.get("quote")));
		});
		
		//add approved quotes
		JSONArray approved = (JSONArray) file_obj.get("approved");
		approved.forEach((quote) ->
		{
			JSONObject quote_obj = (JSONObject) quote;
			Main.checkedquotes.add(new Quote((String) quote_obj.get("id"),(String) quote_obj.get("quote")));
		});
	}
	
	@SuppressWarnings("unchecked")
	public static void writeData() throws IOException
	{
		JSONObject file_obj = new JSONObject();
		JSONArray approved_arr = new JSONArray();
		JSONArray unapproved_arr = new JSONArray();
		
		//write unapproved quotes
		for(Quote quote : Main.uncheckedquotes)
		{
			JSONObject quote_obj = new JSONObject();
			quote_obj.put("id", quote.getId());
			quote_obj.put("quote", quote.getQuote());
			
			unapproved_arr.add(quote_obj);
		}
		
		//write approved quotes
		for(Quote quote : Main.checkedquotes)
		{
			JSONObject quote_obj = new JSONObject();
			quote_obj.put("id", quote.getId());
			quote_obj.put("quote", quote.getQuote());
			
			approved_arr.add(quote_obj);
		}
		
		//save all the info
		file_obj.put("unapproved", unapproved_arr);
		file_obj.put("approved", approved_arr);
		
		PrintWriter pw = new PrintWriter(new FileWriter(new File(FILE_LOC)));
		pw.println(file_obj.toJSONString());
		pw.close();
	}
	
	//finds a quote that has the desired id and quotetext, from Main.uncheckedquotes
	public static Quote findQuoteFromInfo(String id, String quotetext)
	{
		for(Quote quote : Main.uncheckedquotes)
		{
			if(quote.getId().equals(id) && quote.getQuote().equals(quotetext))
				return quote;
		}
		
		return null;
	}
}
