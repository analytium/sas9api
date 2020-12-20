package com.codexsoft.sas.connections.iom.search.models;

import com.codexsoft.sas.connections.iom.models.Permission;
import com.codexsoft.sas.models.KeyValuePair;
import lombok.Data;
import lombok.val;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class SASDetailedObject extends SASObject {
    @Data
    public static class Parsing extends SASObject {
        private List<KeyValuePair<String, String>> parsingAttributes;
        private List<KeyValuePair<String, List<SASObject>>> parsingAssociations;
    }
    protected Map<String, String> attributes;
    protected Map<String, List<SASObject>> associations;
    protected List<Permission> permissions;

    public static SASDetailedObject fromParsing(Parsing p) {
        val result = new SASDetailedObject();
        result.setId(p.getId());
        result.setType(p.getType());
        result.setName(p.getName());
        result.setAttributes(
            KeyValuePair.fromList(p.getParsingAttributes())
        );
        result.setAssociations(
            KeyValuePair.fromList(p.getParsingAssociations())
        );
        return result;
    }

    public String getAttribute(String attributeName) {    	
        return attributes.get(attributeName);
    }

    public String toXml() {
        String baseTemplate = "<%1$s%2$s>%3$s</%1$s>";
        String attributesTemplate = " %s=\"%s\"";
        String associationsTemplate = "\n<%1$s>%2$s</%1$s>";
        val attributesBuilder = new StringBuilder();
        attributes.forEach((key, value) -> {
            attributesBuilder.append(String.format(attributesTemplate, key, value));
        });
        val associationsBuilder = new StringBuilder();
        associations.forEach((key, associationsList) -> {
            String assoctiationsString = associationsList.stream()
                    .map(SASObject::toXml)
                    .collect(Collectors.joining("\n"));
            associationsBuilder.append(String.format(associationsTemplate, key, assoctiationsString));
        });
        return String.format(baseTemplate, type, attributesBuilder, associationsBuilder);
    }

    public String toShortXml() {
        return super.toXml();
    }
}
