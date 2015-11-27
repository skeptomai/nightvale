package com.skeptomai;

import com.google.common.util.concurrent.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

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
        List<Future<String>> fl = new ArrayList<>();

        final ExecutorService pool = Executors.newFixedThreadPool(5);

        ListeningExecutorService executor = MoreExecutors
                .listeningDecorator(pool);

        for (String mp3ToFetch : mp3sToFetch) {
            log.info(mp3ToFetch);

            ListenableFuture<String> lf = executor.submit(new MP3Fetcher(mp3ToFetch));
            fl.add(lf);

            Futures.addCallback(lf, new FutureCallback<String>() {
                public void onSuccess(String result) {
                    System.out.println("Gettin' called back: " + result);
                }

                public void onFailure(Throwable thrown) {
                    System.out.println("Shit! Failed: " + thrown.getMessage());
                }
            });

        }

        for (Future<String> fi : fl){
            try {
                String result = fi.get();
                System.out.println("actually made it past get..: " + result);
            } catch (ExecutionException | InterruptedException ee) {
                log.severe("Interrupted future.  World weeps");
            }
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
        log.info(mp3sToFetch.toString());
        return mp3sToFetch;
    }
}
