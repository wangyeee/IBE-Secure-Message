package hamaster.gradesign.keydist.daemon;

import static java.util.Objects.requireNonNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 执行web容器启动时的初始化操作
 * @author <a href="mailto:wangyeee@gmail.com">Wang Ye</a>
 */
@Component
public class Init {

    private KeyGenClient system;
    private Runnable ibeRequestHandlerDaemon;
    private Runnable mailDaemon;

    @Autowired
    public Init(KeyGenClient system, @Qualifier("ibeRequestHandlerDaemon") Runnable ibeRequestHandlerDaemon, @Qualifier("ibeMailDaemon") Runnable mailDaemon) {
        this.system = requireNonNull(system);
        this.ibeRequestHandlerDaemon = requireNonNull(ibeRequestHandlerDaemon);
        this.mailDaemon = requireNonNull(mailDaemon);
    }

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        system.init();
        Thread requestDaemon = new Thread(this.ibeRequestHandlerDaemon);
        requestDaemon.setName("[IBERequestHandlerDaemon]");
        requestDaemon.start();

        Thread mailDaemon = new Thread(this.mailDaemon);
        mailDaemon.setName("[IBEMailDaemon]");
        mailDaemon.start();
    }
}
