package com.codexsoft.sas.connections.iom.dao;

import com.codexsoft.sas.connections.iom.IOMConnection;
import com.codexsoft.sas.connections.iom.models.Repository;
import com.codexsoft.sas.connections.iom.parsers.RepositoriesParser;
import com.codexsoft.sas.dao.BaseDao;
import com.sas.meta.SASOMI.IOMI;
import org.omg.CORBA.StringHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@Scope("prototype")
public class RepositoriesDao extends BaseDao {
    @Autowired
    private ApplicationContext context;

    private IOMConnection iomConnection;

    public RepositoriesDao(IOMConnection iomConnection) {
        this.iomConnection = iomConnection;
    }

    public List<Repository> getRepositories() throws Exception {
        StringHolder holder = new StringHolder();

        IOMI iOMI = iomConnection.getIOMIConnection();
        iOMI.GetRepositories(holder, 0,"");

        RepositoriesParser parser = context.getBean(RepositoriesParser.class);
        
        return parser.parse(holder.value);
    }

    public Repository getRepositoryByName(String repositoryName) throws Exception {
        List<Repository> repositories = getRepositories();

        String finalRepositoryName = repositoryName == null ? "Foundation" : repositoryName;
        return repositories.stream()
                .filter((repository) -> repository.getName().matches(finalRepositoryName))
                .findFirst()
                .orElseThrow(() -> new Exception("No repository with name '" + repositoryName + "' found"));
    }
}
