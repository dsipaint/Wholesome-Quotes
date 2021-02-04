package utils;

import java.time.Instant;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

public class Server
{
	public static final String approvalEmoteStr = "white_check_mark", rejectEmoteStr = "x";
	public static final String approvalEmoteUnicode = "\u2705", rejectEmoteUnicode = "\u274C";
	public static final String approval_channel_id = "653257117536485387", post_channel_id = "792910160388685824", log_channel_id = "565631919728099338";
	public static final int EMBED_COL_INT = 65280;
	
	//return true if a member has discord mod, admin or is owner
	public static boolean isStaff(Member m)
	{
		try
		{
			//if owner
			if(m.isOwner())
				return true;
		}
		catch(NullPointerException e)
		{
			//no error message reee its pissing me off
		}
		
		//if admin
		if(m.hasPermission(Permission.ADMINISTRATOR))
			return true;
		
		//if discord mod TODO: Make discord mod module for all servers
		switch(m.getGuild().getId())
		{
			case "565623426501443584" : //wilbur's discord
				for(Role r : m.getRoles())
				{
					if(r.getId().equals("565626094917648386")) //wilbur discord mod
						return true;
				}
				break;
				
			case "640254333807755304" : //charlie's server
				for(Role r : m.getRoles())
				{
					if(r.getId().equals("640255355401535499")) //charlie discord mod
						return true;
				}
				break;
		}
		
		return false;
	}
	
	
	public static void sendApproval(String userID, String quote)
	{
		/*
		 * This method sends a log to a channel so that quotes can be approved.
		 * This method assumes the message is a legitimate submission syntax-wise
		 */
		
		EmbedBuilder eb = new EmbedBuilder()
			.setTitle("New Wholesome Quote Submission:")
			.setAuthor(main.Main.jda.getUserById(userID).getAsTag(), null, main.Main.jda.getUserById(userID).getAvatarUrl())
			.addField("User id:", userID, true)
			.setDescription(quote)
			.setTimestamp(Instant.now())
			.setFooter("wholesomequotes")
			.setColor(EMBED_COL_INT);
		
		//send to approval channel
		main.Main.jda.getTextChannelById(approval_channel_id).sendMessage(eb.build()).queue(log ->
		{
			//emote: white_check_mark
			log.addReaction(approvalEmoteUnicode).queue();
			//emote: x
			log.addReaction(rejectEmoteUnicode).queue();
		});
	}
	
	public static void sendQuote(String userid, String quote)
	{
		EmbedBuilder eb = new EmbedBuilder()
				.setTitle("New Wholesome Message!")
				.setAuthor(main.Main.jda.getUserById(userid).getAsTag(), null, main.Main.jda.getUserById(userid).getAvatarUrl())
				.setDescription(quote)
				.setColor(EMBED_COL_INT);
		
		main.Main.jda.getTextChannelById(post_channel_id).sendMessage(eb.build()).queue();
	}
	
	public static void sendLog(String userid, String quote, String approvalid, boolean approved)
	{
		EmbedBuilder eb = new EmbedBuilder()
				.setTitle("Wholesome Quote " + (approved ? "Accepted" : "Rejected"))
				.setAuthor(main.Main.jda.getUserById(userid).getAsTag(), null, main.Main.jda.getUserById(userid).getAvatarUrl())
				.addField("user id", userid, true)
				.addField((approved ? "Approved" : "Rejected") + "by: ", main.Main.jda.getUserById(approvalid).getAsTag(), true)
				.setDescription(quote)
				.setTimestamp(Instant.now())
				.setColor(EMBED_COL_INT);
		
		main.Main.jda.getTextChannelById(log_channel_id).sendMessage(eb.build()).queue();
	}
}
