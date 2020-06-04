package org.ndx.retweet.by.consensus.bot.vote;

import org.ndx.retweet.by.consensus.bot.base.ElementProcessed;
import org.ndx.retweet.by.consensus.bot.twitter.tweet.AbstractStatusProcessed;

import com.fasterxml.jackson.annotation.JsonInclude;

import twitter4j.Status;

public class NotVotable extends AbstractStatusProcessed implements ElementProcessed {

	@JsonInclude public final String message;

	public NotVotable(Status status, String message) {
		super(status);
		this.message = message;
	}

}
