package org.ndx.retweet.by.consensus.bot.twitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Optional;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import com.pivovarit.function.ThrowingConsumer;

import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.UserList;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterProducer {

	public static final String DEFAULT = "@nobody";
	
	public static final String CURATOR = "@Curator";
	
	public static final String PRESENTER = "@Presenter";

	@Produces Configuration createTwitterConfiguration(
			@ConfigProperty(name = "TWITTER_API_KEY") String applicationKey,
			@ConfigProperty(name = "TWITTER_API_SECRET") String applicationSecret,
			@ConfigProperty(name = "TWITTER_TOKEN_KEY") String apiKey,
			@ConfigProperty(name = "TWITTER_TOKEN_SECRET") String apiSecret
			) {
		ConfigurationBuilder builder =  new ConfigurationBuilder();
		builder.setOAuthConsumerKey(applicationKey);
		builder.setOAuthConsumerSecret(applicationSecret);
//		builder.setOAuthAccessToken(apiKey);
//		builder.setOAuthAccessTokenSecret(apiSecret);
		return builder.build();
	}

	@Produces @Named(DEFAULT) Twitter createTwitterObject(Configuration configuration) {
		return new TwitterFactory(configuration).getInstance();
	}

	@Produces @Named(CURATOR) Twitter createTwitterForCurator(@Named(DEFAULT) Twitter twitter,
			@ConfigProperty(name="CURATOR_ACCOUNT") String curator,
			@ConfigProperty(name = "CURATOR_TOKEN_KEY") Optional<String> userKey,
			@ConfigProperty(name = "CURATOR_TOKEN_SECRET") Optional<String> userSecret
			) throws Exception{
		return createTwitterForAccount(twitter, curator, userKey, userSecret);
	}

	@Produces @Named(PRESENTER) Twitter createTwitterForPresenter(@Named(DEFAULT) Twitter twitter,
			@ConfigProperty(name="PRESENTER_ACCOUNT") String curator,
			@ConfigProperty(name = "PRESENTER_TOKEN_KEY") Optional<String> userKey,
			@ConfigProperty(name = "PRESENTER_TOKEN_SECRET") Optional<String> userSecret
			) throws Exception{
		return createTwitterForAccount(twitter, curator, userKey, userSecret);
	}
	
	Twitter createTwitterForAccount(Twitter twitter, 
			String accountName, 
			Optional<String> userKey, 
			Optional<String> userSecret) throws Exception {
		if(userKey.isPresent() && userSecret.isPresent()) {
			AccessToken token = new AccessToken(userKey.get(), userSecret.get());
			twitter.setOAuthAccessToken(token);
			return twitter;
		} else {
			AccessToken token = createAccessToken(twitter, accountName);
			twitter.setOAuthAccessToken(token);
			String message = String.format("An access token has been generated for this user."
					+ "You now have to edit configuration.\n"
					+ "Access token key: \"%s\"\n"
					+ "Access token secret: \"%s\"", token.getToken(), token.getTokenSecret());
			// Send access token infos as private message, to make sure communication works the intended way
/*			moderators
				.stream().forEach(
					ThrowingConsumer.unchecked(
					m -> twitter.sendDirectMessage(m, message)));
*/			
			return twitter;
		}
	}

	private AccessToken createAccessToken(Twitter twitter, String user) throws Exception{
	    RequestToken requestToken = twitter.getOAuthRequestToken();
	    AccessToken accessToken = null;
	    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	    while (null == accessToken) {
	      System.out.println("Open the following URL and grant access to your account:");
	      System.out.println(requestToken.getAuthorizationURL());
	      System.out.print("Enter the PIN(if aviailable) or just hit enter.[PIN]:");
	      String pin = br.readLine();
	      try{
	         if(pin.length() > 0){
	           accessToken = twitter.getOAuthAccessToken(requestToken, pin);
	         }else{
	           accessToken = twitter.getOAuthAccessToken();
	         }
	      } catch (TwitterException te) {
	        if(401 == te.getStatusCode()){
	          System.out.println("Unable to get the access token.");
	        }else{
	          te.printStackTrace();
	        }
	      }
	    }
		return accessToken;
	}
}
