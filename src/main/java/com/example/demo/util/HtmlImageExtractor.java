package com.example.demo.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class HtmlImageExtractor {
    public static List<String> extractImageUrls(String html){
        List<String> urls = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        Elements imgs = doc.select("img");
        for (Element img : imgs) {
            urls.add(img.attr("src"));
        }
        return urls;
    }
}
