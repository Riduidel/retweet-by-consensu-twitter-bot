package org.ndx.retweet.by.consensus.bot.vote;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.ndx.retweet.by.consensus.bot.base.ElementProcessed;
import org.ndx.retweet.by.consensus.bot.twitter.TwitterList;
import org.ndx.retweet.by.consensus.bot.twitter.TwitterListsProducer;
import org.ndx.retweet.by.consensus.bot.twitter.TwitterProducer;
import org.ndx.retweet.by.consensus.bot.twitter.TwitterUtils;
import org.ndx.retweet.by.consensus.bot.twitter.message.NoNeedToProcess;
import org.ndx.retweet.by.consensus.bot.twitter.message.VoteAccepted;
import org.ndx.retweet.by.consensus.bot.twitter.message.VoteCast;

import com.pivovarit.function.ThrowingConsumer;
import com.structurizr.annotation.Component;
import com.structurizr.annotation.UsesComponent;

import twitter4j.DirectMessage;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * Voting machine creates and validates votes, on behalf of curator account.
 * @author nicolas-delsaux
 *
 */
@Component
@ApplicationScoped
public class VotingMachine {
    private static final String MESSAGE_ALREADY_VOTED = "Our moderators have already voted for %s.\nThanks however for your implication";
	private static final String MESSAGE_VOTE_REQUIRED = "Producer [@%s] wants to have tweet [%s] retweeted on account %s. Do you agree (y/n) ?";
	
	public static Pattern VOTE_REQUIRED_DETECTOR = Pattern.compile(
			MESSAGE_VOTE_REQUIRED.replace("[", "\\[")
				.replace("]", "\\]")
				.replace("(", "\\(")
				.replace(")", "\\)")
				.replace("?", "\\?")
				.replace("%s", "(.*)")
			);
	private static final Logger logger = Logger.getLogger(VotingMachine.class);
    @Inject @ConfigProperty(name="CURATOR_ACCOUNT") String curatorAccount;
	@Inject @Named(TwitterProducer.CURATOR) Twitter curator;
	@Inject @Named(TwitterProducer.PRESENTER) Twitter presenter;
	@UsesComponent(description = "Producers accounts are used here to make sure the message deserves a vote")
	@Inject @Named(TwitterListsProducer.PRODUCERS) TwitterList producers;
	@UsesComponent(description = "Messages are sent to each moderator for voting, and their responses are used to see if retweet should happen")
	@Inject @Named(TwitterListsProducer.MODERATORS) TwitterList moderators;

	@UsesComponent(description = "To store temporary vote state")
	@Inject VoteStorage storage;
	/**
	 * Cast a vote for given status if it has not yet been done.
	 * @param toVote status for which we want to vote.
	 * @param indicator status which directed us to the vote status. Its author will be mentionned in vote results.
	 * @throws TwitterException 
	 */
	public ElementProcessed maybeCreateVoteFor(Status toVote, Status indicator) throws TwitterException {
		// Immediatly favorite indicator tweet, to avoid chain of retweets (or conversations)
		if(!indicator.isFavorited()) {
			curator.createFavorite(indicator.getId());
		}
		if(toVote.isFavorited()) {
			// TODO If status has been favorited, it indicates we have already tried to cast a vote on it, so no more effort is needed.
			// Unless maybe sending a message to producer to indicate him we have already done that
			// But just send that message if vote has been cast more than one day ago, because it may otherwise be just a previous message fetching
			String message = String.format(
					MESSAGE_ALREADY_VOTED,
					TwitterUtils.toUrl(toVote));
			return new NotVotable(indicator, message);
		} else {
			// Check if indicator status author is in the producers list.
			User producer = indicator.getUser();
			if(producers.getUsers().contains(producer)) {
				// This is a new status to vote for.
				return new Votable(indicator, storage.store(createVote(toVote)));
			} else {
				return new NotVotable(indicator, String.format("User %s is not a know producer (he is not in producers list)", indicator.getUser().getScreenName()));
			}
		}
	}

	/**
	 * Sends a direct message to the given user.
	 * @param twitter account used
	 * @param user recipient of message
	 * @param message message text
	 */
	void sendMessageTo(Twitter twitter, User user, String message) {
		try {
			twitter.sendDirectMessage(user.getId(), message);
		} catch (TwitterException e) {
			logger.errorf("Unable to send message \n\"%s\""
					+ "\nFrom user \"%s\" to user \"%s\"."
					+ "\nTypical error source is that users don't follow each other. We will let you check that by yourself ...",
					message,
					curatorAccount,
					user.getScreenName());
		}
	}
	
