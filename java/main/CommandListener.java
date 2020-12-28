package main;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import utils.DataHandler;
import utils.Quote;
import utils.Server;

public class CommandListener extends ListenerAdapter
{
	private Random r;
	
	public CommandListener()
	{
		r = new Random();
	}
	
	public void onGuildMessageReceived(GuildMessageReceivedEvent e)
	{
		String msg = e.getMessage().getContentRaw();
		String[] args = msg.split(" ");
		String userID = e.getAuthor().getId();
		
		if(!e.getGuild().getId().equals(Server.SERVER_ID)) //only wilbur's server
			return;
		
		//^positive {ping/id}
		if(args[0].equalsIgnoreCase(Main.PREFIX + "positive"))
		{
			//^positive
			if(args.length == 1)
			{
				Quote randomquote = Main.checkedquotes.get(r.nextInt(Main.checkedquotes.size()));
				Member member = e.getGuild().getMemberById(randomquote.getId());
				MessageEmbed wholesomeembed = new EmbedBuilder()
						.setTitle("A wholesome message for you!")
						.setAuthor(member.getEffectiveName(), null, member.getUser().getAvatarUrl())
						.setDescription(randomquote.getQuote())
						.setColor(Server.EMBED_COL_INT)
						.build();
				
				e.getChannel().sendMessage(wholesomeembed).queue();
				return;
			}
			else //^positive {message}
			{
				//create quote object
				String wholesomequotetext = "";
				for(int i = 1; i < args.length; i++)
					wholesomequotetext += args[i] + " ";
				
				wholesomequotetext = formatExternalEmotes(wholesomequotetext.trim(), e.getMessage().getEmotes()); //format the text safely
				
				if(wholesomequotetext.length() > MessageEmbed.TEXT_MAX_LENGTH)
				{
					e.getChannel().sendMessage("Your message is too big for our bot! Sorry :/").queue();
					return;
				}
				
				Quote newquotesubmission = new Quote(userID, wholesomequotetext);
				
				
				if(!utils.Server.isStaff(e.getMember()))
				{
					//post in mod channel, inform user, if user is not staff
					Main.uncheckedquotes.add(newquotesubmission);
					Server.sendJudgement(newquotesubmission);
					e.getChannel().sendMessage("Your wholesome quote was submitted!").queue();
				}
				else
				{
					//if user is staff, skip approval step
					Main.checkedquotes.add(newquotesubmission);
					EmbedBuilder quotePost = new EmbedBuilder()
							.setTitle("New Wholesome Message!")
							.setColor(Server.EMBED_COL_INT)
							.setAuthor(e.getGuild().getMemberById(newquotesubmission.getId()).getEffectiveName(), null, e.getGuild().getMemberById(newquotesubmission.getId()).getUser().getAvatarUrl())
							.setDescription(newquotesubmission.getQuote())
							.setColor(Server.EMBED_COL_INT);
					
					e.getGuild().getTextChannelById(Server.QUOTE_CHANNEL_ID).sendMessage(quotePost.build()).queue();
					e.getChannel().sendMessage("Your wholesome quote was posted!").queue();
				}
				
				e.getChannel().sendMessage("Your wholesome message has been submitted!");
				return;
			}
		}
		
		//^positivehelp
		if(msg.equalsIgnoreCase(Main.PREFIX + "positivehelp"))
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
			try
			{
				DataHandler.writeData();
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
			e.getChannel().sendMessage("*Wholesome quotes feature disabled-- ask al if you want it back up*").complete();
			Main.jda.shutdown();
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
