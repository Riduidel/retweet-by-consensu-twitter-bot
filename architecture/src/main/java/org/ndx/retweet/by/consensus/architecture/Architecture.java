package org.ndx.retweet.by.consensus.architecture;

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;

import org.ndx.agile.architecture.base.AbstractArchitecture;

import com.structurizr.Workspace;
import com.structurizr.analysis.ComponentFinder;
import com.structurizr.analysis.SourceCodeComponentFinderStrategy;
import com.structurizr.analysis.StructurizrAnnotationsComponentFinderStrategy;
import com.structurizr.model.Container;
import com.structurizr.model.Location;
import com.structurizr.model.Model;
import com.structurizr.model.Person;
import com.structurizr.model.SoftwareSystem;
import com.structurizr.model.Tags;
import com.structurizr.view.ComponentView;
import com.structurizr.view.ContainerView;
import com.structurizr.view.Shape;
import com.structurizr.view.Styles;
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
		producer.interactsWith(curator, "Posts messages on twitter mentionning @Curator");
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
		
		// Detail the architecture of the bot by depending upon it and analyzing it
		Container container = bot.addContainer("bot", "The bot code lives here", "Java/Quarkus");
		container.delivers(curator, "Read mentions to get latest rewteet requests");
		container.delivers(moderator, "Asks moderator to vote on rewteet");
		container.delivers(presentation, "Shows retweet when validated");
		container.delivers(curator, "Presents vote results (who produced the message, who voted for the retweet)");

		ContainerView containerView = views.createContainerView(bot, "bot-container", "description of the bot software system");
		containerView.add(container);
		containerView.add(curator);
		containerView.add(presentation);
		// Now details the various components from the code annotations
		try {
			ComponentFinder componentFinder = new ComponentFinder(
				    container, "org.ndx.retweet.by.consensus.bot",
				    new StructurizrAnnotationsComponentFinderStrategy(),
				    new SourceCodeComponentFinderStrategy(new File("../bot/src/main/java"))
				    );
			if(getClass().getClassLoader() instanceof URLClassLoader) {
				componentFinder.setUrlClassLoader((URLClassLoader) getClass().getClassLoader());
			}
			componentFinder.findComponents();
			ComponentView componentsView = views.createComponentView(container, "bot-components", "Description of the various bot components");
			componentsView.addAllComponents();
		} catch(Exception e) {
			throw new RuntimeException("Unable to read internal components of "+container);
		}
		
		
		Styles styles = views.getConfiguration().getStyles();
		styles.addElementStyle(Tags.SOFTWARE_SYSTEM).background("#1168bd").color("#ffffff");
		styles.addElementStyle(Tags.PERSON).background("#08427b").color("#ffffff").shape(Shape.Person);
		return workspace;
	}

}
