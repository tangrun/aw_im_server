package cn.wildfirechat.pojos;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class ArticleContent {
    public static class Article {
        public String id;
        public String cover;
        public String title;
        public String digest;
        public String url;
        public boolean rr;

        public Article() {
        }

        public Article(String id, String cover, String title, String digest, String url, boolean rr) {
            this.id = id;
            this.cover = cover;
            this.title = title;
            this.digest = digest;
            this.url = url;
            this.rr = rr;
        }
    }
    public Article top;
    public List<Article> subArticles;

    public ArticleContent() {
    }

    public ArticleContent(String id, String cover, String title, String digest, String url, boolean rr) {
        this.top = new Article(id, cover, title, digest, url, rr);
    }

    public ArticleContent addSubArticle(String id, String cover, String title, String digest, String url, boolean rr)  {
        if (subArticles == null)
            subArticles = new ArrayList<>();
        subArticles.add(new Article(id, cover, title, digest, url, rr));
        return this;
    }

    public MessagePayload toPayload() {
        MessagePayload payload = new MessagePayload();
        payload.setType(13);
        payload.setSearchableContent(top.title);
        payload.setBase64edData(Base64.getEncoder().encodeToString(new GsonBuilder().disableHtmlEscaping().create().toJson(this).getBytes(StandardCharsets.UTF_8)));
        return payload;
    }
}
