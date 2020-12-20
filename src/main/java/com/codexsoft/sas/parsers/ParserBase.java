package com.codexsoft.sas.parsers;

import com.nerdforge.unxml.Parsing;
import com.nerdforge.unxml.factory.ParsingFactory;
import com.nerdforge.unxml.parsers.ObjectParser;
import com.nerdforge.unxml.parsers.builders.ObjectNodeParserBuilder;
import org.w3c.dom.Document;

/**
 * Created by eugene on 18.7.17.
 */
public abstract class ParserBase<T> implements IParser<T> {
    private Parsing parsing;
    private ObjectParser<T> parser;

    public ParserBase() {
        this.parsing = ParsingFactory.getInstance().create();
    }

    public ParserBase(Parsing parsing) {
        this.parsing = parsing;
    }

    protected Parsing getParsing() {
        return parsing;
    }

    protected abstract ObjectParser<T> getParser();

    private ObjectParser<T> getParserCached() {
        if (parser == null) {
            parser = getParser();
        }
        return parser;
    }

    protected ObjectNodeParserBuilder getItemParserBuilder() { return null; }

    public T parse(String input) {
        Document document = parsing.xml().document(input);
        return getParserCached().apply(document);
    }
}
