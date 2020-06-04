package org.ndx.retweet.by.consensus.bot.twitter;

import twitter4j.Status;

public class TwitterUtils {

	public static String toUrl(Status status) {
		return String.format("https://twitter.com/%s/statuses/%s", 
				status.getUser().getId(), status.getId());
	}

}
