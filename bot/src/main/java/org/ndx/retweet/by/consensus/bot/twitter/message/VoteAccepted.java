package org.ndx.retweet.by.consensus.bot.twitter.message;

import org.ndx.retweet.by.consensus.bot.base.ElementProcessed;
import org.ndx.retweet.by.consensus.bot.vote.Vote;

import twitter4j.DirectMessage;

public class VoteAccepted extends AbstractDirectMessageProcessed implements ElementProcessed {

	public final Vote vote;

	public VoteAccepted(DirectMessage message, String screenName, String screenName2, Vote vote) {
		super(message, screenName, screenName2);
		this.vote = vote;
	}

}
