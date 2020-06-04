package org.ndx.retweet.by.consensus.bot.twitter;

import java.util.Map;
import java.util.TreeMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.structurizr.annotation.Component;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * A cache of users, allowing us to limit queries to twitter, because we're nice boys
 * @author nicolas-delsaux
 *
 */
@Component
@ApplicationScoped
public class TwitterUserCache {
	Map<Long, User> users = new TreeMap<>();

	@Inject @Named(TwitterProducer.CURATOR) Twitter twitter;

	public User getUserFor(long senderId) throws TwitterException {
		if(!users.containsKey(senderId)) {
			users.put(senderId, twitter.lookupUsers(senderId).get(0));
		}
		return users.get(senderId);
	}
}
