package com.roam.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownUtils {

    // Regex for [[Wiki Title]]
    private static final Pattern WIKI_LINK_PATTERN = Pattern.compile("\\[\\[(.*?)\\]\\]");

    public static String processWikiLinks(String markdown) {
        if (markdown == null)
            return "";

        Matcher matcher = WIKI_LINK_PATTERN.matcher(markdown);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String noteTitle = matcher.group(1);
            // Replace [[Title]] with <a href="Wiki:Title">Title</a>
            // The WebView will intercept "Wiki:" protocol
            String replacement = String.format("<a href='Wiki://%s' class='wiki-link'>%s</a>",
                    noteTitle.replace(" ", "%20"), noteTitle);
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);

        return sb.toString();
    }
}
