package io.elimu.a2d2.genericmodel;

import java.util.ArrayList;
import java.util.List;
public class OutcomeObject implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private List<Issue> issues;

    private void init() {
        if (issues == null) {
            issues = new ArrayList<>();
        }
    }

    public OutcomeObject() {
        init();
    }

    public List<Issue> getIssues() {
        return issues;
    }

    public void setIssues(List<Issue> issues) {
        this.issues = issues;
    }

    public void addIssue(Issue issue) {
        this.issues.add(issue);
    }

    public void removeIssue(Issue issue) {
        this.issues.remove(issue);
    }

    public void clearIssues() {
        this.issues.clear();
    }

    public boolean hasErrors() {
        if (issues == null) {
            return false;
        }
        for (Issue issue : issues) {
            if (issue != null && Issue.IssueSeverity.error.equals(issue.getSeverity())) {
                return true;
            }
        }
        return false;
    }
}