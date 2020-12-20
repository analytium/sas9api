package com.codexsoft.sas.connections.iom.dao;

import com.codexsoft.sas.connections.iom.IOMConnection;
import com.codexsoft.sas.connections.iom.models.Tree;
import com.codexsoft.sas.connections.iom.parsers.TreeListParser;
import com.codexsoft.sas.dao.BaseDao;
import com.sas.meta.SASOMI.IOMI;
import com.sas.metadata.MetadataUtil;
import lombok.val;
import org.omg.CORBA.StringHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

@Component
@Scope("prototype")
public class TreesDao extends BaseDao {
    @Autowired
    private ApplicationContext context;

    private IOMConnection iomConnection;

    public TreesDao(IOMConnection iomConnection) {
        this.iomConnection = iomConnection;
    }

    private String getTreeXmlselectCriteria(String path) throws Exception {
        val pathStack = new Stack<String>() {{
            for (val component : path.split("[\\/]")) {
                if (component.length() > 0) this.push(component);
            }
        }};

        val result = new StringBuilder();
        result.append("Tree[");
        if (pathStack.size() > 0) {
            result.append(String.format("@Name='%s' AND ", pathStack.pop()));
        }
        result.append("@PublicType='Folder'][");
        while (pathStack.size() > 0) {
            result.append(String.format(
                    "ParentTree/Tree[@Name='%s']/", pathStack.pop()));
        }
        result.append("SoftwareComponents/SoftwareComponent[@PublicType='RootFolder']]");
        return "<XMLSELECT Search=\"" + result + "\"/>";
    }

    public List<Tree> getTreesByPath(String repositoryId, String path) throws Exception {
        StringHolder outputMeta = new StringHolder();

        String xmlSelect = getTreeXmlselectCriteria(path);

        String template = ""
                + "<Templates>"
                + "    <Template TemplateName='Tree'>"
                + "         <Tree>"
                + "             <SubTrees />"
                + "         </Tree>"
                + "         <SubTrees>"
                + "             <Tree />"
                + "         </SubTrees>"
                + "    </Template>"
                + "</Templates>";

        IOMI iOMI = iomConnection.getIOMIConnection();
        iOMI.GetMetadataObjects(
                repositoryId,
                "Tree",
                outputMeta,
                "SAS",
                MetadataUtil.OMI_XMLSELECT | MetadataUtil.OMI_TEMPLATE | MetadataUtil.OMI_GET_METADATA | MetadataUtil.OMI_ALL_SIMPLE,
                xmlSelect + template
        );

        TreeListParser parser = context.getBean(TreeListParser.class);
        
        return parser.parse(outputMeta.value);
    }

    public List<String> collectTreeIds(List<Tree> trees, boolean recursive) {
        val result = new ArrayList<String>();
        for (val tree : trees) {
            result.add(tree.getId());
            if (recursive) {
                result.addAll(collectTreeIds(tree.getChildren(), recursive));
            }
        }
        return result;
    }
}
