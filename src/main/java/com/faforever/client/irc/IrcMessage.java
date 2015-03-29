package com.faforever.client.irc;

import java.time.Instant;

public class IrcMessage {

  private final Instant time;
  private final String nick;
  private final String message;

  public IrcMessage(Instant time, String nick, String message) {
    this.time = time;
    this.nick = nick;
    this.message = message;
  }

  public Instant getTime() {
    return time;
  }

  public String getNick() {
    return nick;
  }

  public String getMessage() {
    return message;
  }
}
