package com.codexsoft.sas.parsers;

public interface IParser<T> {
    T parse(String input);
}
