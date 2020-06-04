package org.ndx.retweet.by.consensus.bot.twitter.message;

import org.ndx.retweet.by.consensus.bot.base.ElementProcessed;

import twitter4j.DirectMessage;

public class VoteCast extends AbstractDirectMessageProcessed implements ElementProcessed {

	public final boolean dmAuthorVote;

	public VoteCast(DirectMessage message, String screenName, String screenName2, boolean vote) {
		super(message, screenName, screenName2);
		this.dmAuthorVote = vote;
	}

}
