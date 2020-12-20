package com.codexsoft.sas.connections.iom.dao;

import com.codexsoft.sas.connections.iom.IOMConnection;
import com.codexsoft.sas.connections.iom.models.IdentityGroup;
import com.codexsoft.sas.connections.iom.parsers.IdentityGroupParser;
import com.codexsoft.sas.dao.BaseDao;
import com.sas.meta.SASOMI.IOMI;
import com.sas.metadata.MetadataUtil;
import org.omg.CORBA.StringHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope("prototype")
public class IdentityGroupsDao extends BaseDao {
    @Autowired
    private ApplicationContext context;

    private IOMConnection iomConnection;

    public IdentityGroupsDao(IOMConnection iomConnection) {
        this.iomConnection = iomConnection;
    }

    private List<IdentityGroup> getIdentityGroups(String repositoryId, String publicType) throws Exception {
        StringHolder outputMeta = new StringHolder();
        String xmlselect = String.format(
                "<XMLSELECT Search=\"IdentityGroup[@PublicType='%s']\"/>",
                publicType
        );
        String template = ""
                + "<Templates>"
                + "    <Template TemplateName='IdentityGroup'>"
                + "         <IdentityGroup>"
                + "             <MemberIdentities />"
                + "         </IdentityGroup>"
                + "         <MemberIdentities>"
                + "             <IdentityGroup />"
                + "             <Person />"
                + "         </MemberIdentities>"
                + "    </Template>"
                + "</Templates>";

        IOMI iOMI = iomConnection.getIOMIConnection();
        iOMI.GetMetadataObjects(
                repositoryId,
                "IdentityGroup",
                outputMeta,
                "SAS",
                MetadataUtil.OMI_XMLSELECT | MetadataUtil.OMI_TEMPLATE | MetadataUtil.OMI_GET_METADATA | MetadataUtil.OMI_ALL_SIMPLE,
                xmlselect + template
        );

        IdentityGroupParser parser = context.getBean(IdentityGroupParser.class);
        
        return parser.parse(outputMeta.value);
    }

    private IdentityGroup getIdentityGroupByName(String repositoryId, String publicType, String groupName) throws Exception {
        List<IdentityGroup> identityGroups = getIdentityGroups(repositoryId, publicType);
        return identityGroups.stream()
                .filter(group -> group.getName().equalsIgnoreCase(groupName))
                .findFirst()
                .orElseThrow(() -> new Exception("No group with name '" + groupName + "'"));
    }

    public List<IdentityGroup> getGroups(String repositoryId) throws Exception {
        return getIdentityGroups(repositoryId, IdentityGroupParser.PUBLIC_TYPE_GROUP);
    }

    public IdentityGroup getGroupByName(String repositoryId, String groupName) throws Exception {
        return getIdentityGroupByName(repositoryId, IdentityGroupParser.PUBLIC_TYPE_GROUP, groupName);
    }

    public List<IdentityGroup> getRoles(String repositoryId) throws Exception {
        return getIdentityGroups(repositoryId, IdentityGroupParser.PUBLIC_TYPE_ROLE);
    }

    public IdentityGroup getRoleByName(String repositoryId, String groupName) throws Exception {
        return getIdentityGroupByName(repositoryId, IdentityGroupParser.PUBLIC_TYPE_ROLE, groupName);
    }
}
