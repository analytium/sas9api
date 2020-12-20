package com.codexsoft.sas.connections.iom.dao;

import com.codexsoft.sas.connections.iom.IOMConnection;
import com.codexsoft.sas.connections.iom.models.Permission;
import com.codexsoft.sas.dao.BaseDao;
import com.sas.iom.SASIOMDefs.VariableArray2dOfAnyHolder;
import com.sas.meta.SASOMI.ISecurityAdmin;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@Scope("prototype")
public class PermissionsDao extends BaseDao {
    @Autowired
    private ApplicationContext context;

    private IOMConnection iomConnection;

    public PermissionsDao(IOMConnection iomConnection) {
        this.iomConnection = iomConnection;
    }

    public List<Permission> getObjectPermissions(String objectType, String objectId) throws Exception {
        ISecurityAdmin admin = iomConnection.getISecurityAdmin();

        val holder = new VariableArray2dOfAnyHolder();
        admin.GetAuthorizationsOnObj(
                "",
                String.format("OMSOBJ:%s/%s", objectType, objectId),
                ISecurityAdmin.SECAD_RETURN_DISPLAY_NAME | ISecurityAdmin.SECAD_RETURN_ROLE_TYPE,
                new String[][] {},
                "",
                holder
        );

        return Permission.fromAuthorizationResult(holder.value);
    }
}

