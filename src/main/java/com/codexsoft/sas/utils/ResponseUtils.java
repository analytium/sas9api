package com.codexsoft.sas.utils;

import com.codexsoft.sas.models.APIResponse;
import com.codexsoft.sas.utils.functional.CheckedFunction0;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Created by eugene on 18.7.17.
 */
public class ResponseUtils {
    public static <T> ResponseEntity<APIResponse<T>> withResponse(CheckedFunction0<T> dataSource) {
        APIResponse<T> response = new APIResponse();
        try {
            T payload = dataSource.apply();

            response.setStatus(HttpStatus.OK.value());
            response.setPayload(payload);

            return new ResponseEntity<APIResponse<T>>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setError(e.toString());

            return new ResponseEntity<APIResponse<T>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
