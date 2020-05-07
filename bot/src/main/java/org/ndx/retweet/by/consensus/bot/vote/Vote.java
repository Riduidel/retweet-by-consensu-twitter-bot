package org.ndx.retweet.by.consensus.bot.vote;

import java.util.Map;
import java.util.TreeMap;

public class Vote {
	public final long producerId;
	public final long tweetId;
	private Map<Long, Boolean> results = new TreeMap<>();
	public Vote(long tweetId, long producerId) {
		this.tweetId = tweetId;
		this.producerId = producerId;
	}
}
