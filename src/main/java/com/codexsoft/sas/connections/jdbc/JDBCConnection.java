package com.codexsoft.sas.connections.jdbc;

import com.codexsoft.sas.connections.ConnectionProperties;
import com.codexsoft.sas.connections.workspace.WorkspaceConnection;
import com.sas.iom.SAS.IDataService;
import com.sas.rio.MVAConnection;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.util.Properties;


@Component
@Scope("prototype")
public class JDBCConnection implements AutoCloseable {
    private WorkspaceConnection workspaceConnection;
    private Connection jdbcConnection;
    private boolean closeWorkspaceConnection;

    public JDBCConnection(WorkspaceConnection workspaceConnection) {
        this.workspaceConnection = workspaceConnection;
        this.closeWorkspaceConnection = false;
    }

    public JDBCConnection(ConnectionProperties connectionProperties) {
        this.workspaceConnection = new WorkspaceConnection(connectionProperties);
        this.closeWorkspaceConnection = true;
    }

    public WorkspaceConnection getWorkspaceConnection() {
        return this.workspaceConnection;
    }

    public Connection getConnection() throws Exception {
        if (jdbcConnection == null) {
            IDataService rio = workspaceConnection.getIWorkspace().DataService();
            jdbcConnection = new MVAConnection(rio, new Properties());
        }
        return jdbcConnection;
    }

    public void close() throws Exception {
        jdbcConnection.close();
        if (closeWorkspaceConnection) {
            workspaceConnection.close();
        }
    }
}
