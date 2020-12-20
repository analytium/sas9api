package com.codexsoft.sas.connections.iom.dao;

import com.codexsoft.sas.connections.iom.IOMConnection;
import com.codexsoft.sas.connections.iom.models.Person;
import com.codexsoft.sas.connections.iom.parsers.PersonParser;
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
public class PersonsDao extends BaseDao {
    @Autowired
    private ApplicationContext context;

    private IOMConnection iomConnection;

    public PersonsDao(IOMConnection iomConnection) {
        this.iomConnection = iomConnection;
    }

    public List<Person> getPersons(String repositoryId) throws Exception {
        StringHolder outputMeta = new StringHolder();
        String template = ""
                + "<Templates>"
                + "    <Template TemplateName='Person'>"
                + "         <Person>"
                + "             <IdentityGroups />"
                + "         </Person>"
                + "         <IdentityGroups>"
                + "             <IdentityGroup />"
                + "         </IdentityGroups>"
                + "    </Template>"
                + "</Templates>";

        IOMI iOMI = iomConnection.getIOMIConnection();
        iOMI.GetMetadataObjects(
                repositoryId,
                "Person",
                outputMeta,
                "SAS",
                MetadataUtil.OMI_TEMPLATE | MetadataUtil.OMI_GET_METADATA | MetadataUtil.OMI_ALL_SIMPLE,
                template
        );

        PersonParser parser = context.getBean(PersonParser.class);
        
        return parser.parse(outputMeta.value);
    }

    public Person getPersonByName(String repositoryId, String personName) throws Exception {
        List<Person> persons = getPersons(repositoryId);
        return persons.stream()
                .filter(person -> person.getName().equalsIgnoreCase(personName))
                .findFirst()
                .orElseThrow(() -> new Exception("No user with name '" + personName + "'"));
    }
}
