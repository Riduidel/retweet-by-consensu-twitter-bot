package org.ndx.retweet.by.consensus.bot.vote;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A very simple vote storage mechanism based upon JSON mapping
 * Each vote will be stored in an independent file named from tweet id
 * @author nicolas-delsaux
 *
 */
@ApplicationScoped
public class VoteStorage {
	@Inject @ConfigProperty(name="FILE_STORAGE", defaultValue = "votes-storage") Optional<File> storage;
	@Inject ObjectMapper mapper;

	public Vote store(Vote vote)  {
		File storagePath = storage.orElseGet(() -> new File("votes-storage"));
		storagePath.mkdirs();
		File destination = new File(storagePath, String.format("%d.json", vote.tweetId));
		try {
			FileUtils.write(destination, mapper.writerFor(Vote.class).writeValueAsString(vote), "UTF-8");
			return vote;
		} catch (IOException e) {
			throw new RuntimeException(
					String.format("Unable to write JSON content into %s", destination.getAbsolutePath()), 
					e);
		}
	}

}
