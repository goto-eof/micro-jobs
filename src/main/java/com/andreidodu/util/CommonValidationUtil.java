package com.andreidodu.util;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class CommonValidationUtil {
    public static Predicate<Object> isNull = Objects::isNull;
    public static BiPredicate<Long, Long> isSameId = Long::equals;
}
