package ru.boldr.memebot.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Component

@Slf4j

@AllArgsConstructor
public class ParserService {

   private final static String URL = "https://2ch.hk/b/";


  public String getPicture(){
      Connection connect = Jsoup.connect(URL);
      Document document = null;
      try {
          document = connect.get();
      } catch (IOException e) {
          e.printStackTrace();
      }
      assert document != null;
      Elements article = document.select("article");
      List<DataNode> dataNodes = article.dataNodes();
      dataNodes.size();
return null;
  }


}
