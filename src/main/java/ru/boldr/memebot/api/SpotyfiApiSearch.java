package ru.boldr.memebot.api;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchTracksRequest;
import org.apache.hc.core5.http.ParseException;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;


@Component
public class SpotyfiApiSearch {
    private static final String clientId = "5cbde998a6f04afa83683f7bff277cf6";
    private static final String clientSecret = "1cb7be1535344cf3973ec1cd83567ada";
    private static final URI redirectUri = SpotifyHttpManager.makeUri("http://localhost:8080");

    private static final SpotifyApi spotifyApi = new SpotifyApi.Builder()
            .setClientId(clientId)
            .setClientSecret(clientSecret)
            .setRedirectUri(redirectUri)
            .build();
    private static final AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
            .state("x4xkmn9pu3j6ukrs8n")
            .scope("user-read-currently-playing")
            .show_dialog(true)
            .build();

    public static void authorizationCodeUri_Sync() {
        final URI uri = authorizationCodeUriRequest.execute();
        System.out.println("URI: " + uri.toString());

    }

    public static void authorizationCodeUri_Async() {
        try {
            final CompletableFuture<URI> uriFuture = authorizationCodeUriRequest.executeAsync();
            // Thread free to do other tasks...
            // Example Only. Never block in production code.
            final URI uri = uriFuture.join();

            System.out.println("URI: " + uri.toString());
        } catch (CompletionException e) {
            System.out.println("Error: " + e.getCause().getMessage());
        } catch (CancellationException e) {
            System.out.println("Async operation cancelled.");
        }
    }

    @PostConstruct
    public static void init() {
        authorizationCodeUri_Sync();
        authorizationCodeUri_Async();
    }
    public void search(){
        SearchTracksRequest searchTracksRequest = spotifyApi.searchTracks("muse").build();
        try {
            final Paging<Track> trackPaging = searchTracksRequest.execute();

            System.out.println("Total: " + trackPaging.getTotal());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}

