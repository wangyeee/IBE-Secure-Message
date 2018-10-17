package hamaster.gradesign.keydist.mail;

import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import hamaster.gradesign.keydist.dao.UserDAO;
import hamaster.gradesign.keydist.entity.IDRequest;
import hamaster.gradesign.keydist.entity.User;
import hamaster.gradesign.keydist.service.IDRequestService;

@Component("ibeMailDaemon")
public class IBEMailDaemon implements Runnable {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private IBEMailParameterGenerator mailParameterGenerator;
    private JavaMailSender mailSender;
    private IDRequestService idRequestDAO;
    private UserDAO userDAO;

    @Value("${hamaster.gradesign.keydist.mail.userbat:20}")
    private int userBatchSize;

    @Value("${hamaster.gradesign.keydist.mail.idbat:50}")
    private int idBatchSize;

    @Value("${hamaster.gradesign.keydist.mail.interval:60000}")
    private long interval;

    private volatile boolean running;

    @Autowired
    public IBEMailDaemon(IBEMailParameterGenerator mailParameterGenerator, JavaMailSender mailSender,
            IDRequestService idRequestDAO, UserDAO userDAO) {
        running = true;
        this.mailParameterGenerator = mailParameterGenerator;
        this.mailSender = mailSender;
        this.idRequestDAO = idRequestDAO;
        this.userDAO = userDAO;
    }

    @Override
    public void run() {
        logger.info("IBE Mail Daemon active at:" + new Date().toString());
        while (running) {
            sendingMail();
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        logger.info("IBE Mail Daemon exit at:" + new Date().toString());
    }

    public void stopRunning() {
        this.running = false;
    }

    private void sendingMail() {
        List<User> newUsers = userDAO.listNewRegisteredUsers(userBatchSize);
        List<IDRequest> newRequests = idRequestDAO.listNewRequests(idBatchSize);
        int i = newUsers.size() + newRequests.size();
        if (i == 0)
            return;
        SimpleMailMessage[] mails = new SimpleMailMessage[i];
        i = 0;

        for (User user : newUsers) {
            StringBuilder content = new StringBuilder("请将如下链接复制到浏览器中打开：\r\nhttp://localhost:8080/ibeserver/active?");
            ActivationContent act = new ActivationContent(user);
            Properties props = mailParameterGenerator.sign(act);
            for (Object key : props.keySet()) {
                content.append(key.toString());
                content.append('=');
                content.append(props.getProperty(key.toString()));
                content.append('&');
            }
            content.deleteCharAt(content.length() - 1);
            content.append("\r\n");

            System.out.println(content.toString());// TODO debug

            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom("wangyeee@gmail.com");
            mail.setTo(user.getEmail());
            mail.setSubject("IBE用户激活");
            mail.setText(content.toString());
            mails[i] = mail;
            i++;
        }
        for (IDRequest idRequest : newRequests) {
            StringBuilder content = new StringBuilder("请将如下链接复制到浏览器中打开：\r\nhttp://localhost:8080/ibeserver/active?");
            ActivationContent act = new ActivationContent(idRequest);
            Properties props = mailParameterGenerator.sign(act);
            for (Object key : props.keySet()) {
                content.append(key.toString());
                content.append('=');
                content.append(props.getProperty(key.toString()));
                content.append('&');
            }
            content.deleteCharAt(content.length() - 1);
            content.append("\r\n");
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom("wangyeee@gmail.com");
            mail.setTo(idRequest.getIdentityString());
            mail.setSubject("IBE身份验证");
            mail.setText(content.toString());
            mails[i] = mail;
            i++;
        }
        mailSender.send(mails);
    }

    public void stopMailDaemon() {
        running = false;
    }
}
