package com.codexsoft.sas.secure;

import com.codexsoft.sas.config.models.ProxyConfigModel;
import com.codexsoft.sas.connections.ConnectionProperties;
import com.codexsoft.sas.models.APIResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

@Slf4j
public class ApiKeyRequestInterceptor extends HandlerInterceptorAdapter {
    private final ProxyConfigModel proxyConfigModel;
    public static final String INVALID_API_KEY_ERROR_MESSAGE = "Invalid api key";

    public ApiKeyRequestInterceptor(ProxyConfigModel proxyConfigModel) {
        this.proxyConfigModel = proxyConfigModel;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String passedKey = request.getHeader("apiKey");
        final ConnectionProperties connectionProperties = getConnectionProperties(request);
        final String apiKey = connectionProperties.getKey();
        final boolean apikeyEnabled = connectionProperties.isApikeyEnabled();

        if(apikeyEnabled && (!StringUtils.hasLength(passedKey) || !apiKey.equals(passedKey))){
            log.error(INVALID_API_KEY_ERROR_MESSAGE);

            final APIResponse<String> payload = new APIResponse<>();
            payload.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            payload.setError(INVALID_API_KEY_ERROR_MESSAGE);

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
        return true;
    }

    private ConnectionProperties getConnectionProperties(HttpServletRequest request) throws Exception {
        String serverName = request.getParameter("serverName");
        String userName = request.getParameter("userName");
        return proxyConfigModel.getConnection(serverName, userName);
    }

}