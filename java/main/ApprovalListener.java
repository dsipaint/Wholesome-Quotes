package main;
import java.io.IOException;
import java.time.Instant;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import utils.DataHandler;
import utils.Quote;
import utils.Server;

public class ApprovalListener extends ListenerAdapter
{	
	public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent e)
	{
		if(!e.getGuild().getId().equals(Server.SERVER_ID)) //only wilbur's server
			return;
		
		if(e.getUser().equals(e.getJDA().getSelfUser())) //don't respond to self
			return;
		
		if(e.getChannel().getId().equals(Server.JUDGEMENT_CHANNEL_ID))
		{			
			e.getChannel().retrieveMessageById(e.getMessageId()).queue((msg) ->
			{
				//if a reaction is added to a message in the RIGHT channel but it isn't a reaction to the bot's message- bad system, relies on only bot messages being quotes
				if(!msg.getAuthor().equals(e.getJDA().getSelfUser()))
					return;
				
				//only interested in the right reactions
				if(!e.getReactionEmote().getName().equals(Server.APPROVAL_EMOTE_UNICODE) && !e.getReactionEmote().getName().equals(Server.REJECT_EMOTE_UNICODE))
					return;
				
				//every embed log should have a footer identifier
				MessageEmbed em = msg.getEmbeds().get(0);
				
				//formatted like this in utils.Server.sendJudgement() (may change in the future)
				if(em.getFooter().getText() == null || !em.getFooter().getText().equals(Server.FOOTER_IDENTIFIER))
					return;
				
				String id = "", quotetext = "";
				for(Field field : em.getFields())
				{
					if(field.getName().equals("Id"))
						id = field.getValue();
					else if(field.getName().equals("Quote"))
						quotetext = field.getValue();
					
					if(!id.isEmpty() && !quotetext.isEmpty())
						break;
				}
				
				//always remove from the pending quotes list
				Quote quote = DataHandler.findQuoteFromInfo(id, quotetext);
				Main.uncheckedquotes.remove(quote);
				
				//approved
				if(e.getReactionEmote().getName().equals(Server.APPROVAL_EMOTE_UNICODE))
				{
					Main.checkedquotes.add(quote);
					
					//save in storage after updating
					try
					{
						DataHandler.writeData();
					}
					catch (IOException e1)
					{
						e1.printStackTrace();
					}
					
					EmbedBuilder quotePost = new EmbedBuilder()
						.setTitle("New Wholesome Message!")
						.setColor(Server.EMBED_COL_INT)
						.setAuthor(e.getGuild().getMemberById(id).getEffectiveName(), null, e.getGuild().getMemberById(id).getUser().getAvatarUrl())
						.setDescription(quotetext);
					
					Main.jda.getTextChannelById(Server.QUOTE_CHANNEL_ID).sendMessage(quotePost.build()).queue();
					
					e.getChannel().sendMessage("Wholesome post approved!").queue();
					
					EmbedBuilder log = new EmbedBuilder()
							.setTitle("Wholesome post approved")
							.setColor(Server.EMBED_COL_INT)
							.addField("From: ", e.getGuild().getMemberById(quote.getId()).getUser().getAsTag(), true)
							.addField("Approved by: ", e.getUser().getAsTag(), true)
							.setDescription(quote.getQuote())
							.setTimestamp(Instant.now());
					
					e.getGuild().getTextChannelById(Server.LOG_CHANNEL_ID).sendMessage(log.build()).queue();
					
					msg.delete().queue();
				}
				
				//rejected
				if(e.getReactionEmote().getName().equals(Server.REJECT_EMOTE_UNICODE))
				{
					e.getChannel().sendMessage("Wholesome post rejected").queue();
					
					EmbedBuilder log = new EmbedBuilder()
							.setTitle("Wholesome post rejected")
							.setColor(Server.EMBED_COL_INT)
							.addField("From: ", e.getGuild().getMemberById(quote.getId()).getUser().getAsTag(), true)
							.addField("Rejected by: ", e.getUser().getAsTag(), true)
							.setImage(quote.getQuote())
							.setTimestamp(Instant.now());
					
					e.getGuild().getTextChannelById(Server.LOG_CHANNEL_ID).sendMessage(log.build()).queue();
					
					msg.delete().queue();
				}
			});
		}
	}
}
