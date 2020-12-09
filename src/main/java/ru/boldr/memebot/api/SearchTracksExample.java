package ru.boldr.memebot.api;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.data.search.simplified.SearchTracksRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class SearchTracksExample {
    private static final String accessToken = "AQBgEvHwjMCq-kCJzqgt6Icx-Td_bTrFlkhg7TpKi_NcWlcXeKumR9gJnlIpDWZD-uX8UwmBlm91zjewdYy8mKrp1oKWWHfmK1ABjLcVufxwIO1MM4Q8bTx1ejtuGDL_tb8B2VumpfA9aJ2QFRuRAG8kBt8uW3c5bcw2pEgx3Y8REDthDowW2vbYYeJ05hNiHaRATV4i";
    private static final String q = "Abba";

    private static final SpotifyApi spotifyApi = new SpotifyApi.Builder()
            .setAccessToken(accessToken)
            .build();
    private static final SearchTracksRequest searchTracksRequest = spotifyApi.searchTracks(q)
//          .market(CountryCode.SE)
//          .limit(10)
//          .offset(0)
//          .includeExternal("audio")
            .build();

    public static void searchTracks_Sync() {
        try {
            final Paging<Track> trackPaging = searchTracksRequest.execute();

            System.out.println("Total: " + trackPaging.getTotal());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void searchTracks_Async() {
        try {
            final CompletableFuture<Paging<Track>> pagingFuture = searchTracksRequest.executeAsync();

            // Thread free to do other tasks...

            // Example Only. Never block in production code.
            final Paging<Track> trackPaging = pagingFuture.join();

            System.out.println("Total: " + trackPaging.getTotal());
        } catch (CompletionException e) {
            System.out.println("Error: " + e.getCause().getMessage());
        } catch (CancellationException e) {
            System.out.println("Async operation cancelled.");
        }
    }

    public static void main(String[] args) {
        searchTracks_Sync();
        searchTracks_Async();
    }
}
