package com.codexsoft.sas.connections.workspace;

import com.codexsoft.sas.connections.ConnectionProperties;
import com.codexsoft.sas.connections.workspace.models.SASLanguageResponse;
import com.sas.iom.SAS.ILanguageService;
import com.sas.iom.SAS.IWorkspace;
import com.sas.iom.SAS.IWorkspaceHelper;
import com.sas.iom.SASIOMDefs.GenericError;
import com.sas.services.connection.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class WorkspaceConnection implements AutoCloseable {

    private ConnectionProperties connection;

    public WorkspaceConnection(ConnectionProperties connection) {
        this.connection = connection;
    }

    private IWorkspace iWorkspace;

    public IWorkspace getIWorkspace() throws ConnectionFactoryException {
        if (iWorkspace == null) {
            Server server = new BridgeServer(Server.CLSID_SAS, connection.getHost(), connection.getPort());
            ConnectionFactoryConfiguration cxfConfig = new ManualConnectionFactoryConfiguration(server);
            ConnectionFactoryManager cxfManager = new ConnectionFactoryManager();
            ConnectionFactoryInterface cxf = cxfManager.getFactory(cxfConfig);
            ConnectionInterface cx = cxf.getConnection(connection.getUserName(), connection.getPassword());
            org.omg.CORBA.Object obj = cx.getObject();
            iWorkspace = IWorkspaceHelper.narrow(obj);

            cx.close();
        }

        return iWorkspace;
    }

    public void close() throws Exception {
        iWorkspace.Close();
    }

    public SASLanguageResponse submitSasCommand(String command, boolean logEnabled) throws GenericError, ConnectionFactoryException {
        IWorkspace workspace = getIWorkspace();
        ILanguageService languageService = workspace.LanguageService();
        languageService.Submit(command);
        
        StringBuilder linesBuilder = new StringBuilder();
        String line;
        do {
            line = languageService.FlushList(10000);
            linesBuilder.append(line);
        } while (line.length() > 0);
        
        if (logEnabled) {
            StringBuilder logBuilder = new StringBuilder();
            String log;
            do {
                log = languageService.FlushLog(10000);
                logBuilder.append(log);
            } while (log.length() > 0);

            return new SASLanguageResponse(linesBuilder.toString(), logBuilder.toString());
        }
        return new SASLanguageResponse(linesBuilder.toString(), "");
    }
}