package de.uhh.l2g.webservices.videoprocessor.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 * This Dao is used for generic/ non-model specific database operations
 */
public class GenericDao {
	
	private static GenericDao instance;

	private static EntityManagerFactory emf; 
	
	
	/**
	 * This DAO is a singleton, so only one entitymanagerfactory is created
	 * @return the single instance of the GenericDao class
	 */
	public static GenericDao getInstance() {
		if (instance == null) {
			instance = new GenericDao();
			emf = Persistence.createEntityManagerFactory("videoprocessor");
		}
		return instance;
	}
	
	private EntityManager getEntityManager() {
		return emf.createEntityManager();
	}
	
	
	/**
	 * Saves an entity to the databse
	 * @param o the object to persist
	 * @return the persisted object
	 */
	public <T> T save(T o) {
		EntityManager em = getEntityManager();
		em.getTransaction().begin();
		em.persist(o);
		em.getTransaction().commit();
		em.close();
		return o;
	}
	
	
	/**
	 * Gets an entity from the database with the given id
	 * @param type the class of the entity
	 * @param id the id to find
	 * @return the entity (null if not found)
	 */
	public <T> T get(Class<T> type, Long id) {
		EntityManager em = getEntityManager();
		T entity = em.find(type, id);
		em.close();
		return entity;
	}
	
	/**
	 * Gets entities for an entity class if field-value matches
	 * @param type the class of the entity
	 * @return a list with all filtered entities
	 */
	public <T> List<T> getByFieldValue(Class<T> type, String field, Long value) {
		EntityManager em = getEntityManager();
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<T> criteriaQuery = builder.createQuery(type);
		Root<T> rootEntry = criteriaQuery.from(type);
        CriteriaQuery<T> all = criteriaQuery.select(rootEntry);
        all.where(builder.equal(rootEntry.get(field), value));
		TypedQuery<T> allQuery = em.createQuery(all);
		List<T> entities = allQuery.getResultList();
		em.close();
		return entities;
	}
	
	/**
	 * Gets entities for an entity class if field-value matches ordered by orderField (DESC)
	 * @param type the class of the entity
	 * @param field the field to look for the value
	 * @param value the value which is looked in the field
	 * @param orderField the field which is used to order the results
	 * @return a list with all filtered entities
	 */
	public <T> List<T> getByFieldValueOrderedDesc(Class<T> type, String field, Long value, String orderField) {
		EntityManager em = getEntityManager();
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<T> criteriaQuery = builder.createQuery(type);
		Root<T> rootEntry = criteriaQuery.from(type);
        CriteriaQuery<T> all = criteriaQuery.select(rootEntry);
        all.where(builder.equal(rootEntry.get(field), value));
        all.orderBy(builder.desc(rootEntry.get(orderField)));
		TypedQuery<T> allQuery = em.createQuery(all);
		List<T> entities = allQuery.getResultList();
		em.close();
		return entities;
	}

	/**
	 * Gets the first entity of a list if field-value matches
	 * @param type the class of the entity
	 * @param field the field to look for the value
	 * @param value the value which is looked in the field
	 * @return the first element of a list of filtered entities
	 */
	public <T> T getFirstByFieldValue(Class<T> type, String field, Long value) {
		// test
		List<T> entities = getByFieldValue(type, field, value);
		if (entities.isEmpty()) {
			return null;
		} else {
			return entities.get(0);
		}
	}
	
	/**
	 * Gets the first entity of a list if field-value matches
	 * @param type the class of the entity
	 * @param field the field to look for the value
	 * @param value the value which is looked in the field
	 * @param orderField the field which is used to order the results
	 * @return the first element of a list of filtered entities
	 */
	public <T> T getFirstByFieldValueOrderedDesc(Class<T> type, String field, Long value, String orderField) {
		// test
		List<T> entities = getByFieldValueOrderedDesc(type, field, value, orderField);
		if (entities.isEmpty()) {
			return null;
		} else {
			return entities.get(0);
		}
	}
	
	
	/**
	 * Gets all entities for an entity class
	 * @param type the class of the entity
	 * @return a list with all entities
	 */
	public <T> List<T> getAll(Class<T> type) {
		// use criteria instead of a class specific NamedQuery for this GenericDao
		EntityManager em = getEntityManager();
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<T> criteriaQuery = builder.createQuery(type);
		Root<T> rootEntry = criteriaQuery.from(type);
        CriteriaQuery<T> all = criteriaQuery.select(rootEntry);
		TypedQuery<T> allQuery = em.createQuery(all);
		List<T> entities = allQuery.getResultList();
		em.close();
		return entities;
	}
	
	
	/**
	 * Updates the given entity in the databse
	 * @param o the object to update
	 * @return the updated object 
	 */
	public <T> T update(T o) {
		EntityManager em = getEntityManager();
		em.getTransaction().begin();
		T result = em.merge(o);
		em.getTransaction().commit();
		em.close();
		return result;
	}
	
	/**
	 * Deletes a given entity from the database
	 * WARNING: THIS IS UNTESTED
	 * @param o the object to delete
	 */
	/*public void delete(Object o) {
		EntityManager em = getEntityManager();
		em.getTransaction().begin();
		em.remove(o);
		em.getTransaction().commit();
		em.close();
	}*/
	
	/**
	 * Deletes a entity from the datbase wit the given id
	 * @param type the class of the entity
	 * @param id the id to delete
	 */
	public <T> void deleteById(Class<T> type, Long id) {
		EntityManager em = getEntityManager();
		em.getTransaction().begin();
        T result = em.find(type, id);
		em.remove(result);
		em.getTransaction().commit();
		em.close();
	}
	
	/**
	 * Delete entities for an entity class if field-value matches
	 * @param type the class of the entity
	 * @return a list with all filtered entities
	 */
	public <T> void deleteByFieldValue(Class<T> type, String field, Long value) {
		// needs testing
		EntityManager em = getEntityManager();
		em.getTransaction().begin();
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<T> criteriaQuery = builder.createQuery(type);
		Root<T> rootEntry = criteriaQuery.from(type);
        CriteriaQuery<T> all = criteriaQuery.select(rootEntry);
        all.where(builder.equal(rootEntry.get(field), value));
		TypedQuery<T> allQuery = em.createQuery(all);
		List<T> entities = allQuery.getResultList();
		for(T entity: entities) {
			em.remove(entity);
		}
		em.getTransaction().commit();
		em.close();
	}
}
