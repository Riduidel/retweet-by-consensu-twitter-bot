package org.ndx.retweet.by.consensus.bot;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.logging.Logger;
import org.ndx.retweet.by.consensus.bot.base.ElementProcessed;
import org.ndx.retweet.by.consensus.bot.twitter.TwitterList;
import org.ndx.retweet.by.consensus.bot.twitter.TwitterListsProducer;
import org.ndx.retweet.by.consensus.bot.twitter.TwitterProducer;
import org.ndx.retweet.by.consensus.bot.twitter.TwitterUserCache;
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
 * This bean launches at startup and read the curator timeline and mentions.
 * For each mention, if it is part of a conversation, 
 * the message this mention is a response to is considered as the message to potentially retweet.
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
    
    public @Inject TwitterUserCache userCache;
    
    public @Inject @Named(TwitterListsProducer.PRODUCERS) TwitterList producers;
    
    public @Inject @Named(TwitterListsProducer.MODERATORS) TwitterList moderators;
    
    /**
     * We read messages before statuses because statuses will trigger new votes, potentially
     * burrowing moderator votes under a pile a new list of request for votes
     * @return
     * @throws TwitterException
     */
    public List<ElementProcessed> readAll() throws TwitterException {
    	return Stream.concat(
    			readMessages(),
    			readStatuses()
    			)
    			.collect(Collectors.toList());
    }

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
				return votingMachine.maybeCreateVoteFor(votedMessage.get(0), status);
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

	private Stream<ElementProcessed> readMessages() throws TwitterException {
		Map<User, SortedSet<DirectMessage>> conversations = getConversations();
		Map<String, List<Entry<User, SortedSet<DirectMessage>>>> conversationsByRoles = conversations.entrySet().stream()
			.collect(Collectors.groupingBy(entry -> {
				if(moderators.getUsers().contains(entry.getKey())) {
					return TwitterListsProducer.MODERATORS;
				} else if(producers.getUsers().contains(entry.getKey())) {
					return TwitterListsProducer.PRODUCERS;
				} else {
					return "not in a list";
				}
			}));
		return Stream.concat(
				readModeratorsConversations(conversationsByRoles.getOrDefault(TwitterListsProducer.MODERATORS, Collections.emptyList())),
				readProducersConversations(conversationsByRoles.getOrDefault(TwitterListsProducer.PRODUCERS, Collections.emptyList()))
				);
	}


	private Stream<ElementProcessed> readProducersConversations(List<Entry<User, SortedSet<DirectMessage>>> producersConversations) {
		// TODO support cc by private message
		List<ElementProcessed> list = Collections.emptyList();
		return list.stream();
	}


	private Stream<ElementProcessed> readModeratorsConversations(List<Entry<User, SortedSet<DirectMessage>>> moderatorsConversations) {
		return moderatorsConversations.stream()
				.map(ThrowingFunction.lifted(entry -> readModeratorConversation(entry.getKey(), entry.getValue())))
				// First optional pair is the lifted one
				.filter(optional -> optional.isPresent())
				.map(optional -> optional.get())
				// Second optional pair is given by the readModeratorConversation
				.filter(optional -> optional.isPresent())
				.map(optional -> optional.get())
				;
	}

	private Optional<ElementProcessed> readModeratorConversation(User moderator, SortedSet<DirectMessage> conversation) throws TwitterException {
		// Get last message in conversation
		SortedSet<DirectMessage> interestingPart = findInterestingPartOfConversation(moderator.getId(), conversation);
		if(!interestingPart.isEmpty()) {
			DirectMessage moderatorMessage = interestingPart.last();
			if(votingMachine.isVoteMessage(moderator, moderatorMessage)) {
				Optional<DirectMessage> callForVote = interestingPart.stream()
					.filter(dm -> votingMachine.isCallForVoteMessage(dm.getText()))
					.reduce((first, second) -> second);
				if(callForVote.isPresent()) {
					return votingMachine.applyVote(userCache.getUserFor(moderatorMessage.getRecipientId()),
							moderator, moderatorMessage, callForVote.get());
				}
			}
		}
		return Optional.empty();
	}


	private SortedSet<DirectMessage> findInterestingPartOfConversation(long id, SortedSet<DirectMessage> conversation) {
		if(conversation.isEmpty()) {
			return conversation;
		} else {
			DirectMessage last = conversation.last();
			if(last.getSenderId()==id)
				return conversation;
			else
				return conversation.headSet(last);
		}
	}

	Map<User, SortedSet<DirectMessage>> getConversations() throws TwitterException {
		Map<User, SortedSet<DirectMessage>> conversations = new HashMap<>();
		for(DirectMessage d : curator.getDirectMessages(50)) {
			User sender = userCache.getUserFor(d.getSenderId());
			User recipient = userCache.getUserFor(d.getRecipientId());
			User conversedWith = null;
			if(sender.getId()!=curator.getId()) {
				conversedWith = sender;
			}
			if(recipient.getId()!=curator.getId()) {
				conversedWith = recipient;
			}
			if(!conversations.containsKey(conversedWith)) {
				conversations.put(conversedWith, new TreeSet<>(
						Comparator.comparing(dm -> dm.getCreatedAt()))
						);
			}
			conversations.get(conversedWith).add(d);
		}
		return conversations;
	}
}
