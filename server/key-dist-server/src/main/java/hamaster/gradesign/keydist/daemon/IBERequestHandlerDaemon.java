package hamaster.gradesign.keydist.daemon;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import hamaster.gradesgin.ibe.core.IBEEngine;
import hamaster.gradesgin.ibs.IBSCertificate;
import hamaster.gradesgin.ibs.IBSSignature;
import hamaster.gradesign.keydist.entity.IDRequest;
import hamaster.gradesign.keydist.service.IDRequestService;
import hamaster.gradesign.keygen.IBECSR;

@Component("ibeRequestHandlerDaemon")
public class IBERequestHandlerDaemon implements Runnable {

    private Logger logger = LoggerFactory.getLogger(getClass());

    final private static long YEAR = 31536000000l;

    private IDRequestService idRequestService;

    private RestTemplate restTemplate;

    private KeyGenClient system;

    /**
     * 处理请求间隔 单位毫秒
     */
    @Value("${hamaster.gradesign.keydist.req.interval:60000}")
    private long interval;

    /**
     * 每次处理的请求数量
     */
    @Value("${hamaster.gradesign.keydist.req.idbat:50}")
    private int batchSize;

    private volatile boolean running;

    @Autowired
    public IBERequestHandlerDaemon(RestTemplateBuilder restTemplateBuilder, IDRequestService idRequestDAO, KeyGenClient system) {
        running = true;
        this.restTemplate = requireNonNull(restTemplateBuilder).build();
        this.idRequestService = requireNonNull(idRequestDAO);
        this.system = requireNonNull(system);
    }

    @Override
    public void run() {
        logger.info("Daemon started at:" + new Date().toString());
        while (running) {
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {}
            List<IDRequest> userWork = idRequestService.listUnhandledRequests(batchSize);
            if (userWork.size() == 0)
                continue;
            logger.info("Submitting %d ID requests", userWork.size());
            List<IBECSR> work = new ArrayList<IBECSR>(userWork.size());
            for (IDRequest idRequest : userWork) {
                IBECSR csr = convert(idRequest);
                work.add(csr);
            }
            Map<String, Integer> results = generateIdentityDescriptions(work);
            logger.info("%d responses received for ID requests", results.size());
            idRequestService.requestHandled(results);
        }
        logger.info("Daemon extied at:" + new Date().toString());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Map<String, Integer> generateIdentityDescriptions(List<IBECSR> work) {
        ResponseEntity<Map> response = restTemplate.postForEntity(String.format("%s/genidsync", system.getKeyGenServereURL()), work, Map.class);
        if (response.hasBody())
            return response.getBody();
        return new HashMap<String, Integer>();
    }

    private IBECSR convert(IDRequest request) {
        IBECSR csr = new IBECSR();
        csr.setApplicationDate(request.getApplicationDate());
        csr.setIbeSystemId(request.getIbeSystemId());
        csr.setIdentityString(request.getIdentityString());
        csr.setPassword(request.getPasswordToKeyGen());
        csr.setPeriod(YEAR); // TODO a year
        csr.setRequestId(request.getRequestId());
        IBSCertificate certificate = system.serverCertificate();
        if (certificate != null) {
            IBSSignature signature = IBEEngine.sign(certificate, csr.getDigest(), "SHA-512");
            csr.setSignature(signature.toByteArray());
        }
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

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
}
