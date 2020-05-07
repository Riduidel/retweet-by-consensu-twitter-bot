package org.ndx.retweet.by.consensus.bot;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.logging.Logger;
import org.ndx.retweet.by.consensus.bot.base.ElementProcessed;
import org.ndx.retweet.by.consensus.bot.twitter.TwitterProducer;
import org.ndx.retweet.by.consensus.bot.twitter.message.NoNeedToProcess;
import org.ndx.retweet.by.consensus.bot.vote.NotVotable;
import org.ndx.retweet.by.consensus.bot.vote.VotingMachine;

import com.pivovarit.function.ThrowingFunction;

import twitter4j.DirectMessage;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * This bean launches at sartup and read the curator timeline and mentions.
 * For each mention, if it is part of a conversation, 
 * the message this mention is a response to is consdered as the message to potentially retweet.
 * 
 * TODO add support for mention by direct messages
 * @author nicolas-delsaux
 *
 */
@ApplicationScoped
public class CuratorMessagesReader {
    private static final Logger logger = Logger.getLogger(CuratorMessagesReader.class);
    public @Inject @Named(TwitterProducer.CURATOR) Twitter curator;
    
    public @Inject VotingMachine votingMachine;
    
    public ElementProcessed readStatus(Status status) throws TwitterException {
		logger.infof("Curator is mentionned in %d: \"%s\"", status.getId(), status.getText());
		// So now we have to fetch the tweet this one is a reply-to
		long originalStatusId = status.getInReplyToStatusId();
		if(originalStatusId!=0) {
			ResponseList<Status> votedMessage = curator.lookup(originalStatusId);
			if(votedMessage.isEmpty()) {
				// TODO notify user that the message has no reference, so no possible vote
				return new NotVotable(status, String.format("Status is not a reply to another tweet, so we don't know what to retweet ..."));
			} else {
				return votingMachine.voteFor(votedMessage.get(0), status);
			}
		}
		return new NotVotable(status, String.format("Status has id 0?!"));
    }
    	

	public Stream<ElementProcessed> readStatuses() throws TwitterException {
		return curator.getMentionsTimeline().stream().parallel()
			.map(ThrowingFunction.lifted(this::readStatus))
			.filter(optional -> optional.isPresent())
			.map(optional -> optional.get())
			;
	}

	public List<ElementProcessed> readAll() throws TwitterException {
		return Stream.concat(readStatuses(), readMessages())
				.collect(Collectors.toList());
	}

	public ElementProcessed readMessage(DirectMessage message) throws TwitterException {
		User sender = curator.lookupUsers(message.getSenderId()).get(0);
		User recipient = curator.lookupUsers(message.getRecipientId()).get(0);
		return new NoNeedToProcess(message, 
				sender.getScreenName(),
				recipient.getScreenName(),
				"TODO");
	}

	private Stream<ElementProcessed> readMessages() throws TwitterException {
		return curator.getDirectMessages(50).stream().parallel()
				.map(ThrowingFunction.lifted(this::readMessage))
				.filter(optional -> optional.isPresent())
				.map(optional -> optional.get())
				;
	}
}
