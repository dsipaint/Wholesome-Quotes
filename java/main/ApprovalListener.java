package main;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import utils.Server;

public class ApprovalListener extends ListenerAdapter
{
	private Connection con;
	
	public ApprovalListener()
	{
		con = utils.DataHandler.getConnection();
	}
	
	public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent e)
	{
		if(e.getChannel().getId().equals(utils.Server.approval_channel_id))
		{
			//bot ignores all non-staff and itself
			if(!utils.Server.isStaff(e.getMember()) || e.getMember().getUser().equals(e.getJDA().getSelfUser()))
				return;
			
			e.getChannel().retrieveMessageById(e.getMessageId()).queue(msg ->
			{
				//if a reaction is added to a message in the RIGHT channel but it isn't a reaction to the bot's message
				if(!msg.getAuthor().equals(e.getJDA().getSelfUser()) || !msg.getEmbeds().get(0).getFooter().getText().equals("wholesomequotes"))
					return;
				
				//only interested in the right reactions
				if(!e.getReactionEmote().getName().equals(utils.Server.approvalEmoteUnicode) && !e.getReactionEmote().getName().equals(utils.Server.rejectEmoteUnicode))
					return;
				
				//every embed log should have an image with a url, and a user id
				MessageEmbed em = msg.getEmbeds().get(0);
				String userID = null;
				String quote = em.getDescription();
				for(Field field : em.getFields())
				{
					if(field.getName().equals("User id:"))
					{
						userID = field.getValue();
						break;
					}
				}
				
				try
				{
					//always remove from the pending quotes list (quotes assumed to be unique, and if not, this deletes duplicates anyway)
					con.createStatement().executeUpdate("delete from pendingwholesomequotes where quote = \""
							+ quote + "\"");
					
					//approved
					if(e.getReactionEmote().getName().equals(utils.Server.approvalEmoteUnicode))
					{
						PreparedStatement s = con.prepareStatement("insert into approvedwholesomequotes (userid, quote) values (?,?)");
						s.setLong(1, Long.parseLong(userID));
						s.setString(2, quote);
						s.executeUpdate();
						
						Server.sendQuote(userID, quote);
						e.getChannel().sendMessage("Wholesome quote approved!").queue();
						Server.sendLog(userID, quote, e.getMember().getId(), true);
					}
					else
					{
						e.getChannel().sendMessage("Wholesome quote rejected").queue();
						Server.sendLog(userID, quote, e.getMember().getId(), false);
					}
				}
				catch(SQLException e1)
				{
					e1.printStackTrace();
				}
				
				msg.delete().queue();
			});
		}
	}
}
