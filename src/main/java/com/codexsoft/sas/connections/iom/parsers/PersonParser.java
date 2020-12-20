package com.codexsoft.sas.connections.iom.parsers;

import com.codexsoft.sas.connections.iom.models.Person;
import com.codexsoft.sas.parsers.ParserBase;
import com.nerdforge.unxml.Parsing;
import com.nerdforge.unxml.parsers.ObjectParser;
import com.nerdforge.unxml.parsers.builders.ObjectNodeParserBuilder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PersonParser extends ParserBase<List<Person>> {
    public PersonParser() {}

    public PersonParser(Parsing parsing) {
        super(parsing);
    }

    public ObjectParser<List<Person>> getParser() {
        return getParsing().arr("Objects/Person", getItemParserBuilderAssoc())
            .as(Person.class);
    }

    public ObjectParser<Person> getItemParser() {
        return getItemParserBuilderAssoc().as(Person.class);
    }

    protected ObjectNodeParserBuilder getItemParserBuilderAssoc() {
        String roleTemplate = "IdentityGroups/IdentityGroup[@PublicType='%s']";
        return getItemParserBuilder()
            .attribute("groups", getParsing().arr(
                String.format(roleTemplate, IdentityGroupParser.PUBLIC_TYPE_GROUP),
                new IdentityGroupParser(getParsing()).getItemParserBuilder()))
            .attribute("roles", getParsing().arr(
                String.format(roleTemplate, IdentityGroupParser.PUBLIC_TYPE_ROLE),
                new IdentityGroupParser(getParsing()).getItemParserBuilder()))
            ;
    }

    public ObjectNodeParserBuilder getItemParserBuilder() {
        return getParsing().obj()
            .attribute("id", "@Id")
            .attribute("name", "@Name")
            .attribute("displayName", "@DisplayName")
            .attribute("isHidden", "@IsHidden")
            .attribute("publicType", "@PublicType");
    }
}
