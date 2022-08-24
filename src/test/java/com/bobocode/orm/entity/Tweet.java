package com.bobocode.orm.entity;

import com.bobocode.orm.annotation.Column;
import com.bobocode.orm.annotation.Id;
import com.bobocode.orm.annotation.Table;
import java.time.LocalDate;
import lombok.Data;

@Table(name = "tweets")
@Data
public class Tweet {

  @Id private Long id;

  @Column(name = "tweet_text")
  private String tweetText;

  @Column(name = "created_at")
  private LocalDate createdAt;
}
