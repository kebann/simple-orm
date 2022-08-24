package com.bobocode.orm.entity;

import com.bobocode.orm.annotation.Id;
import com.bobocode.orm.annotation.Table;
import lombok.Data;

@Table(name = "users")
@Data
public class User {

  @Id private Long id;
  private String name;
  private String handle;
}
