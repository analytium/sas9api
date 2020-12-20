package com.codexsoft.sas.utils.functional;

/**
 * Created by eugene on 18.7.17.
 */
@FunctionalInterface
public interface CheckedFunction2<T1, T2, R> {
    R apply(T1 t1, T2 t2) throws Exception;
}

