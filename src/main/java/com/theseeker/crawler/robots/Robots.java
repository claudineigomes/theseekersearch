package com.theseeker.crawler.robots;

import com.theseeker.crawler.entities.DNS;
import com.theseeker.crawler.entities.OrderedURL;
import com.theseeker.util.robots.NoRobotClient;
import com.theseeker.util.robots.NoRobotException;
import com.theseeker.util.url.URLCanonicalizer;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import com.theseeker.crawler.entities.dnsDAO.DNSDao;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by claudinei on 11/04/17.
 */
@Component
public class Robots {

    @Autowired
    DNSDao dnsDAO;

    ExecutorService executorService;

    @PostConstruct
    public void resolveRobots() throws URISyntaxException, IOException, NoRobotException {

        BasicThreadFactory factory = new BasicThreadFactory.Builder()
                .namingPattern("dnsthread-%d").build();

        executorService = Executors.newSingleThreadExecutor(factory);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                while(true){
                    List<DNS> listDNS = dnsDAO.getRobots();

                    for(DNS dns: listDNS){
                        dnsDAO.remove(dns);
                        dns.setRobots(true);
                        dnsDAO.setTime(dns);

                        String[] x = dns.getDominio().split("/");
                        try {
                            downloadRobots(x[0] + "//" + x[2] + "/robots.txt");
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        String urlRestante = "";
                        for(int i = 3; i < x.length; i++){
                            urlRestante += "/" + x[i];
                        }


                        try {
                            Path path = Paths.get(System.getProperty("user.dir") + "/src/main/resources/robotsData/" + getDomainName(dns.getDominio()));
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }


                        String hardCode = null;
                        hardCode = "file:///"+new File(System.getProperty("user.dir") + "/src/main/resources/robotsData/").getAbsoluteFile()+ "/";
String base = "";
                        try {
                            base = hardCode + getDomainName(dns.getDominio()) + "/";
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }


                        NoRobotClient nrc = new NoRobotClient("SeekerRobot-1.0");
                        try {
                            nrc.parse( new URL(base) );
                        } catch (NoRobotException e) {
                            e.printStackTrace();
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                        try {
                            System.out.println( nrc.isUrlAllowed( new URL(base+urlRestante) ) ) ;
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        });



        String url;


    }

    public void downloadRobots(String url) throws URISyntaxException, IOException {
        String dominio = getDomainName(url);
        String hardCode = "file:///"+new File(System.getProperty("user.dir") + "/src/main/resources/robotsData/").getAbsoluteFile()+"/";

        Path path = Paths.get(System.getProperty("user.dir") + "/src/main/resources/robotsData/" + dominio);

        if( !(Files.exists(path))){
            Files.createDirectories(path);

            Path path2 = Paths.get(System.getProperty("user.dir") + "/src/main/resources/robotsData/" + dominio + "/" + "robots.txt");
            Files.createFile(path2);

            try{
                PrintWriter writer = new PrintWriter(path2.toString(), "UTF-8");

                try(BufferedReader in = new BufferedReader(
                        new InputStreamReader(new URL(url).openStream()))) {
                    String line = null;
                    while((line = in.readLine()) != null) {
                        writer.println(line);
                    }
                } catch (IOException e) {

                }



                writer.close();
            } catch (IOException e) {

            }
        }
    }

    public String getDomainName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }
}
