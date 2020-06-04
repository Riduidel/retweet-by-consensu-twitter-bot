package org.ndx.retweet.by.consensus.bot.twitter.message;

import org.ndx.retweet.by.consensus.bot.base.ElementProcessed;

import com.fasterxml.jackson.annotation.JsonInclude;

import twitter4j.DirectMessage;

public class NoNeedToProcess extends AbstractDirectMessageProcessed implements ElementProcessed {
	@JsonInclude public final String message;

	public NoNeedToProcess(DirectMessage dm, String author, String recipient, String message) {
		super(dm, author, recipient);
		this.message = message;
	}

}
