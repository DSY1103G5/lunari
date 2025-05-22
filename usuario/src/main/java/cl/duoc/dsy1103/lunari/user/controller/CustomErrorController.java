package cl.duoc.dsy1103.lunari.user.controller;

import java.util.Map;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.ServletWebRequest;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

public class CustomErrorController implements ErrorController{

    private final ErrorAttributes errorAttributes;

    public CustomErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @RequestMapping("/error")
    public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request) {
        ServletWebRequest webRequest = new ServletWebRequest(request);
        Map<String, Object> errorDetails = errorAttributes.getErrorAttributes(
                webRequest, ErrorAttributeOptions.of(ErrorAttributeOptions.Include.MESSAGE)
        );

        Object statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        HttpStatus status = statusCode != null
                ? HttpStatus.valueOf(Integer.parseInt(statusCode.toString()))
                : HttpStatus.INTERNAL_SERVER_ERROR;

        return new ResponseEntity<>(Map.of(
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", errorDetails.get("message"),
                "path", errorDetails.get("path")
        ), status);
    }

}
