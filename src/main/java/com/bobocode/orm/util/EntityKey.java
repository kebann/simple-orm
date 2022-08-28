package com.bobocode.orm.util;

import lombok.NonNull;

public record EntityKey<T>(@NonNull Class<T> type, @NonNull Object id) {}
