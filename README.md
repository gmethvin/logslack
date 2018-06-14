# Logslack - Slack Logback appender

Logslack is a Logback appender that posts log messages to Slack and allows selectively directing log messages to Slack channels by passing a `Marker`. The library is written in Scala using an Akka HTTP client to make requests asynchronously to the Slack API.

## Dependency

In sbt:

```scala
libraryDependencies += "io.methvin" %% "logslack" % logslackVersion
```

Replace the `logslackVersion` with the latest version ([![Maven](https://img.shields.io/maven-central/v/io.methvin/logslack_2.12.svg)](https://mvnrepository.com/artifact/io.methvin/logslack)), or any older version you wish to use.

## Configuration

To enable, add the appender to your `logback.xml`, providing at least a `token`:

```xml
<configuration>
  <appender name="SLACK" class="io.methvin.logback.SlackAppender">
    <!-- Your slack token (required). This must have at least the "chat.write.bot" permission. -->
    <token>YOUR_SLACK_TOKEN</token>

    <!-- If you set the channel option, ALL logs will go to this channel -->
    <!-- <channel>#general-logs</channel> -->

    <!-- The emoji to use as the icon for this message  -->
    <!-- <iconEmoji>logger</iconEmoji> -->

    <!-- The bot's user name -->
    <!-- <username>logger</username> -->
  </appender>

  <root level="ERROR">
    <appender-ref ref="SLACK" />
  </root>
</configuration>
```

## Usage

With the above configuration, logslack will not post to any slack channels by default. You can create a SLF4J `Marker` to tag a log message for a specific channel. For example:

```scala
object SlackMarkers {
  final val GeneralAlerts = MarkerFactory.getMarker("#general-alerts")
}
```

The name of the marker should be the same as the channel you wish to post to.

Then specify the marker when logging:

```scala
// for a standard SLF4J logger:
logger.info(SlackMarkers.GeneralAlerts, "Something happened!")

// for Play logger:
logger.info("Something happened!")(SlackMarkers.GeneralAlerts)
```

To log all messages in some channel, set the `channel` option in the configuration, described above.
