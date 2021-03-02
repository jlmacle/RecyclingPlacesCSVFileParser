package parser;

import java.nio.file.FileSystems;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;


public class HibernateConnectionUtil {
	
	private HibernateConnectionUtil()
	{
		throw new IllegalStateException("Utility class with static methods and attributes.");
	}
	
	private static SessionFactory sessionFactory;
	
	public static void configureHibernate() {
		
		Configuration configuration = new Configuration().configure()
				.addFile(FileSystems.getDefault().getPath("src/main/resources/hibernate.cfg.xml").toString())
				.setProperty("hibernate.connection.url", System.getenv("DB_URL") )
				.setProperty("hibernate.connection.username", System.getenv("DB_USERNAME"))
				.setProperty("hibernate.connection.password", System.getenv("DB_PASSWORD"))
				.addAnnotatedClass(Entry.class);
		
		ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
				.applySettings(configuration.getProperties()).build();
		
		sessionFactory = configuration.buildSessionFactory(serviceRegistry);
	}
	
	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	
	public static void shutdownSession() {
		
		if(sessionFactory!= null &&  !sessionFactory.isClosed()) {sessionFactory.close();}
	}
}
