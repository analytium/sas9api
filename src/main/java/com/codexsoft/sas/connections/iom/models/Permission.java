package com.codexsoft.sas.connections.iom.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.val;
import org.omg.CORBA.Any;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class Permission {
    private static final HashMap<Integer, Access.AccessType> codeAccessTypeMap = new HashMap<Integer, Access.AccessType>();
    {
        codeAccessTypeMap.put(0x01, new Access.AccessType(0x01, "SECAD_PERM_EXPD", "Explicit Deny", "Deny was specified directly on the object."));
        codeAccessTypeMap.put(0x02, new Access.AccessType(0x02, "SECAD_PERM_EXPG", "Explicit Grant", "Grant was specified directly on the object."));
        codeAccessTypeMap.put(0x03, new Access.AccessType(0x03, "SECAD_PERM_EXPM", "Explicit Mask", "Mask to extract explicit value."));
        codeAccessTypeMap.put(0x04, new Access.AccessType(0x04, "SECAD_PERM_ACTD", "ACT Deny", "Deny from an ACT other than the default ACT."));
        codeAccessTypeMap.put(0x08, new Access.AccessType(0x08, "SECAD_PERM_ACTG", "ACT Grant", "Grant from an ACT other than the default ACT."));
        codeAccessTypeMap.put(0x0C, new Access.AccessType(0x0C, "SECAD_PERM_ACTM", "ACT Mask", "Mask to extract ACT value."));
        codeAccessTypeMap.put(0x10, new Access.AccessType(0x10, "SECAD_PERM_NDRD", "Indirect Deny", "Deny from IdentityGroup inheritance or from the default ACT."));
        codeAccessTypeMap.put(0x20, new Access.AccessType(0x20, "SECAD_PERM_NDRG", "Indirect Grant", "Grant from IdentityGroup inheritance or from the default ACT."));
        codeAccessTypeMap.put(0x30, new Access.AccessType(0x30, "SECAD_PERM_NDRM", "Indirect Mask", "Mask to extract indirect value."));
    }

    @Getter
    public static class Access {
        @Getter
        @AllArgsConstructor
        public static class AccessType {
            private int code;
            private String symbol;
            private String permissionType;
            private String permissionDescription;
        }
        private int code;
        private ArrayList<AccessType> accessTypes;

        public Access(int code) {
            this.code = code;
            this.accessTypes = new ArrayList<Access.AccessType>();

            int[] masks = {0x03, 0x0C, 0x30};
            for (int mask : masks) {
                val accessType = codeAccessTypeMap.get(code & mask);
                if (accessType != null) {
                    accessTypes.add(accessType);
                }
            }
        }
    }
    private String identityType;
    private String identityName;
    private String identityDisplayName;
    private Map<String, Access> accessPermissions;

    public static List<Permission> fromAuthorizationResult(Any[][] authResult) {
        val permissionByNameType = new HashMap<String, Permission>();
        for (val row : authResult) {
            val identityType = row[0].extract_string();
            val identityName = row[1].extract_string();
            val permissionCode = row[2].extract_long();
            val permissionName = row[3].extract_string();
            val identityDisplayName = row[5].extract_string();

            val identityNameType = identityName + ":" + identityType;

            Permission permission = permissionByNameType.get(identityNameType);
            if (permission == null) {
                permission = new Permission();
                permission.setIdentityName(identityName);
                permission.setIdentityType(identityType);
                permission.setIdentityDisplayName(identityDisplayName);
                permission.setAccessPermissions(new HashMap<>());

                permissionByNameType.put(identityNameType, permission);
            }

            val accessPermissions = permission.getAccessPermissions();
            accessPermissions.put(permissionName, new Access(permissionCode));
        }
        return new ArrayList<>(permissionByNameType.values());
    }
}