	/**
	 * Create the vote object, and also sends DM to each moderator
	 * @param toVote status to vote on
	 * @return the vote object
	 * @throws TwitterException
	 */
	Vote createVote(Status toVote) throws TwitterException {
		Vote vote = new Vote(toVote.getId(), toVote.getUser().getId());
		// Ask to each moderator if that tweet should be displayed
		String message = String.format(MESSAGE_VOTE_REQUIRED,
				toVote.getUser().getScreenName(),
				TwitterUtils.toUrl(toVote),
				presenter.getScreenName()
				);
		moderators.getUsers().forEach(ThrowingConsumer.unchecked(m -> curator.sendDirectMessage(m.getId(), message)));
		// Mark it as favorite!
		curator.createFavorite(toVote.getId());
		// And finally memorize the tweet
		return vote;
	}

	/**
	 * Analyze if message is a vote one. Should depend upon the user locale and resource bundle (later)
	 * @param moderator 
	 * @param messageFromModerator
	 * @return
	 */
	public boolean isVoteMessage(User moderator, DirectMessage messageFromModerator) {
		return messageFromModerator.getText().toLowerCase().startsWith("y") ||
				messageFromModerator.getText().toLowerCase().startsWith("n");
	}

	public Optional<ElementProcessed> castVote(User moderator, DirectMessage botMessage) {
		String text = botMessage.getText();
		// message should be built according to format define around line 101
		// So it can be parsed, no ?
//		Pattern pattern = Pattern.compile(regex)
		return Optional.empty();
	}

	public boolean isCallForVoteMessage(String text) {
		return VOTE_REQUIRED_DETECTOR.matcher(text).matches();
	}

	public Optional<ElementProcessed> applyVote(User curator, User moderator, DirectMessage moderatorMessage,
			DirectMessage callForVote) throws NumberFormatException, TwitterException {
		String voteText = moderatorMessage.getText();
		if(voteText.toLowerCase().startsWith("y")) {
			return applyVote(curator, moderator, true, callForVote);
		} else if(voteText.toLowerCase().startsWith("n")) {
			return applyVote(curator, moderator, false, callForVote);
		} else {
			return Optional.of(new NoNeedToProcess(moderatorMessage,
					moderator.getScreenName(), curator.getScreenName(), 
					"A valid vote can only start by \"y\" or \"n\""));
		}
	}

	private Optional<ElementProcessed> applyVote(User curatorUser, User moderator, boolean vote, DirectMessage message) throws NumberFormatException, TwitterException {
		Matcher matcher = VOTE_REQUIRED_DETECTOR.matcher(message.getText());
		if(matcher.matches()) {
			String producer = matcher.group(1);
			String curatorName = matcher.group(3);
			if(message.getURLEntities().length>0) {
				// In fact, the link to the source tweet should be in the first (and only) entity
				String tweetUrl = message.getURLEntities()[0].getExpandedURL();
				String tweetId = tweetUrl.substring(tweetUrl.lastIndexOf('/')+1);
				Vote forTweet = storage.getVoteFor(tweetId);
				forTweet.vote(moderator, vote);
				if(isAccepted(forTweet)) {
					// Present the retweet
					presenter.retweetStatus(Long.valueOf(tweetId));
					// And expose the voting process
					curator.updateStatus(String.format(
							"We present retweet of %s, proposed by %s, and validated by %s",
							tweetUrl,
							producer,
							forTweet.acceptors()
							));
					return Optional.of(new VoteAccepted(message, moderator.getScreenName(), curatorUser.getScreenName(), forTweet));
				} else {
					return Optional.of(new VoteCast(message, moderator.getScreenName(), curatorUser.getScreenName(), vote));
				}
			}
			return Optional.of(new NoNeedToProcess(message,
					moderator.getScreenName(), curatorUser.getScreenName(), 
					"We couldn't find tweet id from message"));
		}
		return Optional.empty();
	}

	/**
	 * Vote is accepted when there are more than 50% moderators that validate the tweet
	 * @param forTweet
	 * @return
	 */
	private boolean isAccepted(Vote forTweet) {
		long count = moderators.getUsers().size();
		long accepting = forTweet.votes(true);
		return (accepting*1.0/count)>0.5;
	}
}
