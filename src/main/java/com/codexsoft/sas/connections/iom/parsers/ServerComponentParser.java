package com.codexsoft.sas.connections.iom.parsers;

import com.codexsoft.sas.connections.iom.models.ServerComponent;
import com.codexsoft.sas.parsers.ParserBase;
import com.nerdforge.unxml.parsers.ObjectParser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ServerComponentParser extends ParserBase<List<ServerComponent>> {
    protected ObjectParser<List<ServerComponent>> getParser() {
        return getParsing().arr("//ServerComponent")
            .attribute("id", "@Id")
            .attribute("name", "ancestor::ServerContext/@Name")
            .attribute("publicType", "@PublicType")
            .attribute("connections", getParsing().arr("SourceConnections/TCPIPConnection")
                    .attribute("id", "@Id")
                    .attribute("host", "@HostName")
                    .attribute("port", "@Port")
                    .attribute("description", "@Desc")
            )
            .as(ServerComponent.class);
    }
}
