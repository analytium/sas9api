package com.codexsoft.sas.secure;

import com.codexsoft.sas.config.models.ProxyConfigModel;
import com.codexsoft.sas.connections.ConnectionProperties;
import com.codexsoft.sas.connections.iom.IOMConnection;
import com.codexsoft.sas.models.APIResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
public class AuthRequestsInterceptor extends HandlerInterceptorAdapter {
    private final ProxyConfigModel proxyConfigModel;
    public static final String INVALID_API_KEY_ERROR_MESSAGE = "Invalid api key";
    public static final String UNAUTHORISED_BASIC_ERROR_MESSAGE = "Unauthorised, please check username and password";
    public static final String UNAUTHORISED_ERROR_MESSAGE = "Unauthorised";

    public AuthRequestsInterceptor(ProxyConfigModel proxyConfigModel) {
        this.proxyConfigModel = proxyConfigModel;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        final ConnectionProperties connectionProperties = getConnectionProperties(request);
        String passedApiKey = request.getHeader("apiKey");
        String basicAuth = request.getHeader("Authorization");

        if (connectionProperties.isApikeyEnabled() && StringUtils.hasLength(passedApiKey)) {
            return checkApiKeyAuthentication(passedApiKey, connectionProperties, response);
        }

        if (connectionProperties.isBasicAuthEnabled() &&
                StringUtils.hasLength(basicAuth) &&
                basicAuth.toLowerCase().startsWith("basic")) {
            return checkBasicAuthentication(basicAuth, connectionProperties, response);
        }

        if (connectionProperties.isApikeyEnabled() ||
                connectionProperties.isBasicAuthEnabled()) {
            return buildUnauthorisedHttpResponse(response, UNAUTHORISED_ERROR_MESSAGE);
        }

        return true;
    }

    private ConnectionProperties getConnectionProperties(HttpServletRequest request) throws Exception {
        String serverName = request.getParameter("serverName");
        String userName = request.getParameter("userName");
        return proxyConfigModel.getConnection(serverName, userName);
    }

    private boolean checkApiKeyAuthentication(String passedApiKey, ConnectionProperties connectionProperties, HttpServletResponse response) throws Exception {
        final String apiKey = connectionProperties.getKey();
        return (StringUtils.hasLength(passedApiKey) && apiKey.equals(passedApiKey)) ||
                buildUnauthorisedHttpResponse(response, INVALID_API_KEY_ERROR_MESSAGE);
    }

    private boolean checkBasicAuthentication(String basicAuth, ConnectionProperties connectionProperties, HttpServletResponse response) throws Exception {
        final String[] values = decodeBasicAuthCredentials(basicAuth);
        String passedUserName = values[0];
        String passedPassword = values[1];

        overideConnectionProperties(connectionProperties, passedUserName, passedPassword);

        try (final IOMConnection iomConnection = new IOMConnection(connectionProperties)) {
            iomConnection.makeMdOMRConnection();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return buildUnauthorisedHttpResponse(response, UNAUTHORISED_BASIC_ERROR_MESSAGE);
        }
        return true;
    }

    private void overideConnectionProperties(ConnectionProperties connectionProperties, String passedUserName, String passedPassword) {
        connectionProperties.setUserName(passedUserName);
        connectionProperties.setPassword(passedPassword);
        proxyConfigModel.setConnection(connectionProperties);
    }

    private String[] decodeBasicAuthCredentials(String basicAuth) {
        String base64Credentials = basicAuth.substring("Basic".length()).trim();
        byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
        String credentials = new String(credDecoded, StandardCharsets.UTF_8);
        final String[] values = credentials.split(":", 2);
        return values;
    }

    private boolean buildUnauthorisedHttpResponse(HttpServletResponse response, String errorMessage) throws Exception {
        log.error(errorMessage);

        final APIResponse<String> payload = new APIResponse<>();
        payload.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        payload.setError(errorMessage);

        String jsonResponse = new ObjectMapper().writeValueAsString(payload);
        response.reset();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(new MediaType(MediaType.APPLICATION_JSON.getType(),
                MediaType.APPLICATION_JSON.getSubtype(),
                StandardCharsets.UTF_8).toString());
        response.setContentLength(jsonResponse.length());
        response.getWriter().write(jsonResponse);

        return false;
    }
}