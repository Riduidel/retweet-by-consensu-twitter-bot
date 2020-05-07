package org.ndx.retweet.by.consensus.bot.twitter.tweet;

import java.util.Date;

import org.ndx.retweet.by.consensus.bot.twitter.TwitterUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import twitter4j.Status;

public class AbstractStatusProcessed {
	@JsonIgnore private final Status status;
	
	@JsonInclude public final String statusText;
	@JsonInclude public final String statusUrl;
	@JsonInclude public final String statusAuthor;
	@JsonInclude private Date statusDate;

	public AbstractStatusProcessed(Status status) {
		super();
		this.status = status;
		this.statusText = status.getText();
		this.statusUrl = TwitterUtils.toUrl(status);
		this.statusDate = status.getCreatedAt();
		this.statusAuthor = status.getUser().getScreenName();
	}
}
