package com.codexsoft.sas.connections.iom.search.parsers;

import com.codexsoft.sas.connections.iom.search.models.SASDetailedObject;
import com.codexsoft.sas.parsers.IParser;
import com.codexsoft.sas.parsers.ParserBase;
import com.nerdforge.unxml.parsers.ObjectParser;
import com.nerdforge.unxml.parsers.builders.ObjectNodeParserBuilder;
import lombok.val;

import java.util.ArrayList;
import java.util.List;

public class SASDetailedObjectParser implements IParser<List<SASDetailedObject>> {
    public static class Parsing extends ParserBase<List<SASDetailedObject.Parsing>> {

        public ObjectParser<List<SASDetailedObject.Parsing>> getParser() {
            return getParsing().arr("Objects/*", getItemParserBuilder())
                    .as(SASDetailedObject.Parsing.class);
        }

        public ObjectNodeParserBuilder getItemParserBuilder() {
            SASObjectParser objectParser = new SASObjectParser();
            return objectParser.getItemParserBuilder()
                    .attribute("parsingAttributes", getParsing().arr("@*")
                            .attribute("key", getParsing().simple().nodeNameParser())
                            .attribute("value", getParsing().simple().textParser())
                    )
                    .attribute("parsingAssociations", getParsing().arr("*")
                            .attribute("key", getParsing().simple().nodeNameParser())
                            .attribute("value", getParsing().arr("*", objectParser.getItemParserBuilder()))
                    );
        }
    }


    public List<SASDetailedObject> parse(String input) {
    	val parser = new Parsing();
        val result = parser.parse(input);
        return new ArrayList<SASDetailedObject>() {{
            for (val item : result) {
                add(SASDetailedObject.fromParsing(item));
            }
        }};
    }
}

