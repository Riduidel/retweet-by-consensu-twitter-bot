package org.ndx.retweet.by.consensus.bot.vote;

import java.util.Set;

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

import com.pivovarit.function.ThrowingConsumer;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * Voting machine creates and validates votes, on behalf of curator account.
 * @author nicolas-delsaux
 *
 */
@ApplicationScoped
public class VotingMachine {
    private static final Logger logger = Logger.getLogger(VotingMachine.class);
    @Inject @ConfigProperty(name="CURATOR_ACCOUNT") String curatorAccount;
	@Inject @Named(TwitterProducer.CURATOR) Twitter curator;
	@Inject @Named(TwitterProducer.PRESENTER) Twitter presenter;
	@Inject @Named(TwitterListsProducer.PRODUCERS) TwitterList producers;
	@Inject @Named(TwitterListsProducer.MODERATORS) TwitterList moderators;

	@Inject VoteStorage storage;
	/**
	 * Cast a vote for given status if it has not yet been done.
	 * @param toVote status for which we want to vote.
	 * @param indicator status which directed us to the vote status. Its author will be mentionned in vote results.
	 * @throws TwitterException 
	 */
	public ElementProcessed voteFor(Status toVote, Status indicator) throws TwitterException {
		// Immediatly favorite indicator tweet, to avoid chain of retweets (or conversations)
		if(!indicator.isFavorited()) {
			curator.createFavorite(indicator.getId());
		}
		if(toVote.isFavorited()) {
			// TODO If status has been favorited, it indicates we have already tried to cast a vote on it, so no more effort is needed.
			// Unless maybe sending a message to producer to indicate him we have already done that
			// But just send that message if vote has been cast more than one day ago, because it may otherwise be just a previous message fetching
			String message = String.format(
					"Our moderators have already voted for %s.\n"
					+ "Thanks however for your implication",
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
	
	Vote createVote(Status toVote) throws TwitterException {
		Vote vote = new Vote(toVote.getId(), toVote.getUser().getId());
		// Ask to each moderator if that tweet should be displayed
		String message = String.format("Producer [@%s] wants to have tweet [%s] retweeted on account %s. "
				+ "Do you agree (y/n) ?",
				toVote.getUser().getName(),
				TwitterUtils.toUrl(toVote),
				presenter.getScreenName()
				);
		moderators.getUsers().forEach(ThrowingConsumer.unchecked(m -> curator.sendDirectMessage(m.getId(), message)));
		// Mark it as favorite!
		curator.createFavorite(toVote.getId());
		// And finally memorize the tweet
		return vote;
	}
}
