package com.skeptomai;

import com.google.common.util.concurrent.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.ParametersAreNonnullByDefault;

public class Main {
    private static final Logger log = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        try {
            HashMap<String,String> m = new Cli(args).parse();
            List<String> mp3sToFetch = getMP3Filenames(m.get("u"));
            fetchMP3s(mp3sToFetch);
        } catch (IOException ioe) {
            log.severe(ioe.getMessage());
        }
    }

    private static void fetchMP3s(List<String> mp3sToFetch) {

        final ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        ListeningExecutorService executor = MoreExecutors
                .listeningDecorator(pool);

        final List<ListenableFuture<String>> collect =
                mp3sToFetch
                        .stream()
                        .map((s) -> {
                            ListenableFuture<String> lf = executor.submit(new MP3Fetcher(s));

                            Futures.addCallback(lf, new FutureCallback<String>() {
                                public void onSuccess(String result) {
                                    System.out.println("Gettin' called back: " + result);
                                }
                                @ParametersAreNonnullByDefault
                                public void onFailure(Throwable thrown) {
                                    System.out.println("Shit! Failed: " + thrown.getMessage());
                                }
                            });

                            return lf;

                        }).collect(Collectors.toList());


        ListenableFuture<List<String>> lf1 = Futures.successfulAsList(collect);
        try {
            lf1.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        log.info("Finished fetches.  All done..");
        pool.shutdown();
    }

    private static List<String> getMP3Filenames(String url) throws IOException {
        List<String> mp3sToFetch = new ArrayList<>();
        URLListFetcher ulf = new URLListFetcher(url);
        Document html = Jsoup.parse(ulf.call());

        for (Element element : html.body().getElementsByClass("postDetails")) {
            Element mp3Link = element.getElementsByAttributeValueStarting("href","http").first();
            if (mp3Link != null) {
                mp3sToFetch.add(mp3Link.attr("href"));
            }
        }

        return mp3sToFetch;
    }
}
