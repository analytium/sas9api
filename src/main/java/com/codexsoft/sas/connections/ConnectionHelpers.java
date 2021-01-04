package com.codexsoft.sas.connections;

import com.codexsoft.sas.config.models.ProxyConfigModel;
import com.codexsoft.sas.connections.iom.IOMConnection;
import com.codexsoft.sas.connections.iom.dao.RepositoriesDao;
import com.codexsoft.sas.connections.iom.dao.ServersDao;
import com.codexsoft.sas.connections.jdbc.JDBCConnection;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
@Scope("singleton")
public class ConnectionHelpers {
    private final ApplicationContext context;
    private final ProxyConfigModel proxyConfigModel;

    @Value("${proxy.default_workspace_server_name}")
    String defaultWorkspaceServerName;

    public ConnectionHelpers(ApplicationContext context, ProxyConfigModel proxyConfigModel) {
        this.context = context;
        this.proxyConfigModel = proxyConfigModel;
    }

    public ConnectionProperties getWorkspaceConnectionPropsByServerName(
            ConnectionProperties iomConnectionProps,
            String serverName,
            String repositoryName) throws Exception {

        if (serverName == null) {
            serverName = defaultWorkspaceServerName;
        }
        try (val iomConnection = context.getBean(IOMConnection.class, iomConnectionProps)) {
            val repositoriesDao = context.getBean(RepositoriesDao.class, iomConnection);
            val repository = repositoriesDao.getRepositoryByName(repositoryName);
            val serversDao = context.getBean(ServersDao.class, iomConnection);
            val serverComponent = serversDao.getWorkspaceServersByName(repository.getId(), serverName);
            val connection = serverComponent.getConnection();
            return new ConnectionProperties(
                    connection.getHost(),
                    connection.getPort(),
                    iomConnectionProps.getUserName(),
                    iomConnectionProps.getPassword()
            );
        }
    }

    public ConnectionProperties getWorkspaceConnectionPropsByHost(
            ConnectionProperties iomConnectionProps,
            String workspaceHost,
            String workspactPort
    ) throws Exception {
        if (workspaceHost == null && workspactPort == null) {
            return getWorkspaceConnectionPropsByServerName(iomConnectionProps, null, null);
        }
        return new ConnectionProperties(
                workspaceHost,
                Integer.parseInt(workspactPort),
                iomConnectionProps.getUserName(),
                iomConnectionProps.getPassword()
        );
    }

    public JDBCConnection getJDBCConnectionByServerName(
            ConnectionProperties iomConnectionProps,
            String serverName,
            String repositoryName
    ) throws Exception {
        ConnectionProperties workspaceConnectionProps = getWorkspaceConnectionPropsByServerName(
                iomConnectionProps,
                serverName,
                repositoryName
        );
        return new JDBCConnection(workspaceConnectionProps);
    }

    public JDBCConnection getJDBCConnectionByHost(
            ConnectionProperties iomConnectionProps,
            String workspaceHost,
            String workspacePort
    ) throws Exception {
        if (workspaceHost == null && workspacePort == null) {
            return getJDBCConnectionByServerName(iomConnectionProps, null, null);
        }
        ConnectionProperties workspaceConnectionProps = new ConnectionProperties(
                workspaceHost,
                Integer.parseInt(workspacePort),
                iomConnectionProps.getUserName(),
                iomConnectionProps.getPassword()
        );
        return new JDBCConnection(workspaceConnectionProps);
    }

    public ConnectionProperties getConnectionProperties(HttpServletRequest request) throws Exception {
        String serverName = request.getParameter("serverName");
        String userName = request.getParameter("userName");
        return proxyConfigModel.getConnection(serverName, userName);
    }
}
