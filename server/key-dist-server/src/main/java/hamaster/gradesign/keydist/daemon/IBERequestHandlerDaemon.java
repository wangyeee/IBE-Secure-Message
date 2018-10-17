package hamaster.gradesign.keydist.daemon;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import hamaster.gradesign.keydist.entity.IDRequest;
import hamaster.gradesign.keydist.service.IDRequestService;
import hamaster.gradesign.keygen.IBECSR;
import hamaster.gradesign.keygen.idmgmt.IdentityDescriptionBean;

@Component("ibeRequestHandlerDaemon")
public class IBERequestHandlerDaemon implements Runnable {

    private Logger logger = LoggerFactory.getLogger(getClass());

    final private static long YEAR = 31536000000l;

    private IDRequestService idRequestDAO;

    private IdentityDescriptionBean identityDescriptionBean;

    /**
     * 处理请求间隔 单位毫秒
     */
    private long interval;

    /**
     * 每次处理的请求数量
     */
    private int batchSize;

    private volatile boolean running;

    public IBERequestHandlerDaemon() {
        running = true;
    }

    @Override
    public void run() {
        logger.info("Daemod started at:" + new Date().toString());
        while (running) {
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {}
            List<IDRequest> userWork = idRequestDAO.listUnhandledRequests(batchSize);
            if (userWork.size() == 0)
                continue;
            List<IBECSR> work = new ArrayList<IBECSR>(userWork.size());
            for (IDRequest idRequest : userWork) {
                IBECSR csr = convert(idRequest);
                work.add(csr);
            }
            Future<Map<String, Integer>> future = identityDescriptionBean.generateIdentityDescriptions(work);
            try {
                Map<String, Integer> results = future.get();
                idRequestDAO.requestHandled(results);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    private IBECSR convert(IDRequest request) {
        IBECSR csr = new IBECSR();
        csr.setApplicationDate(request.getApplicationDate());
        csr.setIbeSystemId(request.getIbeSystemId());
        csr.setIdentityString(request.getIdentityString());
        csr.setPassword(request.getPassword());
        csr.setPeriod(YEAR); // TODO a year
        csr.setRequestId(request.getRequestId());
        return csr;
    }

    public void stopHandler() {
        this.running = false;
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public IdentityDescriptionBean getIdentityDescriptionBean() {
        return identityDescriptionBean;
    }

    public void setIdentityDescriptionBean(IdentityDescriptionBean identityDescriptionBean) {
        this.identityDescriptionBean = identityDescriptionBean;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
}
