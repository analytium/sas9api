package com.codexsoft.sas.connections.iom.search.criterias;

import com.codexsoft.sas.connections.iom.search.models.SASDetailedObject;
import lombok.val;

import java.util.Stack;

public class LocationSearchCriteria implements ISearchCriteria {
    private String searchLocation;

    public LocationSearchCriteria(String searchLocation) {
        this.searchLocation = searchLocation;
    }

    public int getPriority() { return 2; }

    public int getType() { return CRITERIA_PATH; }

    public String getXmlselectCriteria() {
        val pathComponentsStack = new Stack<String>() {{
            for (val component : searchLocation.split("[\\/]")) {
                if (component.length() > 0) this.push(component);
            }
        }};
        if (pathComponentsStack.size() == 0) return null;

        val template = new StringBuilder();
        template.append("[");
        while (pathComponentsStack.size() > 0) {
            template.append(String.format(
                    "ParentTree/Tree[@Name='%s']/", pathComponentsStack.pop()
            ));
        }
        template.append("SoftwareComponents/SoftwareComponent[@PublicType='RootFolder']]");
        return template.toString();
    }

    public boolean checkCriteria(SASDetailedObject object) {
        // there is currently no way to check if the object is in given location
        // without retrieving more associations
        return true;
    }
}
