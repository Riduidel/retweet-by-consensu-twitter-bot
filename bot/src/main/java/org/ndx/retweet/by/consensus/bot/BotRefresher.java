package org.ndx.retweet.by.consensus.bot;

import java.io.IOException;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import twitter4j.TwitterException;

@Path("/refresh")
public class BotRefresher {
	
	@Inject CuratorMessagesReader reader;
	/**
	 * Fires a force refresh
	 * @return For each mention of the curator account, will return a status indicating
	 * if this status has triggered a vote, and what is the vote result
	 * @throws TwitterException
	 * @throws IOException
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response refresh() throws TwitterException, IOException {
		return Response.ok(reader.readAll()).build();
	}

}
