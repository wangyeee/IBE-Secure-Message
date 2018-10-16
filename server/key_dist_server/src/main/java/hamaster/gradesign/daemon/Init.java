package hamaster.gradesign.daemon;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import hamaster.gradesign.mail.IBEMailDaemon;

/**
 * 执行web容器启动时的初始化操作
 * @author <a href="mailto:wangyeee@gmail.com">Wang Ye</a>
 */
@Component
public class Init {

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        System.err.println("System start...");
        EJBClient system = EJBClient.getInstance();
        system.init();
        Runnable target = system.getBean("ibeRequestHandlerDaemon", IBERequestHandlerDaemon.class);
        if (target != null) {
            Thread requestDaemon = new Thread(target);
            requestDaemon.setName("[IBERequestHandlerDaemon]");
            requestDaemon.start();
        }

        Runnable mTarget = system.getBean("mailDaemon", IBEMailDaemon.class);
        if (mTarget != null) {
            Thread mailDaemon = new Thread(mTarget);
            mailDaemon.setName("[IBEMailDaemon]");
            mailDaemon.start();
        }
    }
}
