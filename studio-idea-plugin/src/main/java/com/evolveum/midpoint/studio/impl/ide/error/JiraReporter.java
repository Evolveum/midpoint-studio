package com.evolveum.midpoint.studio.impl.ide.error;

import com.intellij.openapi.diagnostic.SubmittedReportInfo;
//import okhttp3.Call;
//import okhttp3.OkHttpClient;
//import okhttp3.*;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Viliam Repan (lazyman).
 */
public class JiraReporter {

    private static final String JIRA_URL = "https://jira.evolveum.com";

    private static final String JIRA_REST_PREFIX = "/rest/api/latest";

    private static final String SEARCH_PARAM_NAME = "jql";

    private static final String SEARCH_PARAM_VALUE = "project = MID AND status = Open AND resolution = Unresolved AND labels = \"#studio\" ORDER BY priority DESC, updated DESC";

    private static final String ISSUE_LABEL = "auto-generated";

    private String username;

    private String password;

//    private OkHttpClient client;

    private void init() {
//        OkHttpClient.Builder builder = new OkHttpClient.Builder();
//        if (username != null || password != null) {
//            builder.authenticator((route, response) -> {
//
//                String credential = Credentials.basic(username, password);
//                return response.request().newBuilder()
//                        .header("Authorization", credential)
//                        .build();
//            });
//        }
//
//        client = builder.build();
    }

    public SubmittedReportInfo sendFeedback(ReporterError error) {
        try {
            JiraIssue issue = buildIssue(error);

            JiraIssue existing = findIssue(issue);
            if (existing != null) {
                commentIssue(issue);
            } else {
                createIssue(issue);
            }

            String id = issue.getId();
            URL htmlUrl = issue.getUrl();
            String message;
            if (existing == null) {
                message = "<a href=\"" + htmlUrl + "\">Created issue " + id + "</a>. Thank you for your feedback!";
            } else {
                message = "<a href=\"" + htmlUrl + "\">A similar issues was already reported (#" + id + ")</a>. Thank you for your feedback!";
            }

            return new SubmittedReportInfo(htmlUrl.toString(), message, existing == null ?
                    SubmittedReportInfo.SubmissionStatus.NEW_ISSUE : SubmittedReportInfo.SubmissionStatus.DUPLICATE);
        } catch (Exception ex) {
            return new SubmittedReportInfo(null, "Could not communicate with GitHub", SubmittedReportInfo.SubmissionStatus.FAILED);
        }
    }

    private JiraIssue buildIssue(ReporterError error) {
        // todo implement
        return new JiraIssue();
    }

    private void commentIssue(JiraIssue issue) {
        // todo implement
    }

    private JiraIssue findIssue(JiraIssue issue) {
        // todo implement

        Map<String, String> params = new HashMap<>();
        params.put(SEARCH_PARAM_NAME, SEARCH_PARAM_VALUE);

//        Request request = build("/search", params).build();
//        Call call = client.newCall(request);

        return new JiraIssue();
    }

    private JiraIssue createIssue(JiraIssue issue) {
        // todo implement
        return new JiraIssue();
    }

//    private Request.Builder build(String path, Map<String, String> params) {
//        HttpUrl.Builder builder = HttpUrl.parse(path).newBuilder();
//
//        if (params != null) {
//            for (Map.Entry<String, String> param : params.entrySet()) {
//                builder.addQueryParameter(param.getKey(), param.getValue());
//            }
//        }
//
//        return new Request.Builder().url(builder.build());
//    }
}
