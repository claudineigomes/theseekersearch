package com.theseeker.entities.dnsDAO;

import com.theseeker.entities.DNS;
import com.theseeker.entities.FetchedPages;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

/**
 * Created by claudinei on 28/03/17.
 */
@Repository
public class DNSDaoImpl implements DNSDao {

    @PersistenceContext
    protected EntityManager em;


    public List<DNS> getDNS() throws DataAccessException {
        Query query = em.createQuery("select d from DNS d");
        List<DNS> resultList = query.getResultList();
        System.out.println(query.getFirstResult());
        return resultList;
    }


    public DNS getDNS(String dominio) throws DataAccessException {
        Query query = em.createQuery("select d from DNS d where d.dominio = :dominio", DNS.class).setParameter("dominio", dominio);
        query.setMaxResults(1);
        DNS result;
        try{
            result = (DNS) query.getSingleResult();
        }catch (Exception e){
            e.printStackTrace();
            result = null;
        }
        return result;
    }

    @Transactional
    public void insertDNS(DNS dns){
        System.out.println(dns.toString());
        em.persist(dns);
    }
}