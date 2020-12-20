package com.codexsoft.sas.connections.iom.operations;

import com.codexsoft.sas.connections.ConnectionProperties;
import com.codexsoft.sas.connections.iom.IOMConnection;
import com.codexsoft.sas.connections.iom.dao.TreesDao;
import com.codexsoft.sas.connections.iom.search.SearchDao;
import com.codexsoft.sas.connections.iom.search.models.SearchParams;
import com.codexsoft.sas.dao.BaseDao;
import com.sas.meta.SASOMI.IOMI;
import com.sas.metadata.MetadataUtil;
import lombok.val;
import org.omg.CORBA.StringHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
@Scope("prototype")
public class ObjectOperationsDao extends BaseDao {
    @Autowired
    private ApplicationContext context;

    @Value("${proxy.sas.sashome}")
    private String sasHome;

    private IOMConnection iomConnection;

    public ObjectOperationsDao(IOMConnection iomConnection) {
        this.iomConnection = iomConnection;
    }

    public void deleteObject(
            String repositoryId,
            String sourceLocation,
            String sourceName,
            String publicType
    ) throws Exception {
        val treesDao = context.getBean(TreesDao.class, iomConnection);
        val srcTrees = treesDao.getTreesByPath(repositoryId, sourceLocation);
        if (srcTrees.size() == 0) {
            throw new Exception("Source location is not found: " + sourceLocation);
        }
        val srcTree = srcTrees.get(0);
        val authorizationDao = context.getBean(AuthorizationDao.class, iomConnection);
        if (!authorizationDao.isAuthorized("Tree", srcTree.getId(), AuthorizationDao.PERMISSION_WRITE_MEMBER_METADATA)) {
            throw new Exception("Current user is not authorized for changing source folder content");
        }

        val srcSearchParams = new SearchParams();
        srcSearchParams.setLocationFolderIds(treesDao.collectTreeIds(srcTrees, false).toArray(new String[0]));
        srcSearchParams.setNameEquals(sourceName);
        srcSearchParams.setPublicTypes(new String[] { publicType });

        val searchDao = context.getBean(SearchDao.class, iomConnection);
        val srcObjects = searchDao.getFilteredObjects(repositoryId, srcSearchParams);
        if (srcObjects.size() == 0) {
            throw new Exception("Source object with name '" + sourceName + "' was not found in location '" + sourceLocation + "'");
        }
        if (srcObjects.size() > 1) {
            throw new Exception("More than one object with name '" + sourceName + "' was found in location '" + sourceLocation + "'");
        }

        String deleteObjectXml = srcObjects.get(0).toShortXml();

        IOMI iOMI = iomConnection.getIOMIConnection();

        val outputMeta = new StringHolder();
        int rc = iOMI.DeleteMetadata(deleteObjectXml, outputMeta, "SAS", MetadataUtil.OMI_TRUSTED_CLIENT, "");
        if (rc != 0) {
            throw new Exception("DeleteMetadata returned code " + rc + "instead of 0");
        }
    }

