package org.ndx.retweet.by.consensus.bot.vote;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.structurizr.annotation.Component;

/**
 * A very simple vote storage mechanism based upon JSON mapping
 * Each vote will be stored in an independent file named from tweet id
 * @author nicolas-delsaux
 *
 */
@Component
@ApplicationScoped
public class VoteStorage {
	@Inject @ConfigProperty(name="FILE_STORAGE", defaultValue = "votes-storage") Optional<File> storage;
	@Inject ObjectMapper mapper;

	public Vote store(Vote vote)  {
		File destination = getStoragePath(Long.toString(vote.tweetId));
		try {
			mapper.writerFor(Vote.class).writeValues(destination).write(vote).close();
			return vote;
		} catch (IOException e) {
			throw new RuntimeException(
					String.format("Unable to write JSON content into %s", destination.getAbsolutePath()), 
					e);
		}
	}

	File getStoragePath(String tweetId) {
		File storagePath = getStorageFile();
		storagePath.mkdirs();
		File destination = new File(storagePath, String.format("%d.json", tweetId));
		return destination;
	}

	File getStorageFile() {
		return storage.orElseGet(() -> new File("votes-storage"));
	}

	public Vote getVoteFor(String tweetId) {
		File destination = getStoragePath(tweetId);
		try {
			return mapper.readerFor(Vote.class).readValue(destination);
		} catch (IOException e) {
			throw new RuntimeException(
					String.format("Unable to write JSON content into %s", destination.getAbsolutePath()), 
					e);
		}
	}

}
