/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jirban.jira.impl.board;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.dmr.ModelNode;
import org.jirban.jira.impl.config.BoardConfig;
import org.jirban.jira.impl.config.BoardProjectConfig;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.order.SortOrder;

/**
 * The data for a board project, i.e. a project whose issues should appear as cards on the board.
 *
 * @author Kabir Khan
 */
public class
BoardProject {

    private final BoardProjectConfig projectConfig;
    private final List<List<Issue>> issuesByState;

    public BoardProject(BoardProjectConfig projectConfig, List<List<Issue>> issuesByState) {
        this.projectConfig = projectConfig;
        this.issuesByState = issuesByState;
    }

    public boolean isDataSame(BoardProject boardProject) {
        return false;
    }

    public int getStateIndex(String state) {
        return 0;
    }

    public int getAssigneeIndex(Assignee assignee) {
        return 0;
    }

    void serialize(ModelNode parent) {
        ModelNode projectIssues = parent.get("issues");
        for (List<Issue> issuesForState : issuesByState) {
            ModelNode issuesForStateNode = new ModelNode();
            issuesForStateNode.setEmptyList();
            for (Issue issue : issuesForState) {
                if (issue.isValid()) {
                    issuesForStateNode.add(issue.getKey());
                }
            }
            projectIssues.add(issuesForStateNode);
        }
    }

    static Builder builder(SearchService searchService, User user, Board.Builder builder, BoardProjectConfig projectConfig) {
        return new Builder(searchService, user, builder, projectConfig);
    }

    static class Builder {
        private final SearchService searchService;
        private final User user;
        private final Board.Builder boardBuilder;
        private final BoardProjectConfig projectConfig;
        private final Map<String, List<Issue>> issuesByState = new HashMap<>();


        public Builder(SearchService searchService, User user, Board.Builder boardBuilder, BoardProjectConfig projectConfig) {
            this.searchService = searchService;
            this.user = user;
            this.boardBuilder = boardBuilder;
            this.projectConfig = projectConfig;
        }

        public Builder addIssue(String state, Issue issue) {
            final List<Issue> issues = issuesByState.computeIfAbsent(state, l -> new ArrayList<>());
            issues.add(issue);
            boardBuilder.addIssue(issue);
            return this;
        }

        public BoardProject build(BoardConfig boardConfig, boolean owner) {
            final List<List<Issue>> resultIssues = new ArrayList<>();
            for (String state : boardBuilder.getOwnerStateNames()) {
                List<Issue> issues = issuesByState.get(owner ? state : projectConfig.mapBoardStateOntoOwnState(state));
                if (issues == null) {
                    issues = new ArrayList<>();
                }
                resultIssues.add(Collections.synchronizedList(issues));
            }

            return new BoardProject(projectConfig, Collections.unmodifiableList(resultIssues));
        }

        public void load() throws SearchException {
            JqlQueryBuilder queryBuilder = JqlQueryBuilder.newBuilder();
            queryBuilder.where().project(projectConfig.getCode());
            if (projectConfig.getQueryFilter() != null) {
                queryBuilder.where().addCondition(projectConfig.getQueryFilter());
            }
            queryBuilder.orderBy().addSortForFieldName("Rank", SortOrder.ASC, true);

            SearchResults searchResults =
                    searchService.search(user, queryBuilder.buildQuery(), PagerFilter.getUnlimitedFilter());

            for (com.atlassian.jira.issue.Issue jiraIssue : searchResults.getIssues()) {
                Issue.Builder issueBuilder = Issue.builder(this);
                issueBuilder.load(jiraIssue);
                Issue issue = issueBuilder.build();
                addIssue(issue.getState(), issue);
            }
        }

        public BoardProjectConfig getConfig() {
            return projectConfig;
        }

        public Integer getPriorityIndex(String issueKey, Priority priorityObject) {
            return boardBuilder.getPriorityIndex(issueKey, priorityObject);
        }

        public Integer getIssueTypeIndex(String issueKey, IssueType issueTypeObject) {
            return boardBuilder.getIssueTypeIndex(issueKey, issueTypeObject);
        }

        public Assignee getAssignee(User user) {
            return boardBuilder.getAssignee(user);
        }

        public String getCode() {
            return projectConfig.getCode();
        }
    }
}