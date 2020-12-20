package com.codexsoft.sas.utils.functional;

/**
 * Created by eugene on 18.7.17.
 */
@FunctionalInterface
public interface CheckedFunction0<R> {
    R apply() throws Exception;
}
