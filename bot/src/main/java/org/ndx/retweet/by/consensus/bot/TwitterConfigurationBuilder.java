package org.ndx.retweet.by.consensus.bot;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * A resource used to show used configuration. This resource will use a template
 * filled with infos obtained from configuration. Presented infos are
 * 
 * <ul>
 * <li>Application name and link (as obtained from Twitter)
 * <li>Curator account name and link
 * <li>Curator lists members (the producers as well as the moderators ones)
 * <li>Presenter account name and link
 * </ul>
 * 
 * Maybe we will add some stats later
 * 
 * @author nicolas-delsaux
 *
 */
@Path("/")
public class TwitterConfigurationBuilder {
	
	@Inject @Named(TwitterProducer.CURATOR) Twitter curator;
	
	@Inject
	Template twitterConfigurationViewer;

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public TemplateInstance get() throws TwitterException, IOException {
	    return twitterConfigurationViewer.data("curator", curator
	    		);
	}

}
