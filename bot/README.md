# bot project

This module uses Quarkus, the Supersonic Subatomic Java Framework.

It provides all fetures of our application, which sole role is to receive webhooks from Twitter, and react to these webhooks.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```
./mvnw quarkus:dev
```

## Integration testing
Since this applications collaborates massively with Twitter API, 
it requires some Twitter configuration to run, which must be set in your `settings.xml`.
You should ask for application key/secret and for OAuth tokens of both **@Curator** and **@Presentation** twitter accounts.
Once you have them, fill the following XML fragment, put it in your `settings.xml` in a profile called `retweet-by-consensus-twitter-bot` which looks like this

```
		<profile>
			<id>settings-retweet-by-consensus</id>
			<properties>
				<TWITTER_API_KEY>TODO</TWITTER_API_KEY>
				<TWITTER_API_SECRET>TODO</TWITTER_API_SECRET>
				<TWITTER_ACCESS_TOKEN_KEY>TODO</TWITTER_ACCESS_TOKEN_KEY>
				<TWITTER_ACCESS_TOKEN_SECRET>TODO</TWITTER_ACCESS_TOKEN_SECRET>
			</properties>
		</profile>
```
 

## Packaging and running the application

The application can be packaged using `./mvnw package`.
It produces the `bot-1.0-SNAPSHOT-runner.jar` file in the `/target` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/lib` directory.

The application is now runnable using `java -jar target/bot-1.0-SNAPSHOT-runner.jar`.

## Creating a native executable

You can create a native executable using: `./mvnw package -Pnative`.

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: `./mvnw package -Pnative -Dquarkus.native.container-build=true`.

You can then execute your native executable with: `./target/bot-1.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/building-native-image.