package org.ndx.retweet.by.consensus.bot.twitter.message;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import twitter4j.DirectMessage;

public abstract class AbstractDirectMessageProcessed {
	@JsonIgnore public final DirectMessage dm;
	@JsonInclude public final String dmText;
	@JsonInclude public final String dmAuthor;
	@JsonInclude public final Date dmDate;
	@JsonInclude public final String dmRecipient;


	public AbstractDirectMessageProcessed(DirectMessage message, String author, String recipient) {
		super();
		this.dm = message;
		this.dmText = message.getText();
		this.dmAuthor = author;
		this.dmRecipient = recipient;
		this.dmDate = message.getCreatedAt();
	}
}
