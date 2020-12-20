package com.codexsoft.sas.utils.functional;

/**
 * Created by eugene on 12.6.17.
 */
@FunctionalInterface
public interface CheckedFunction1<T, R> {
    R apply(T t) throws Exception;
}

