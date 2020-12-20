package com.codexsoft.sas.connections.iom.parsers;

import com.codexsoft.sas.connections.iom.models.Tree;
import com.codexsoft.sas.parsers.ParserBase;
import com.nerdforge.unxml.parsers.ObjectParser;
import com.nerdforge.unxml.parsers.builders.ObjectNodeParserBuilder;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class TreeListParser extends ParserBase<List<Tree>> {
    protected ObjectParser<List<Tree>> getParser() {
        ObjectNodeParserBuilder treeParser = getParsing().obj()
            .attribute("id", "@Id")
            .attribute("name", "@Name")
            .attribute("publicType", "@PublicType");

        treeParser = treeParser
            .attribute("children", getParsing().arr("SubTrees/Tree", treeParser));

        return getParsing().arr("Objects/Tree", treeParser)
            .as(Tree.class);
    }
}
