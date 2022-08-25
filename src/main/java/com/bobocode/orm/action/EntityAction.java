package com.bobocode.orm.action;

public interface EntityAction {

  void execute();

  int priority();
}
