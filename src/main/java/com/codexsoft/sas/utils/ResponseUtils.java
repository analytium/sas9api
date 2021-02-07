package com.codexsoft.sas.utils;

import com.codexsoft.sas.models.APIResponse;
import com.codexsoft.sas.utils.functional.CheckedFunction0;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.validation.ValidationException;

/**
 * Created by eugene on 18.7.17.
 */

@Slf4j
public class ResponseUtils {
    public static <T> ResponseEntity<APIResponse<T>> withResponse(CheckedFunction0<T> dataSource) {
        APIResponse<T> response = new APIResponse();
        try {
            T payload = dataSource.apply();

            response.setStatus(HttpStatus.OK.value());
            response.setPayload(payload);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception exception) {
            log.error("Error occurred: {}", exception.getMessage(), exception);

            if (exception instanceof ValidationException)
                return getApiResponseResponseEntity(response, exception.getMessage(), HttpStatus.BAD_REQUEST);

            return getApiResponseResponseEntity(response, exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private static <T> ResponseEntity<APIResponse<T>> getApiResponseResponseEntity(APIResponse<T> response, String exceptionMessage, HttpStatus httpStatus) {
        response.setStatus(httpStatus.value());
        response.setError(exceptionMessage);
        return new ResponseEntity<>(response, httpStatus);
    }
}