    public void moveObject(
            String repositoryId,
            String sourceLocation,
            String sourceName,
            String publicType,
            String destinationLocation
    ) throws Exception {
        // get source and destination folders
        val treesDao = context.getBean(TreesDao.class, iomConnection);
        val srcTrees = treesDao.getTreesByPath(repositoryId, sourceLocation);
        val dstTrees = treesDao.getTreesByPath(repositoryId, destinationLocation);
        
        if (srcTrees.size() == 0) {
            throw new Exception("Source location is not found: " + sourceLocation);
        }
        if (dstTrees.size() == 0) {
            throw new Exception("Destination location is not found: " + destinationLocation);
        }
        val srcTree = srcTrees.get(0);
        val dstTree = dstTrees.get(0);
        // check if locations match
        if (srcTree.getId().equals(dstTree.getId())) return;
        // check source and destination folder permissions
        val authorizationDao = context.getBean(AuthorizationDao.class, iomConnection);
        if (!authorizationDao.isAuthorized("Tree", srcTree.getId(), AuthorizationDao.PERMISSION_WRITE_MEMBER_METADATA)) {
            throw new Exception("Current user is not authorized for changing source folder content");
        }
        if (!authorizationDao.isAuthorized("Tree", dstTree.getId(), AuthorizationDao.PERMISSION_WRITE_MEMBER_METADATA)) {
            throw new Exception("Current user is not authorized for changing destination folder content");
        }

        val srcSearchParams = new SearchParams();
        srcSearchParams.setLocationFolderIds(treesDao.collectTreeIds(srcTrees, false).toArray(new String[0]));
        srcSearchParams.setNameEquals(sourceName);
        srcSearchParams.setPublicTypes(new String[] { publicType });

        val searchDao = context.getBean(SearchDao.class, iomConnection);
        val srcObjects = searchDao.getFilteredObjects(repositoryId, srcSearchParams);
        if (srcObjects.size() == 0) {
            throw new Exception("Source object with name '" + sourceName + "' was not found in location '" + sourceLocation + "'");
        }
        if (srcObjects.size() > 1) {
            throw new Exception("More than one object with name '" + sourceName + "' was found in location '" + sourceLocation + "'");
        }

        String template = "<MULTIPLE_REQUESTS>%s</MULTIPLE_REQUESTS>";

        String updateTemplate = ""
                + "<UpdateMetadata>"
                + "  <Metadata>"
                + "     <Tree Id='%1$s'>"
                + "         <Members Function='%2$s'>%3$s</Members>"
                + "     </Tree>"
                + "  </Metadata>"
                + "  <NS>SAS</NS>"
                + "  <Flags>" + MetadataUtil.OMI_TRUSTED_CLIENT + "</Flags>"
                + "  <Options/>"
                + "</UpdateMetadata>"
                ;

        String srcObjectXml = srcObjects.get(0).toObjRefXml();

        String cutRequest = String.format(updateTemplate, srcTree.getId(), "Remove", srcObjectXml);
        String pasteRequest = String.format(updateTemplate, dstTree.getId(), "Append", srcObjectXml);

        String request = String.format(template, cutRequest + pasteRequest);

        IOMI iOMI = iomConnection.getIOMIConnection();
        val outputMeta = new StringHolder();
        int rs = iOMI.DoRequest(request, outputMeta);
        if (rs != 0) {
            throw new Exception("DoRequest returned " + rs + " instead of 0");
        }
    }

    private String readStream(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder();
        String line = null;
        String sep = System.getProperty("line.separator");
        while ( (line = reader.readLine()) != null) {
            builder.append(line);
            builder.append(sep);
        }
        return builder.toString();
    }

    public String copyObject(
            ConnectionProperties connectionProperties,
            String repositoryId,
            String sourceLocation,
            String objectName,
            String publicType,
            String destinationLocation
    ) throws Exception {
        // get source and destination folders
        val treesDao = context.getBean(TreesDao.class, iomConnection);
        val srcTrees = treesDao.getTreesByPath(repositoryId, sourceLocation);
        val dstTrees = treesDao.getTreesByPath(repositoryId, destinationLocation);
        if (srcTrees.size() == 0) {
            throw new Exception("Source location is not found: " + sourceLocation);
        }
        if (dstTrees.size() == 0) {
            throw new Exception("Destination location is not found: " + destinationLocation);
        }
        val srcTree = srcTrees.get(0);
        val dstTree = dstTrees.get(0);
              
        // there is no sense to check read/write permissions as in delete/move cases
        // because copying process is inherently more complex so leave it to the copy tool

        List<String> cmd = new ArrayList<>();
        cmd.add("java");

        cmd.add("-Xmx512M");

        cmd.add("-Djava.system.class.loader=com.sas.app.AppClassLoader");

        cmd.add(String.format(
                "-Dsas.app.launch.picklist=%s",
                Paths.get(sasHome, "SASPlatformObjectFramework", "9.4", "platform.picklist")
        ));

        cmd.add(String.format(
                "-Dsas.app.repository.path=%s",
                Paths.get(sasHome, "SASVersionedJarRepository", "eclipse")
        ));

        cmd.add("-cp");
        cmd.add(Paths.get(sasHome, "SASVersionedJarRepository", "eclipse", "plugins", "sas.launcher.jar").toString());

        cmd.add("com.sas.metadata.promotion.BatchCopyObjects");

        cmd.add("-nolog");

        cmd.add("-host");
        cmd.add(connectionProperties.getHost());

        cmd.add("-port");
        cmd.add("" + connectionProperties.getPort());

        cmd.add("-user");
        cmd.add(connectionProperties.getUserName());

        cmd.add("-password");
        cmd.add(connectionProperties.getPassword());

        cmd.add("-objects");
        cmd.add(String.format(
                "%s/%s(%s)",
                sourceLocation, objectName, publicType
        ));

        cmd.add("-target");
        cmd.add(destinationLocation);

        ProcessBuilder pb = new ProcessBuilder(cmd);

        Process p = pb.start();

        int code = p.waitFor();

        String stdout = readStream(p.getInputStream());
        String stderr = readStream(p.getErrorStream());

        if (code != 0 && code != 4) {
            throw new Exception("STDOUT: " + stdout + "\nSTDERR: " + stderr);
        }
        return stdout;
    }
    

}
