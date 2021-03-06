package utils;

import main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

public class Server
{
	public static final String APPROVAL_EMOTE_STR = "white_check_mark", REJECT_EMOTE_STR = "x";
	public static final String APPROVAL_EMOTE_UNICODE = "\u2705", REJECT_EMOTE_UNICODE = "\u274C";
	public static final String JUDGEMENT_CHANNEL_ID = "653257117536485387", QUOTE_CHANNEL_ID = "792910160388685824", LOG_CHANNEL_ID = "565631919728099338";
	public static final String SERVER_ID = "565623426501443584";
	public static final int EMBED_COL_INT = 65280;
	public static final String FOOTER_IDENTIFIER = "wholesomequotes";
	
	public static boolean isStaff(Member m)
	{
		for(Role r : m.getRoles())
		{	
			//admin: 602889336748507164  mod: 565626094917648386
			if(r.getId().equals("602889336748507164") || r.getId().equals("565626094917648386"))
				return true;
		}
		return false;
	}
	
	
	public static void sendJudgement(Quote quote)
	{
		/*
		 * This method sends a log to a channel so that quotes can be approved.
		 * This method assumes the message is a legitimate submission syntax-wise
		 */
		
		EmbedBuilder eb = new EmbedBuilder()
			.setTitle("New Wholesome Quote Submission:")
			.setColor(Server.EMBED_COL_INT)
			.setAuthor(main.Main.jda.getUserById(quote.getId()).getAsTag())
			.addField("Author", Main.jda.getUserById(quote.getId()).getAsTag(), true)
			.addField("Quote", quote.getQuote(), true)
			.addField("Id", quote.getId(), true)
			.setFooter(FOOTER_IDENTIFIER); //placed here so the bot can identify wholesome quotes
		
		//send to log channel
		main.Main.jda.getTextChannelById(JUDGEMENT_CHANNEL_ID).sendMessage(eb.build()).queue((log) ->
		{
			//emote: white_check_mark
			log.addReaction(APPROVAL_EMOTE_UNICODE).queue();
			//emote: x
			log.addReaction(REJECT_EMOTE_UNICODE).queue();
		});
	}
}
