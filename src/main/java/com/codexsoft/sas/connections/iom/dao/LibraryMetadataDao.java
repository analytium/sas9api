package com.codexsoft.sas.connections.iom.dao;

import com.codexsoft.sas.connections.iom.IOMConnection;
import com.codexsoft.sas.connections.jdbc.models.Library;
import com.codexsoft.sas.models.LibraryParams;
import com.sas.metadata.remote.*;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope("prototype")
public class LibraryMetadataDao {
    @Autowired
    private ApplicationContext context;

    private IOMConnection iomConnection;

    public LibraryMetadataDao(IOMConnection iomConnection) {
        this.iomConnection = iomConnection;
    }

    private ServerContext getSasServerContextByName(
            MdFactory factory,
            MdObjectStore store,
            String repositoryId,
            String serverName
    ) throws Exception {
        String xmlSelect = String.format(
                "<XMLSELECT Search=\"*[@Name='%s']\"/>",
                serverName
        );

        List serverContexts = factory.getOMIUtil().getMetadataObjectsSubset(
                store,
                repositoryId,
                MetadataObjects.SERVERCONTEXT,
                MdOMIUtil.OMI_XMLSELECT | MdOMIUtil.OMI_ALL_SIMPLE | MdOMIUtil.OMI_GET_METADATA,
                xmlSelect
        );

        if (serverContexts.size() == 0) {
            throw new Exception(String.format(
                    "ServerContext with name '%s' was not found",
                    serverName));
        }

        return (ServerContext) serverContexts.get(0);
    }

    public Library createLibrary(String repositoryId, LibraryParams libraryParams) throws Exception {
        MdFactory factory = iomConnection.getMdFactory();
        MdObjectStore objectStore = factory.createObjectStore();

        // get tree object for corresponding path
        val treesDao = context.getBean(TreesDao.class, iomConnection);
        val trees = treesDao.getTreesByPath(repositoryId, libraryParams.getLocation());
        if (trees == null || trees.size() == 0) {
            throw new Exception(String.format(
                    "SAS folder with location '%s' was not found",
                    libraryParams.getLocation()));
        }
        val locationTree = trees.get(0);
        // create ref object for the tree
        CMetadata locationTreeRef = factory.createComplexMetadataObject(
                objectStore,
                "Location",
                "Tree",
                locationTree.getId()
        );

        String repositoryIdPart = repositoryId.split("[.]")[1];

        ServerContext serverContext = getSasServerContextByName(
                factory,
                objectStore,
                repositoryId,
                libraryParams.getServerName()
        );

        // create directory object with library path
        Directory directory = (Directory) factory.createComplexMetadataObject(
                objectStore,
                "Path",
                MetadataObjects.DIRECTORY,
                repositoryIdPart
        );
        directory.setDirectoryRole("LibraryPath");
        directory.setDirectoryName(libraryParams.getPath());

        // create SAS library object
        SASLibrary lib = (SASLibrary) factory.createComplexMetadataObject(
                objectStore,
                libraryParams.getLibraryName(),
                MetadataObjects.SASLIBRARY,
                repositoryIdPart
        );
        lib.setIsDBMSLibname(0);
        lib.setEngine(libraryParams.getEngine());
        lib.setLibref(libraryParams.getLibRef());
        lib.setIsPreassigned(libraryParams.isPreassigned() ? 1 : 0);
        lib.setUsingPackages(new AssociationList("UsingPackages") {{
            add(directory);
        }});
        lib.setDeployedComponents(new AssociationList("DeployedComponents") {{
            add(serverContext);
        }});
        lib.setTrees(new AssociationList("Trees") {{
            add(locationTreeRef);
        }});
        lib.setPublicType("Library");
        lib.setUsageVersion(1000000);

        objectStore.updatedMetadataAll();

        // return library model object
        Library result = new Library();
//OP:   id field removed as id=null for libraries list        
//      result.setId(lib.getId());
        result.setLevel(0);
        result.setEngine(lib.getEngine());
        result.setLibname(lib.getLibref());
        result.setPath(libraryParams.getPath());
        result.setReadonly(false);
        result.setSequential(false);
        result.setTemp(false);

        objectStore.dispose();

        return result;
    }

    public void deleteLibrary(String repositoryId, String libref) throws Exception {
        MdObjectStore objectStore = iomConnection.getMdFactory().createObjectStore();

        String xmlSelect = String.format(
                "<XMLSELECT Search=\"*[@Libref='%s']\"/>",
                libref
        );

        List<SASLibrary> libraryList = (List<SASLibrary>) iomConnection.getOMIUtil().getMetadataObjectsSubset(
                objectStore,
                repositoryId,
                MetadataObjects.SASLIBRARY,
                MdOMIUtil.OMI_XMLSELECT | MdOMIUtil.OMI_ALL_SIMPLE | MdOMIUtil.OMI_GET_METADATA,
                xmlSelect
        );

        iomConnection.getMdFactory().deleteMetadataObjects(libraryList);

        objectStore.dispose();
    }
}
