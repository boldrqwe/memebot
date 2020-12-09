package ru.boldr.memebot.api;
/**
 * Sample Java code for youtube.search.list
 * See instructions for running these code samples locally:
 * https://developers.google.com/explorer-help/guides/code_samples#java
 */

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.boldr.memebot.helpers.JsonHelper;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Component
public class YoutubeApiSearch {
    private static final String CLIENT_SECRETS = "/client_secret.json";
    private static final Collection<String> SCOPES =
            Arrays.asList("https://www.googleapis.com/auth/youtube " +
                    "https://www.googleapis.com/auth/youtube.force-ssl " +
                    "https://www.googleapis.com/auth/youtube.readonly " +
                    "https://www.googleapis.com/auth/youtubepartner");

    private static final String APPLICATION_NAME = "API code samples";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private final static Logger logger = LoggerFactory.getLogger(YoutubeApiSearch.class);

    private YouTube youtubeService;
    private final JsonHelper jsonHelper;
    YouTube.Search.List request;

    public YoutubeApiSearch(JsonHelper jsonHelper) {
        this.jsonHelper = jsonHelper;
    }

    public static Credential authorize(final NetHttpTransport httpTransport) throws IOException {
        // Load client secrets.
        InputStream in = YoutubeApiSearch.class.getResourceAsStream(CLIENT_SECRETS);
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                        .build();
        Credential credential =
                new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        return credential;
    }

    public static YouTube getService() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = authorize(httpTransport);
        return new YouTube.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

//    @PostConstruct
//    public void init()
//            throws GeneralSecurityException, IOException, GoogleJsonResponseException {
//        this.youtubeService = getService();
//    }

    public String find(String query) {

        SearchListResponse response = null;
        try {
            request = this.youtubeService.search()
                    .list("snippet");
            response = request.setQ(query).execute();
            logger.info("new update: {}", jsonHelper.lineToMap(response));
            List<SearchResult> searchResultList = response.getItems();
            String result = "";
            for (int i = 0; i < searchResultList.size(); i++) {
                SearchResult searchResult = searchResultList.get(0 + i);
                if (!Objects.equals(searchResult, null)) {
                    result = searchResult.getId().getVideoId();
                }
            }
            return result;
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}