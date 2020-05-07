package org.ndx.retweet.by.consensus.bot.twitter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.pivovarit.function.ThrowingConsumer;
import com.pivovarit.function.ThrowingFunction;

import twitter4j.PagableResponseList;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.UserList;

/**
 * Facade for twitter list allowing us to limit the number of calls to Twitter
 * API
 * 
 * @author nicolas-delsaux
 *
 */
public class TwitterList {
	public final Twitter user;
	public final String name;
	private Optional<Set<User>> users = Optional.empty();
	private Optional<UserList> userList = Optional.empty();
	private Optional<TwitterList> included = Optional.empty();

	public TwitterList(Twitter curator, String moderatorsListName) {
		this.user = curator;
		this.name = moderatorsListName;
	}

	public TwitterList(Twitter curator, String producersListName, TwitterList moderators) {
		this(curator, producersListName);
	}

	public Set<User> getUsers() throws TwitterException {
		if (users.isEmpty()) {
			if (userList.isEmpty()) {
				userList = Optional.of(getListNamed(user, name));
			}
			// Now build user list by first fetching users, then adding included ones
			Set<User> toUse = new HashSet<>(toRealList(user, userList.get()));
			included.stream()
				.forEach(ThrowingConsumer.unchecked(list -> toUse.addAll(list.getUsers())));
			users = Optional.of(toUse);
		}
		return users.get();
	}

	static UserList getListNamed(Twitter curator, String listName) throws TwitterException {
		ResponseList<UserList> allLists = curator.getUserLists(curator.getId());
		for (UserList list : allLists) {
			if (list.getName().equals(listName)) {
				return list;
			}
		}
		throw new UnsupportedOperationException(String.format(
				"User \"%s\" has no list named \"%s\". This list is mandatory", curator.getScreenName(), listName));
	}

	public static List<User> toRealList(Twitter curator, UserList list) throws TwitterException {
		return toRealList(list.getMemberCount(),
				ThrowingFunction.unchecked(cursor -> curator.getUserListMembers(curator.getId(), list.getSlug(),
						Math.min(list.getMemberCount(), 5000 /* hard value defined by twitter4j */), cursor,
						true)));
	}

	/**
	 * Read a user list, whatever it come from
	 * 
	 * @param list
	 * @param listReader
	 * @return
	 * @throws TwitterException
	 */
	static List<User> toRealList(int memberCount, Function<Long, PagableResponseList<User>> listReader)
			throws TwitterException {
		List<User> returned = new ArrayList<>();
		long cursor = -1;
		while (returned.size() < memberCount) {
			PagableResponseList<User> members = listReader.apply(cursor);
			cursor = members.getNextCursor();
			members.forEach(m -> returned.add(m));
		}
		return returned;
	}
}