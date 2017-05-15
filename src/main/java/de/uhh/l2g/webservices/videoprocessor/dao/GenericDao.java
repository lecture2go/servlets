package de.uhh.l2g.webservices.videoprocessor.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import de.uhh.l2g.webservices.videoprocessor.model.VideoConversion;

public class GenericDao {
	
	private static GenericDao instance;

	//@PersistenceUnit(unitName = "videoprocessor")
	private static EntityManagerFactory emf; 
		
	public static GenericDao getInstance() {
		if (instance == null) {
			System.out.println("no instance - create one");
			instance = new GenericDao();
			emf = Persistence.createEntityManagerFactory("videoprocessor");
		}
		return instance;
	}
	
	private EntityManager getEntityManager() {
		return emf.createEntityManager();
	}
	
	public <T> T save(T o) {
		EntityManager em = getEntityManager();
		em.getTransaction().begin();
		em.persist(o);
		em.getTransaction().commit();
		em.close();
		return o;
	}
	
	public <T> T get(Class<T> type, Long id) {
		return getEntityManager().find(type, id);
	}
	
	public <T> List<T> getAll(Class<T> type) {
		// use criteria instead of a class specific NamedQuery for this GenericDao
		EntityManager em = getEntityManager();
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<T> criteriaQuery = builder.createQuery(type);
		TypedQuery<T> allQuery = em.createQuery(criteriaQuery);
		return allQuery.getResultList();
		
		
		/*
		CriteriaQuery<T> criteriaQuery = builder.createQuery(type);
		Root<T> rootEntry = criteriaQuery.from(type);
		CriteriaQuery<T> all = criteriaQuery.select(rootEntry);
		TypedQuery<T> allQuery = em.createQuery(all);
		return allQuery.getResultList();
		 */
	}
	
	public <T> T update(T o) {
		EntityManager em = getEntityManager();
		em.getTransaction().begin();
		T result = em.merge(o);
		em.getTransaction().commit();
		em.close();
		return result;
	}
		
	public void delete(Object o) {
		getEntityManager().remove(o);
	}
	
}
