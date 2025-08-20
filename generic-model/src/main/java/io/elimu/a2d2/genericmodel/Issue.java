package io.elimu.a2d2.genericmodel;

public class Issue implements java.io.Serializable{

    private static final long serialVersionUID = 1L;

    public enum IssueSeverity {
        INFORMATION,
        WARNING,
        ERROR
    }

    public enum IssueType {
        INVALID_VALUE,
        INVALID_STRUCTURE,
        MISSING_ITEM,
        SECURITY_EXPIRED,
        SECURITY_FORBIDDEN,
        PROCESSING_NOT_SUPPORTED,
        PROCESSING_DUPLICATE,
        PROCESSING_MULTIPLE_MATCHES,
        PROCESSING_NOT_FOUND,
        PROCESSING_BUSINESS_RULE_VIOLATION,
        TRANSIENT_TIMEOUT,
        TRANSIENT_EXCEPTION,
        INFORMATIONAL
    }
    private IssueType code;


    private IssueSeverity severity;
    private String diagnostics;


    public Issue() {
    }

    public IssueSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(IssueSeverity severity) {
        this.severity = severity;
    }

    public IssueType getCode() {
        return code;
    }

    public void setCode(IssueType code) {
        this.code = code;
    }

    public String getDiagnostics() {
        return diagnostics;
    }

    public void setDiagnostics(String diagnostics) {
        this.diagnostics = diagnostics;
    }

}
