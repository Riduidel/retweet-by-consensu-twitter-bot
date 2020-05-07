package org.ndx.retweet.by.consensus.bot.vote;

import org.ndx.retweet.by.consensus.bot.base.ElementProcessed;
import org.ndx.retweet.by.consensus.bot.twitter.tweet.AbstractStatusProcessed;

import com.fasterxml.jackson.annotation.JsonInclude;

import twitter4j.Status;

public class Votable extends AbstractStatusProcessed implements ElementProcessed {

	@JsonInclude public final Vote vote;

	public Votable(Status indicator, Vote store) {
		super(indicator);
		this.vote = store;
	}

}
