package com.theseeker.crawler.dispatcher;

import com.theseeker.crawler.entities.FetchedPages;
import com.theseeker.crawler.entities.queuedURL;
import com.theseeker.crawler.fetcher.Fetcher;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;

import com.theseeker.crawler.entities.queuedURLDAO.queuedURLDAO;

import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by claudinei on 28/03/17
 */
@Component
public class Dispatcher {

    @Autowired
    ApplicationContext ctx;

    @Autowired
    queuedURLDAO queuedURLDAO;

    ExecutorService executorService;

    public Dispatcher(){

    }

    @PostConstruct
    public void readFromQueuedURLs() throws IOException {

        BasicThreadFactory factory = new BasicThreadFactory.Builder()
                .namingPattern("dispatcherThread-%d").build();

        executorService = Executors.newSingleThreadExecutor(factory);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                while(true){
                    while(!(queuedURLDAO.queuedURLIsEmpty())){
                        queuedURL qurl = queuedURLDAO.retrieveAndDelete();
                        if(qurl != null){
                            System.out.println("DISPATCHER PROCESSANDO: " + qurl.getDominio());
                            Fetcher fetcher = ctx.getBean(Fetcher.class);
                            try {
                                fetcher.start(qurl.getDominio());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });

    }
}
