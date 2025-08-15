package io.elimu.a2d2.genericmodel;

import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

public class OutcomeObjectTest {

    @Test
    public void testAddAndGetIssues() {
        OutcomeObject outcome = new OutcomeObject();
        Issue issue = new Issue();
        issue.setSeverity(Issue.IssueSeverity.warning);
        outcome.addIssue(issue);
        assertEquals(1, outcome.getIssues().size());
        assertEquals(Issue.IssueSeverity.warning, outcome.getIssues().get(0).getSeverity());
    }

    @Test
    public void testSetIssues() {
        OutcomeObject outcome = new OutcomeObject();
        Issue issue1 = new Issue();
        issue1.setSeverity(Issue.IssueSeverity.information);
        Issue issue2 = new Issue();
        issue2.setSeverity(Issue.IssueSeverity.error);
        outcome.setIssues(Arrays.asList(issue1, issue2));
        assertEquals(2, outcome.getIssues().size());
    }

    @Test
    public void testRemoveIssue() {
        OutcomeObject outcome = new OutcomeObject();
        Issue issue = new Issue();
        outcome.addIssue(issue);
        outcome.removeIssue(issue);
        assertTrue(outcome.getIssues().isEmpty());
    }

    @Test
    public void testClearIssues() {
        OutcomeObject outcome = new OutcomeObject();
        outcome.addIssue(new Issue());
        outcome.clearIssues();
        assertTrue(outcome.getIssues().isEmpty());
    }

    @Test
    public void testHasErrors() {
        OutcomeObject outcome = new OutcomeObject();
        Issue info = new Issue();
        info.setSeverity(Issue.IssueSeverity.information);
        Issue error = new Issue();
        error.setSeverity(Issue.IssueSeverity.error);
        outcome.addIssue(info);
        assertFalse(outcome.hasErrors());
        outcome.addIssue(error);
        assertTrue(outcome.hasErrors());
    }

    @Test
    public void testHasErrorsWithNullIssues() {
        OutcomeObject outcome = new OutcomeObject();
        outcome.setIssues(null);
        assertFalse(outcome.hasErrors());
    }
}
