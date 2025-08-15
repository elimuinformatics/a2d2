package io.elimu.a2d2.genericmodel;


import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class IssueTest {

    @Test
    public void testSeverityGetterSetter() {
        Issue issue = new Issue();
        issue.setSeverity(Issue.IssueSeverity.fatal);
        assertEquals(Issue.IssueSeverity.fatal, issue.getSeverity());
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
}
