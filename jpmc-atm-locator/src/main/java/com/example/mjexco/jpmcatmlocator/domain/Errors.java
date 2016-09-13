package com.example.mjexco.jpmcatmlocator.domain;

public class Errors {
    private String issueType;
    private Object actionCode;
    private Integer code;
    private Object attributes;
    private String message;

    /**
     *
     * @return
     * The issueType
     */
    public String getIssueType() {
        return issueType;
    }

    /**
     *
     * @param issueType
     * The issueType
     */
    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    /**
     *
     * @return
     * The actionCode
     */
    public Object getActionCode() {
        return actionCode;
    }

    /**
     *
     * @param actionCode
     * The actionCode
     */
    public void setActionCode(Object actionCode) {
        this.actionCode = actionCode;
    }

    /**
     *
     * @return
     * The code
     */
    public Integer getCode() {
        return code;
    }

    /**
     *
     * @param code
     * The code
     */
    public void setCode(Integer code) {
        this.code = code;
    }

    /**
     *
     * @return
     * The attributes
     */
    public Object getAttributes() {
        return attributes;
    }

    /**
     *
     * @param attributes
     * The attributes
     */
    public void setAttributes(Object attributes) {
        this.attributes = attributes;
    }

    /**
     *
     * @return
     * The message
     */
    public String getMessage() {
        return message;
    }

    /**
     *
     * @param message
     * The message
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
