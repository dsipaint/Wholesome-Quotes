package main;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import utils.Server;

public class CommandListener extends ListenerAdapter
{
	private Connection con;
	
	public CommandListener()
	{
		con = utils.DataHandler.getConnection();
	}
	
	public void onGuildMessageReceived(GuildMessageReceivedEvent e)
	{
		String msg = e.getMessage().getContentRaw();
		String[] args = msg.split(" ");
		
		if(args[0].equalsIgnoreCase(Main.PREFIX + "positive"))
		{
			//^positive
			if(args.length == 1)
			{
				try
				{
					ResultSet quotes = con.createStatement().executeQuery("select * from approvedwholesomequotes order by RAND() limit 1");
					quotes.next();
					Member member = e.getGuild().getMemberById(quotes.getLong("userid"));
					MessageEmbed quotepost  = new EmbedBuilder()
							.setAuthor(member.getEffectiveName(), null, member.getUser().getAvatarUrl())
							.setTitle("A wholesome message for you!")
							.setDescription(quotes.getString("quote"))
							.setColor(Server.EMBED_COL_INT)
							.build();
					
					e.getChannel().sendMessage(quotepost).queue();
					
				}
				catch (SQLException e1)
				{
					e1.printStackTrace();
				}
				
				return;
			}
			else //^positive {wholesome message}
			{
				//construct quote
				String quote = "";
				for(int i = 1; i < args.length; i++)
					quote += args[i] + " ";
				
				quote = formatExternalEmotes(quote.trim(), e.getMessage().getEmotes());
				
				if(Server.isStaff(e.getMember()))
				{
					//auto submit
					try
					{
						PreparedStatement s = con.prepareStatement("insert into approvedwholesomequotes (userid, quote) values (?,?)");
						s.setLong(1, e.getAuthor().getIdLong());
						s.setString(2, quote);
						s.executeUpdate();
					}
					catch (SQLException e1)
					{
						e1.printStackTrace();
					}
					
					Server.sendQuote(e.getAuthor().getId(), quote);
					e.getChannel().sendMessage("Wholesome quote posted!").queue();
				}
				else
				{
					//store as pending
					try
					{
						PreparedStatement s = con.prepareStatement("insert into pendingwholesomequotes (userid, quote) values (?,?)");
						s.setLong(1, e.getAuthor().getIdLong());
						s.setString(2, quote);
						s.executeUpdate();
					}
					catch (SQLException e1)
					{
						e1.printStackTrace();
					}
					
					//send for approval
					Server.sendApproval(e.getAuthor().getId(), quote);
					e.getChannel().sendMessage("Your wholesome quote has been submitted!").queue();
				}
			}
			
			return;
		}
		
		if(args[0].equalsIgnoreCase(Main.PREFIX + "positivehelp"))
		{
			EmbedBuilder eb = new EmbedBuilder()
					.setTitle("**Wholesome Quotes Help:**")
					.setColor(Server.EMBED_COL_INT)
					.setDescription("**" + Main.PREFIX + "positive {wholesome message}: **"
							+ "submits a wholesome message to display to the whole server :)")
					
						.appendDescription("\n\n**" + Main.PREFIX + "positive: **"
							+ "get a random wholesome message just for you :)")
						
						.appendDescription("\n\n**" + Main.PREFIX + "positivehelp: **"
							+ "displays this message")
						
						.appendDescription("\n\n**" + Main.PREFIX + "disable wholesomequotes: **"
							+ "(staff only) disables the wholesome quotes feature");
			
					e.getChannel().sendMessage(eb.build()).queue();
					
					return;
		}
		
		if(utils.Server.isStaff(e.getMember()) && args[0].equalsIgnoreCase(Main.PREFIX + "disable") && args[1].equalsIgnoreCase("wholesomequotes"))
		{
			e.getChannel().sendMessage("*Wholesome quotes feature disabled-- ask al if you want it back up*").complete();
			Main.jda.shutdownNow();
			System.exit(0);
		}
	}
	
	public static String formatExternalEmotes(String inputstring, List<Emote> emotes)
	{
		for(Emote emote : emotes)
		{
			//if we can't print this nicely
			if(!Main.jda.getEmotes().contains(emote))
				inputstring = inputstring.replace(emote.getAsMention(), ":" + emote.getName() + ":");
		}
		
		return inputstring;
	}
}
