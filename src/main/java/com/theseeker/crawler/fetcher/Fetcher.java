package com.theseeker.crawler.fetcher;


import com.theseeker.crawler.dns.DNSUtil;
import com.theseeker.crawler.entities.DNS;
import com.theseeker.crawler.entities.FetchedPages;
import com.theseeker.crawler.entities.dnsDAO.DNSDao;
import com.theseeker.crawler.entities.fetchedPagesDAO.FetchedPagesDAO;
import com.theseeker.crawler.writeReader.Writer;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetAddress;

import org.jsoup.Jsoup;

/**
 * Created by claudinei on 28/03/17.
 */
@Component
@Scope("prototype")
public class Fetcher {

    @Autowired
    private DNSDao dnsDao;

    @Autowired
    private FetchedPagesDAO fpDao;

    @Autowired
    private Writer writer;

    public Fetcher(){

    }

    public Document getHtmlContent(String dominio) throws IOException {
        Document doc = Jsoup.connect(dominio).get();

        return doc;
    }

    private boolean alreadyCollected(String dominio){
        if( fpDao.getIdDNS(dominio) != null ){
            return true;
        }else{
            return false;
        }
    }

    public void start(String listDomains) throws IOException {
        String[] domains = listDomains.split("\n");

        for(int i = 0; i < domains.length; i++){
            String dominio = domains[i];

            /*Procuro se existe no banco de dns*/
            DNS dns = dnsDao.getDNS(dominio);
            if(dns == null){
                InetAddress ip = DNSUtil.getIp(dominio);
                if(ip != null){
                    dns = new DNS(dominio, ip.getHostAddress());
                    dnsDao.insertDNS(dns);
                }
            }

            /*Acessar através do IP*/

            /*Acessando atraves do LINK*/
            Document doc = getHtmlContent(dominio);
            FetchedPages fp = new FetchedPages(dns.getIp(), dominio, doc.title(), doc.html());

            /*Chamando o Writer para escrever no banco de dados*/
            writer.writerInFetchedPages(fp);
        }
    }
}