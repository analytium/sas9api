package com.codexsoft.sas.connections.iom.operations;

import com.codexsoft.sas.connections.iom.IOMConnection;
import com.codexsoft.sas.dao.BaseDao;
import lombok.val;
import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.StringHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class AuthorizationDao extends BaseDao {
    public static final String PERMISSION_WRITE_METADATA = "WriteMetadata";
    public static final String PERMISSION_WRITE_MEMBER_METADATA = "WriteMemberMetadata";

    @Autowired
    private ApplicationContext context;

    private IOMConnection iomConnection;

    public AuthorizationDao(IOMConnection iomConnection) {
        this.iomConnection = iomConnection;
    }

    public boolean isAuthorized(
            String objectType,
            String objectId,
            String permissionType
    ) throws Exception{
        val admin = iomConnection.getISecurity();

        val permCond = new StringHolder();
        val isAuth = new BooleanHolder();
        val resource = String.format("OMSOBJ:%s/%s", objectType, objectId);

        admin.IsAuthorized("", resource, permissionType, permCond, isAuth);

        return isAuth.value;
    }
}