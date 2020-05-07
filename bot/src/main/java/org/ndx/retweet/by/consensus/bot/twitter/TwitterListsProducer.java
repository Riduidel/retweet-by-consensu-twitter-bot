package org.ndx.retweet.by.consensus.bot.twitter;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import twitter4j.Twitter;
import twitter4j.TwitterException;

public class TwitterListsProducer {
	public static final String PRODUCERS = "producers";

	public static final String MODERATORS = "moderators";

	@Produces
	@ApplicationScoped
	@Named(PRODUCERS)
	TwitterList createProducersList(@Named(TwitterProducer.CURATOR) Twitter curator,
			@ConfigProperty(name = "PRODUCERS_LIST", defaultValue = "producers") String producersListName,
			@Named(MODERATORS) TwitterList moderators) throws TwitterException {
		return new TwitterList(curator, producersListName, moderators);
	}

	@Produces
	@ApplicationScoped
	@Named(MODERATORS)
	TwitterList createModeratorsList(@Named(TwitterProducer.CURATOR) Twitter curator,
			@ConfigProperty(name = "MODERATORS_LIST", defaultValue = "moderators") String moderatorsListName)
			throws TwitterException {
		return new TwitterList(curator, moderatorsListName);
	}
}
