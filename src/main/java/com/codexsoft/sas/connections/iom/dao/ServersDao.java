package com.codexsoft.sas.connections.iom.dao;

import com.codexsoft.sas.connections.iom.IOMConnection;
import com.codexsoft.sas.connections.iom.models.ServerComponent;
import com.codexsoft.sas.connections.iom.parsers.ServerComponentParser;
import com.codexsoft.sas.dao.BaseDao;
import com.sas.meta.SASOMI.IOMI;
import com.sas.metadata.MetadataUtil;
import lombok.extern.slf4j.Slf4j;
import org.omg.CORBA.StringHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope("prototype")
@Slf4j
public class ServersDao extends BaseDao {
    static final String PUBLIC_TYPE_WORKSPACE = "LogicalServer.Workspace";
    static final String PUBLIC_TYPE_STORED_PROCESS = "LogicalServer.StoredProcess";

    @Autowired
    private ApplicationContext context;

    private IOMConnection iomConnection;

    public ServersDao(IOMConnection iomConnection) {
        this.iomConnection = iomConnection;
    }

    public List<ServerComponent> getServerComponents(String repositoryId, String publicType) throws Exception {
        StringHolder outputMeta = new StringHolder();
        String template = ""
                + "<Templates>"
                + "    <Template TemplateName='ServerContext' >"
                + "        <ServerContext>"
                + "           <DependentComponents search=\"LogicalServer[@PublicType = '" + publicType + "']\" />"
                + "        </ServerContext>"
                + "        <DependentComponents>"
                + "             <LogicalServer />"
                + "        </DependentComponents>"
                + "        <LogicalServer>"
                + "             <UsingComponents />"
                + "        </LogicalServer>"
                + "        <UsingComponents>"
                + "             <ServerComponent />"
                + "        </UsingComponents>"
                + "        <ServerComponent>"
                + "           <SourceConnections />"
                + "        </ServerComponent>"
                + "        <TCPIPConnection />"
                + "    </Template>"
                + "</Templates>";

        IOMI iOMI = iomConnection.getIOMIConnection();
        iOMI.GetMetadataObjects(
                repositoryId,
                "ServerContext",
                outputMeta,
                "SAS",
                MetadataUtil.OMI_TEMPLATE | MetadataUtil.OMI_GET_METADATA | MetadataUtil.OMI_ALL_SIMPLE,
                template
        );

        ServerComponentParser parser = context.getBean(ServerComponentParser.class);
        
        return parser.parse(outputMeta.value);
    }

    public ServerComponent getServerComponentByName(String repositoryId, String publicType, String name) throws Exception {
        List<ServerComponent> serverComponents = getServerComponents(repositoryId, publicType);

        return serverComponents.stream()
                .filter((serverComponent) -> serverComponent.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> getServerNotFoundException(name));
    }

    private Exception getServerNotFoundException(String name) {
        log.error("No server with name '{}' found", name);
        return new Exception("No server with name '" + name + "' found");
    }

    public List<ServerComponent> getWorkspaceServers(String repositoryId) throws Exception {
        return getServerComponents(repositoryId, PUBLIC_TYPE_WORKSPACE);
    }

    public ServerComponent getWorkspaceServersByName(String repositoryId, String serverName) throws Exception {
        return getServerComponentByName(repositoryId, PUBLIC_TYPE_WORKSPACE, serverName);
    }

    public List<ServerComponent> getSTPServers(String repositoryId) throws Exception {
        return getServerComponents(repositoryId, PUBLIC_TYPE_STORED_PROCESS);
    }

    public ServerComponent getSTPServerByName(String repositoryId, String serverName) throws Exception {
        return getServerComponentByName(repositoryId, PUBLIC_TYPE_STORED_PROCESS, serverName);
    }
}
