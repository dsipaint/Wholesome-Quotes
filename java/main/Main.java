package main;
import java.io.IOException;
import java.util.ArrayList;

import javax.security.auth.login.LoginException;

import org.json.simple.parser.ParseException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import utils.DataHandler;
import utils.Quote;
import utils.Server;

public class Main
{
	public static JDA jda;
	public static final String PREFIX = "^";
	public static ArrayList<Quote> uncheckedquotes = new ArrayList<Quote>();
	public static ArrayList<Quote> checkedquotes = new ArrayList<Quote>();
	
	public static void main(String[] args)
	{
		try
		{
			jda = JDABuilder.createDefault("")
					.enableIntents(GatewayIntent.GUILD_MEMBERS)
					.setMemberCachePolicy(MemberCachePolicy.ALL)
					.build();
		}
		catch (LoginException e)
		{
			e.printStackTrace();
		}
		
		try
		{
			jda.awaitReady();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		
		
		try
		{
			DataHandler.readData();
		}
		catch (IOException | ParseException e)
		{
			e.printStackTrace();
		}
		
		jda.getGuildById(Server.SERVER_ID).loadMembers(); //load all members into cache on startup- just makes life simpler for me
		
		jda.addEventListener(new CommandListener());
		jda.addEventListener(new ApprovalListener());
	}
}
