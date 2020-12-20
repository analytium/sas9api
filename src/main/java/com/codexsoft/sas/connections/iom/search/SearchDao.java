package com.codexsoft.sas.connections.iom.search;

import com.codexsoft.sas.connections.iom.IOMConnection;
import com.codexsoft.sas.connections.iom.dao.PermissionsDao;
import com.codexsoft.sas.connections.iom.search.criterias.ISearchCriteria;
import com.codexsoft.sas.connections.iom.search.models.SASDetailedObject;
import com.codexsoft.sas.connections.iom.search.models.SearchParams;
import com.codexsoft.sas.connections.iom.search.parsers.SASDetailedObjectParser;
import com.codexsoft.sas.dao.BaseDao;
import com.sas.meta.SASOMI.IOMI;
import com.sas.metadata.MetadataUtil;
import lombok.val;
import org.omg.CORBA.StringHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
public class SearchDao extends BaseDao {
    @Autowired
    private ApplicationContext context;

    private IOMConnection iomConnection;

    public SearchDao(IOMConnection iomConnection) {
        this.iomConnection = iomConnection;
    }

    public String getXmlselectStatement(List<ISearchCriteria> criterias) throws Exception {
        if (criterias.size() == 0) {
            // empty string is valid option
            return "";
        }

        val criteriasSorted = criterias.stream()
                .sorted(Comparator.comparing(ISearchCriteria::getPriority))
                .collect(Collectors.toList());
        val criteriasAttribute = criteriasSorted.stream()
                .filter(criteria -> criteria.getType() == ISearchCriteria.CRITERIA_ATTRIBUTE)
                .collect(Collectors.toList());
        val criteriasPath = criteriasSorted.stream()
                .filter(criteria -> criteria.getType() == ISearchCriteria.CRITERIA_PATH)
                .collect(Collectors.toList());
        val criteriasType = criteriasSorted.stream()
                .filter(criteria -> criteria.getType() == ISearchCriteria.CRITERIA_TYPE)
                .collect(Collectors.toList());

        StringBuilder result = new StringBuilder();

        if (criteriasType.size() == 1) {
            result.append(criteriasType.get(0).getXmlselectCriteria());
        } else if (criteriasType.size() == 0){
            result.append("*");
        } else {
            throw new Exception("More than one type criteria");
        }

        if (criteriasAttribute.size() > 0) {
            result.append("[");
            result.append(criteriasAttribute.stream()
                    .map(iSearchCriteria -> iSearchCriteria.getXmlselectCriteria())
                    .collect(Collectors.joining(" AND "))
            );
            result.append("]");
        }

        if (criteriasPath.size() > 0) {
            result.append(criteriasPath.stream()
                    .map(iSearchCriteria -> iSearchCriteria.getXmlselectCriteria())
                    .collect(Collectors.joining())
            );
        }
        return "<XMLSELECT Search=\"" + result + "\"/>";
    }

    public int getMetadataFlags(
            List<ISearchCriteria> criterias,
            boolean includeAssociations
    ) {
        int flags = MetadataUtil.OMI_INCLUDE_SUBTYPES | MetadataUtil.OMI_GET_METADATA;
        if (criterias.size() > 0) {
            flags |= MetadataUtil.OMI_XMLSELECT;
        }
        if (includeAssociations) {
            return flags | MetadataUtil.OMI_ALL;
        } else {
            return flags | MetadataUtil.OMI_ALL_SIMPLE;
        }
    }

    public String getMetadataTemplate() {
        // just for a case we need it varying...
        return "";
    }

    public List<SASDetailedObject> getObjects(
            String repositoryId,
            List<ISearchCriteria> criterias,
            boolean includeAssociations
    ) throws Exception {
        IOMI iOMI = iomConnection.getIOMIConnection();

        String xmlselect = getXmlselectStatement(criterias);
        String template = getMetadataTemplate();
        int flags = getMetadataFlags(criterias, includeAssociations);

        StringHolder outputMeta = new StringHolder();
        iOMI.GetMetadataObjects(
                repositoryId,
                "Root",
                outputMeta,
                "SAS",
                flags,
                xmlselect + template
        );

        val parser = new SASDetailedObjectParser();
        return parser.parse(outputMeta.value);
    }

    public List<SASDetailedObject> filterObjects(
            List<SASDetailedObject> objects,
            List<ISearchCriteria> criterias
    ) {
        return objects.stream()
            .filter(object -> {
                boolean isValid = true;
                for (val criteria : criterias) {
                    isValid &= criteria.checkCriteria(object);
                    if (!isValid) break;
                }
                return isValid;
            })
            .collect(Collectors.toList());
}

    public void includeObjectsPermissions(List<SASDetailedObject> objects) throws Exception {
        val permissionsDao = context.getBean(PermissionsDao.class, iomConnection);
        for (val object : objects) {
            val permissions = permissionsDao.getObjectPermissions(object.getType(), object.getId());
            object.setPermissions(permissions);
        }
    }

    public List<SASDetailedObject> getFilteredObjects(
            String repositoryId,
            List<ISearchCriteria> criterias,
            boolean includeAssociations,
            boolean includePermissions
    ) throws Exception {
        val objects = getObjects(repositoryId, criterias, includeAssociations);
        val filtered = filterObjects(objects, criterias);
        if (includePermissions) {
            includeObjectsPermissions(filtered);
        }
        return filtered;
    }

    public List<SASDetailedObject> getFilteredObjects(
            String repositoryId,
            SearchParams searchParams
    ) throws Exception{
        val criterias = searchParams.getSearchCriterias();
        return getFilteredObjects(
                repositoryId,
                criterias,
                searchParams.isIncludeAssociations(),
                searchParams.isIncludePermissions()
        );
    }
}
