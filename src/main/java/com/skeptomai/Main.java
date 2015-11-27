package com.skeptomai;

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
        ExecutorCompletionService executor = new ExecutorCompletionService(pool);
        for (String mp3ToFetch : mp3sToFetch) {
            log.info(mp3ToFetch);
            fl.add(executor.submit(new MP3Fetcher(mp3ToFetch)));
        }

        for (Future<String> fi : fl){
            try {
                fi.get();
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
