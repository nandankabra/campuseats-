package com.example.campuseatshttp.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;

@JacksonXmlRootElement(localName = "problem")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Problem {

    public static final MediaType PROBLEM_JSON_MEDIA_TYPE =
            MediaType.parseMediaType("application/problem+json;charset=utf-8");

    private String type;
    private String title;
    private int status;
    private String detail;

    @JacksonXmlElementWrapper(localName = "errors")
    @JacksonXmlProperty(localName = "error")
    private List<FieldErrorDetail> errors;

    public Problem() {
    }

    public Problem(String type, String title, int status, String detail) {
        this.type = type;
        this.title = title;
        this.status = status;
        this.detail = detail;
    }

    public Problem(String type, String title, int status, String detail, List<FieldErrorDetail> errors) {
        this.type = type;
        this.title = title;
        this.status = status;
        this.detail = detail;
        this.errors = errors;
    }

    public static ResponseEntity<Problem> problem(String type, String title, HttpStatus status, String detail) {
        Problem p = new Problem(type, title, status.value(), detail);
        return ResponseEntity.status(status)
                .contentType(PROBLEM_JSON_MEDIA_TYPE)
                .body(p);
    }

    public static ResponseEntity<Problem> problem(String type, String title, HttpStatus status, String detail, List<FieldErrorDetail> errors) {
        Problem p = new Problem(type, title, status.value(), detail, errors);
        return ResponseEntity.status(status)
                .contentType(PROBLEM_JSON_MEDIA_TYPE)
                .body(p);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public List<FieldErrorDetail> getErrors() {
        return errors;
    }

    public void setErrors(List<FieldErrorDetail> errors) {
        this.errors = errors;
    }
}