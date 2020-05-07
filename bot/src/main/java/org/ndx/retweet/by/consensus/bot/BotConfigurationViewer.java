package org.ndx.retweet.by.consensus.bot;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.ndx.retweet.by.consensus.bot.twitter.TwitterList;
import org.ndx.retweet.by.consensus.bot.twitter.TwitterListsProducer;
import org.ndx.retweet.by.consensus.bot.twitter.TwitterProducer;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

@Path("/")
public class BotConfigurationViewer {
	
	@Inject @Named(TwitterProducer.CURATOR) Twitter curator;
	@Inject @Named(TwitterProducer.PRESENTER) Twitter presenter;
	@Inject @ConfigProperty(name="PRODUCERS_LIST", defaultValue = "producers") String producersList;
	@Inject @ConfigProperty(name="MODERATORS_LIST", defaultValue = "moderators") String moderatorsList;
	@Inject @Named(TwitterListsProducer.PRODUCERS) TwitterList producers;
	@Inject @Named(TwitterListsProducer.MODERATORS) TwitterList moderators;
	
	@Inject
	Template twitterConfigurationViewer;

	/**
	 * A resource used to show used configuration. This resource will use a template
	 * filled with infos obtained from configuration. Presented infos are
	 * 
	 * <ul>
	 * <li>Curator account name and link
	 * <li>Curator lists members (the producers as well as the moderators ones)
	 * <li>Presenter account name and link
	 * </ul>
	 * 
	 * Maybe we will add some stats later
	 *
	 * TODO add an endpoint to validate config for each producer and moderator
	 */
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public TemplateInstance get() throws TwitterException, IOException {
	    return twitterConfigurationViewer
	    		.data("curator", curator)
	    		.data("producers_list_name", producersList)
	    		.data("producers_user_list", producers.getUsers().stream().map(u -> u.getScreenName()).collect(Collectors.joining(", ")))
	    		.data("moderators_list_name", moderatorsList)
	    		.data("moderators_user_list", moderators.getUsers().stream().map(u -> u.getScreenName()).collect(Collectors.joining(", ")))
	    		.data("presenter", presenter);
	}

}
