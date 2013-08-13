package hamaster.gradesign.daemon;

import hamaster.gradesign.mail.IBEMailDaemon;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * 执行web容器启动时的初始化操作
 * @author <a href="mailto:wangyeee@gmail.com">Wang Ye</a>
 */
@WebListener
public class Init implements ServletContextListener {

    public Init() {
    }

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
    public void contextInitialized(ServletContextEvent e) {
    	EJBClient system = EJBClient.getInstance();
    	system.init();
    	Runnable target = system.getBean("ibeRequestHandlerDaemon", IBERequestHandlerDaemon.class);
    	Thread requestDaemon = new Thread(target);
    	requestDaemon.setName("[IBERequestHandlerDaemon]");
    	requestDaemon.start();
    	
    	Runnable mTarget = system.getBean("mailDaemon", IBEMailDaemon.class);
    	Thread mailDaemon = new Thread(mTarget);
    	mailDaemon.setName("[IBEMailDaemon]");
    	mailDaemon.start();
    }

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
    public void contextDestroyed(ServletContextEvent e) {
    }
}
