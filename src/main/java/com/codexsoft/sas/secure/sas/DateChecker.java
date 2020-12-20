package com.codexsoft.sas.secure.sas;

import com.codexsoft.sas.connections.ConnectionHelpers;
import com.codexsoft.sas.connections.ConnectionProperties;
import com.codexsoft.sas.connections.workspace.WorkspaceConnection;
import com.codexsoft.sas.connections.workspace.models.SASLanguageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

@Component
@Scope("prototype")
public class DateChecker {
    @Autowired
    private ApplicationContext context;

    private ConnectionProperties iomConnectionProps;

    public DateChecker(ConnectionProperties iomConnectionProps) {
        this.iomConnectionProps = iomConnectionProps;
    }

    public LocalDate getWorkspaceDate() throws Exception {
        ConnectionHelpers connectionHelpers = context.getBean(ConnectionHelpers.class);
        ConnectionProperties workspaceConnectionProps = connectionHelpers.getWorkspaceConnectionPropsByHost(iomConnectionProps, null, null);
        try (WorkspaceConnection workspaceConnection = context.getBean(WorkspaceConnection.class, workspaceConnectionProps)) {
            final String dateCommand = "%PUT %SYSFUNC(DATE());";
            SASLanguageResponse siteNumberResponse = workspaceConnection.submitSasCommand(dateCommand, true);
//OP getLines
            String responseLines = siteNumberResponse.getLog();
            String[] responseLinesArr = responseLines.trim().split("\n");
            String dateString = responseLinesArr[responseLinesArr.length - 1].trim();
            long dateLong = Long.parseLong(dateString);
            return LocalDate.of(1960, 1, 1).plusDays(dateLong);
        }
    }

}
