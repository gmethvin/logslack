# Logslack: Logback appender for Slack

[![Travis CI](https://travis-ci.org/gmethvin/logslack.svg?branch=master)](https://travis-ci.org/gmethvin/logslack) [![Maven](https://img.shields.io/maven-central/v/io.methvin/logslack_2.12.svg)](https://mvnrepository.com/artifact/io.methvin/logslack)

Logslack is a Logback appender that posts log messages to Slack. Since you generally don't want to send all logs to Slack, it allows selectively directing log messages to Slack channels by passing a `Marker`.

The library is written in Scala, using an Akka HTTP client to make requests asynchronously to the Slack API.

## Dependency

In sbt:

```scala
libraryDependencies += "io.methvin" %% "logslack" % logslackVersion
```

In maven:

```xml
<dependency>
    <groupId>io.methvin</groupId>
    <artifactId>logslack_2.12</artifactId>
    <version>${logslackVersion}</version>
</dependency>
```

Replace the `logslackVersion` with the version ([![Maven](https://img.shields.io/maven-central/v/io.methvin/logslack_2.12.svg)](https://mvnrepository.com/artifact/io.methvin/logslack)).

## Configuration

To enable, add the appender to your `logback.xml`, providing at least a `token`:

```xml
<configuration>
  <appender name="SLACK" class="io.methvin.logback.SlackAppender">
    <!-- The app's slack API token. This must have at least the "chat.write.bot" permission. -->
    <token>YOUR_SLACK_TOKEN</token>

    <!-- If you set the channel option, ALL logs will go to this channel. -->
    <!-- <channel>#application-logs</channel> -->

    <!-- The emoji to use as the icon for this message (must start and end in a colon) -->
    <!-- <iconEmoji>:shrug:</iconEmoji> -->

    <!-- The bot's username. -->
    <!-- <username>logger</username> -->

    <!-- Whether Slack should use markdown to process the log messages. Defaults to true. -->
    <!-- <useMarkdown>false</useMarkdown> -->

    <!-- Formatting -->
    <layout class="ch.qos.logback.classic.PatternLayout">
      <pattern>%-4relative [%thread] %-5level %class - %msg%n</pattern>
    </layout>
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

To log all messages in some channel, set the `channel` option in the configuration, described in the configuration section above.
