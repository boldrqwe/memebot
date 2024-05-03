package ru.boldr.memebot.service;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.boldr.memebot.model.TorrentLink;

import java.time.Duration;
import java.util.List;

@Service
public class TorrentScraperService {

    private final WebClient webClient;
    private final WebDriver driver;

    public TorrentScraperService(WebClient.Builder webClientBuilder, WebDriver driver) {
        this.webClient = webClientBuilder.baseUrl("http://www.ptorrents.com/movies/3d-vr")
                .filter((clientRequest, next) -> next.exchange(clientRequest)
                        .flatMap(clientResponse -> {
                            if (clientResponse.statusCode().is3xxRedirection() && clientResponse.headers().header("Location").size() > 0) {
                                String redirectUrl = clientResponse.headers().header("Location").get(0);
                                return handleRedirect(clientRequest, next, redirectUrl, 1);
                            } else {
                                return Mono.just(clientResponse);
                            }
                        }))
                .build();
        this.driver = driver;
    }

    private static final int MAX_REDIRECTS = 5;


    private static Mono<ClientResponse> handleRedirect(ClientRequest clientRequest, ExchangeFunction next, String redirectUrl, int redirectCount) {
        if (redirectCount > MAX_REDIRECTS) {
            return Mono.error(new IllegalStateException("Too many redirects: " + redirectCount));
        }

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(redirectUrl);
        ClientRequest redirectedRequest = ClientRequest.create(clientRequest.method(), uriBuilder.build().toUri())
                .headers(headers -> headers.addAll(clientRequest.headers()))
                .cookies(cookies -> cookies.addAll(clientRequest.cookies()))
                .attributes(attributes -> attributes.putAll(clientRequest.attributes()))
                .body(clientRequest.body())
                .build();

        return next.exchange(redirectedRequest)
                .flatMap(response -> {
                    if (response.statusCode().is3xxRedirection() && response.headers().header("Location").size() > 0) {
                        String nextRedirectUrl = response.headers().header("Location").get(0);
                        return handleRedirect(redirectedRequest, next, nextRedirectUrl, redirectCount + 1);
                    } else {
                        return Mono.just(response);
                    }
                });
    }


    public Flux<TorrentLink> scrapeLinks(List<String> tags, int startPage, int endPage) {
        return Flux.range(startPage, endPage - startPage + 1)
                .concatMap(page -> scrapePage("/page/" + page, tags).delayElements(Duration.ofSeconds(7)));
    }

    private Flux<TorrentLink> scrapePage(String url, List<String> tags) {
        return webClient.get().uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .map(Jsoup::parse)
                .flatMapIterable(document -> document.select("a[href]"))
                .filter(element -> isAllTagsFound(element.text(), tags))
                .concatMap(element -> getTorrentLink(element.text(), element.attr("href"))
                        .delayElement(Duration.ofSeconds(7)))
                .delayElements(Duration.ofSeconds(7));
    }


    private boolean isAllTagsFound(String text, List<String> tags) {
        for (String tag : tags) {
            if (!text.toLowerCase().contains(tag.toLowerCase())) {
                return false;
            }
        }
        return true;
    }

    private Mono<TorrentLink> parse(String url) {
        return Mono.fromCallable(() -> {
            // Открывает страницу один раз
            driver.get(url);
            WebDriverWait wait = new WebDriverWait(driver, 30);
            wait.until(webDriver -> webDriver.findElement(By.tagName("body")));

            // Получение исходного кода страницы
            String pageSource = driver.getPageSource();

            // Парсинг HTML с помощью Jsoup вместо повторного использования WebDriver
            Document doc = Jsoup.parse(pageSource);
            TorrentLink link = new TorrentLink();

            // Извлечение изображения
            Element image = doc.select("img").first();
            if (image != null) {
                link.setImageUrl(image.attr("src"));
            }

            // Извлечение ссылки на скачивание
            Element downloadLinkElement = doc.select("a.edl_post_dlinks").first();
            if (downloadLinkElement != null) {
                link.setDownloadLink(downloadLinkElement.attr("href"));
            }

            driver.navigate().back();
            return link;
        }).subscribeOn(Schedulers.boundedElastic()); // Выполнение блокирующих операций в отдельном потоке
    }

    @NotNull
    private Mono<TorrentLink> getTorrentLink(String title, String href) {
        return parse(href)
                .map(link -> {
                    link.setTitle(title);
                    link.setPageUrl(href);
                    return link;
                });
    }
}
