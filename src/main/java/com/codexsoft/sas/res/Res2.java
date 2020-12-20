package com.codexsoft.sas.res;

import com.codexsoft.sas.config.LicenseCondition;
import com.codexsoft.sas.models.APIResponse;
import com.codexsoft.sas.models.ServerConfiguration;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/res2")
@Api(value = "res2", description = "res2")
@Conditional(LicenseCondition.class)
public class Res2 {
    @ApiOperation(value = "Gets current server connection configuration and its repositories")
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ResponseEntity<APIResponse<ServerConfiguration>> sasMetadataInfo() {
        return null;
    }
}

