package io.github.zivasd.spring.boot.data.shared.repository.support;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.lang.Nullable;

import io.github.zivasd.spring.boot.data.shared.repository.SharedRepository;

public class SharedRepositoryFactoryBean<T extends SharedRepository> extends RepositoryFactoryBeanSupport<T, Void, Void> {
	private static final String DEFAULT_TRANSACTION_MANAGER_BEAN_NAME = "transactionManager";
	
	private @Nullable EntityManager entityManager;
	private String transactionManagerName = DEFAULT_TRANSACTION_MANAGER_BEAN_NAME;
	private boolean enableDefaultTransactions = false;

	protected SharedRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
		super(repositoryInterface);
	}

	@Override
	protected RepositoryFactorySupport createRepositoryFactory() {
		return new SharedRepositoryFactory(entityManager);
	}

	@PersistenceContext
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
	
	public void setTransactionManager(String transactionManager) {
		this.transactionManagerName = transactionManager == null ? DEFAULT_TRANSACTION_MANAGER_BEAN_NAME : transactionManager;
	}
	
	public void setEnableDefaultTransactions(boolean enableDefaultTransactions) {
		this.enableDefaultTransactions = enableDefaultTransactions;
	}
	
	protected String getTransactionManagerName() {
		return transactionManagerName;
	}
	
	protected boolean isDefaultTransactionManager() {
		return enableDefaultTransactions;
	}
}
