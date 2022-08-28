package com.bobocode.orm.entity;

import com.bobocode.orm.annotation.Id;
import com.bobocode.orm.annotation.Table;
import lombok.Data;
import lombok.experimental.Accessors;

@Table(name = "users")
@Data
@Accessors(chain = true)
public class User {

  @Id private Long id;
  private String name;
  private String handle;
}
