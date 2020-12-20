package com.codexsoft.sas.connections.iom.search.parsers;

import com.codexsoft.sas.connections.iom.search.models.SASObject;
import com.codexsoft.sas.parsers.ParserBase;
import com.nerdforge.unxml.parsers.ObjectParser;
import com.nerdforge.unxml.parsers.builders.ObjectNodeParserBuilder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SASObjectParser extends ParserBase<List<SASObject>> {
    public ObjectParser<List<SASObject>> getParser() {
        return getParsing().arr("Objects/*", getItemParserBuilder())
                .as(SASObject.class);
    }

    public ObjectNodeParserBuilder getItemParserBuilder() {
        return getParsing().obj()
                .attribute("type", getParsing().simple().nodeNameParser())
                .attribute("name", "@Name")
                .attribute("id", "@Id");
    }
}
