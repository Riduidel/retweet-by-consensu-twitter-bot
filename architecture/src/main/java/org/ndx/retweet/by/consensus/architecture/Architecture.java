package org.ndx.retweet.by.consensus.architecture;

import java.io.IOException;

import org.ndx.agile.architecture.base.AbstractArchitecture;

import com.structurizr.Workspace;
import com.structurizr.model.Container;
import com.structurizr.model.Location;
import com.structurizr.model.Model;
import com.structurizr.model.Person;
import com.structurizr.model.SoftwareSystem;
import com.structurizr.view.SystemContextView;
import com.structurizr.view.ViewSet;

public class Architecture extends AbstractArchitecture {

	/**
	 * Main method simply starts the {@link Architecture#run()} method after having injected all parameters
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws Throwable {
		AbstractArchitecture.main(Architecture.class, args);
	}

	/**
	 * Creates the workspace object and add in it both the architecture components
	 * AND the views used to display it
	 * 
	 * @return
	 */
	protected Workspace describeArchitecture() {
		Workspace workspace = new Workspace("Getting Started", "This is a model of my software system.");
		Model model = workspace.getModel();

		Person producer = model.addPerson("Producer", "Someone wanting to have a tweet displayed on @Presentation twitter account");
		Person moderator = model.addPerson("Moderator", "Someone voting for the tweets displayed on @Presentation twitter account");
		Person curator = model.addPerson("@Curator", "The @Curator twitter account allows voting");
		Person presentation= model.addPerson("@Presentation", "The @Presentation twitter account is where everything will show up");
		SoftwareSystem bot = model.addSoftwareSystem(Location.Internal, "retweet-by-consensus", 
				"Retweet by consensus bot proposing the votes and exposing their results");
		// Weirdly enough, this doesn't work
//		producer.delivers(curator, "Posts messages on twitter mentionning @Curator");
		bot.delivers(curator, "Read mentions to get latest rewteet requests");
		bot.delivers(moderator, "Asks moderator to vote on rewteet");
		moderator.uses(bot, "Validates retweet");
		bot.delivers(presentation, "Shows retweet when validated");
		bot.delivers(curator, "Presents vote results (who produced the message, who voted for the retweet)");

		ViewSet views = workspace.getViews();
		SystemContextView contextView = views.createSystemContextView(bot, "SystemContext",
				"An example of a System Context diagram.");
		contextView.addAllSoftwareSystems();
		contextView.addAllPeople();

//		Styles styles = views.getConfiguration().getStyles();
//		styles.addElementStyle(Tags.SOFTWARE_SYSTEM).background("#1168bd").color("#ffffff");
//		styles.addElementStyle(Tags.PERSON).background("#08427b").color("#ffffff").shape(Shape.Person);
		return workspace;
	}

}
