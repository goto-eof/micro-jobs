package com.andreidodu.util;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class CommonValidationUtil {
    public static Predicate<Object> isNull = value -> value == null;
    public static BiPredicate<Long, Long> isSameId = (id1, id2) -> id1.equals(id2);
}
