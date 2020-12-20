package com.codexsoft.sas.secure.sas;

import com.codexsoft.sas.connections.ConnectionHelpers;
import com.codexsoft.sas.connections.ConnectionProperties;
import com.codexsoft.sas.connections.iom.IOMConnection;
import com.codexsoft.sas.connections.iom.dao.RepositoriesDao;
import com.codexsoft.sas.connections.iom.models.Repository;
import com.codexsoft.sas.connections.iom.search.SearchDao;
import com.codexsoft.sas.connections.iom.search.models.SASDetailedObject;
import com.codexsoft.sas.connections.iom.search.models.SearchParams;
import com.codexsoft.sas.connections.workspace.WorkspaceConnection;
import com.codexsoft.sas.connections.workspace.models.SASLanguageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
public class SiteNumberChecker {
    @Autowired
    private ApplicationContext context;

    private ConnectionProperties iomConnectionProps;

    private static final Pattern METADATA_SITE_NUMBER_PATTERN = Pattern.compile("^.*\\s+SITE=(\\d{8})\\s+.*$", Pattern.DOTALL);

    public SiteNumberChecker(ConnectionProperties iomConnectionProps) {
        this.iomConnectionProps = iomConnectionProps;
    }

    public String getSiteNumber() throws Exception {
        List<String> metadataSiteNumbers = getMetadataSiteNumbers();    
        String workspaceSiteNumber = getWorkspaceSiteNumber();        
        for(String siteNumber : metadataSiteNumbers) {           
            if (siteNumber.equals(workspaceSiteNumber)) {            	
                return siteNumber;
            }
        }
        throw new Exception("No valid site number found");
    }

    private List<String> getMetadataSiteNumbers() throws Exception {
        SearchParams searchParams = new SearchParams();
        searchParams.setType("TextStore");
        searchParams.setNameContains("setinit");
        try (IOMConnection iomConnection = context.getBean(IOMConnection.class, iomConnectionProps)) {       	
            RepositoriesDao repositoryDao = context.getBean(RepositoriesDao.class, iomConnection);            
            Repository defaultRepo = repositoryDao.getRepositoryByName(null);   // Foundation by default            
            SearchDao searchDao = context.getBean(SearchDao.class, iomConnection);            
            List<SASDetailedObject> objects = searchDao.getFilteredObjects(defaultRepo.getId(), searchParams);
            return objects.stream()
                    .map(object -> {
                        String storedText = object.getAttribute("StoredText");                        
                        Matcher matcher = METADATA_SITE_NUMBER_PATTERN.matcher(storedText);                        
                        if (matcher.matches()) {
                            return matcher.group(1);
                        } else {
                            return null;
                        }
                    })
                    .filter(siteNumer -> siteNumer != null)
                    .collect(Collectors.toList());
        }
    }

    private String getWorkspaceSiteNumber() throws Exception {
        ConnectionHelpers connectionHelpers = context.getBean(ConnectionHelpers.class);        
        ConnectionProperties workspaceConnectionProps = connectionHelpers.getWorkspaceConnectionPropsByHost(iomConnectionProps, null, null);        
        try (WorkspaceConnection workspaceConnection = context.getBean(WorkspaceConnection.class, workspaceConnectionProps)) {
            final String siteNumberCommand = "%put &syssite;";
            SASLanguageResponse siteNumberResponse = workspaceConnection.submitSasCommand(siteNumberCommand, true);            
//OP: was getLines()            
            String responseLines = siteNumberResponse.getLog();
            String[] responseLinesArr = responseLines.trim().split("\n");
            String siteNumber = responseLinesArr[responseLinesArr.length - 1].trim();
            if (siteNumber.length() != 8 || Integer.parseInt(siteNumber) <= 0) {
                throw new Exception("Invalid SAS site number");
            }
            return siteNumber;
        }
    }
}
