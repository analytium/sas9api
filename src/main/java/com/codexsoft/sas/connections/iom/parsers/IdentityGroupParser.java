package com.codexsoft.sas.connections.iom.parsers;

import com.codexsoft.sas.connections.iom.models.IdentityGroup;
import com.codexsoft.sas.parsers.ParserBase;
import com.nerdforge.unxml.Parsing;
import com.nerdforge.unxml.parsers.ObjectParser;
import com.nerdforge.unxml.parsers.builders.ObjectNodeParserBuilder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IdentityGroupParser extends ParserBase<List<IdentityGroup>> {
    public static final String PUBLIC_TYPE_ROLE = "Role";
    public static final String PUBLIC_TYPE_GROUP = "UserGroup";

    public IdentityGroupParser() {}

    public IdentityGroupParser(Parsing parsing) {
        super(parsing);
    }

    public ObjectParser<List<IdentityGroup>> getParser() {
        return getParsing().arr("Objects/IdentityGroup", getItemParserBuilderAssoc())
                .as(IdentityGroup.class);
    }

    public ObjectParser<IdentityGroup> getItemParser() {
        return getItemParserBuilderAssoc().as(IdentityGroup.class);
    }

    protected ObjectNodeParserBuilder getItemParserBuilderAssoc() {
        String identityTemplate = "MemberIdentities/IdentityGroup[@PublicType='%s']";
        return getItemParserBuilder()
            .attribute("groups", getParsing().arr(
                    String.format(identityTemplate, PUBLIC_TYPE_GROUP),
                    // create a new base parser to make the parsing non-recursive
                    // use the same instance of parser for recursive parsing
                    getItemParserBuilder()))
            .attribute("roles", getParsing().arr(
                    String.format(identityTemplate, PUBLIC_TYPE_ROLE),
                    getItemParserBuilder()))
            .attribute("users", getParsing().arr(
                    "MemberIdentities/Person", new PersonParser(getParsing()).getItemParserBuilder()))
            ;
    }

    public ObjectNodeParserBuilder getItemParserBuilder() {
        return getParsing().obj()
                .attribute("id", "@Id")
                .attribute("name", "@Name")
                .attribute("displayName", "@DisplayName")
                .attribute("description", "@Desc")
                .attribute("groupType", "@GroupType")
                .attribute("publicType", "@PublicType");
    }
}