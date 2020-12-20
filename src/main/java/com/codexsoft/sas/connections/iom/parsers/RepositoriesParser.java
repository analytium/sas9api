package com.codexsoft.sas.connections.iom.parsers;

import com.codexsoft.sas.connections.iom.models.Repository;
import com.codexsoft.sas.parsers.ParserBase;
import com.nerdforge.unxml.parsers.ObjectParser;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class RepositoriesParser extends ParserBase<List<Repository>> {
    protected ObjectParser<List<Repository>> getParser() {
        return getParsing().arr("Repositories/Repository")
                .attribute("id", "@Id")
                .attribute("name", "@Name")
                .attribute("desc", "@Desc")
                .attribute("defaultNS", "@DefaultNS")
                .as(Repository.class);
    }
}
