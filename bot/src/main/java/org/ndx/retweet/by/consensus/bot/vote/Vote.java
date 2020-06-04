package org.ndx.retweet.by.consensus.bot.vote;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import twitter4j.User;

public class Vote {
	public final long producerId;
	public final long tweetId;
	private Map<String, Boolean> results = new TreeMap<>();
	public Vote(long tweetId, long producerId) {
		this.tweetId = tweetId;
		this.producerId = producerId;
	}
	public void vote(User moderator, boolean vote) {
		results.put(moderator.getScreenName(), vote);
	}
	public Long votes(boolean b) {
		return results.entrySet().stream()
				.filter(entry -> entry.getValue())
				.collect(Collectors.counting());
	}
	public String acceptors() {
		return results.entrySet().stream()
				.filter(entry -> entry.getValue())
				.map(entry -> entry.getKey())
				.map(user -> "@"+user)
				.collect(Collectors.joining(", "));
	}
}
