package io.elimu.a2d2.genericmodel;


import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class IssueTest {

    @Test
    public void testSeverityGetterSetter() {
        Issue issue = new Issue();
        issue.setSeverity(Issue.IssueSeverity.ERROR);
        assertEquals(Issue.IssueSeverity.ERROR, issue.getSeverity());
    }

    @Test
    public void testCodeGetterSetter() {
        Issue issue = new Issue();
        issue.setCode(Issue.IssueType.INVALID_VALUE);
        assertEquals(Issue.IssueType.INVALID_VALUE, issue.getCode());
    }

    @Test
    public void testDiagnosticsGetterSetter() {
        Issue issue = new Issue();
        issue.setDiagnostics("Some diagnostics");
        assertEquals("Some diagnostics", issue.getDiagnostics());
    }
    
    @Test
    public void testHttpStatusCodeGetterSetter() {
        Issue issue = new Issue();
        issue.setHttpStatusCode(404);
        assertEquals(Integer.valueOf(404), issue.getHttpStatusCode());
    }
}
