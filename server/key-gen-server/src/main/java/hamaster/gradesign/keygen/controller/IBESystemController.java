package hamaster.gradesign.keygen.controller;

import static java.util.Objects.requireNonNull;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import hamaster.gradesgin.ibe.IBEPublicParameter;
import hamaster.gradesign.keygen.SimpleRESTResponse;
import hamaster.gradesign.keygen.idmgmt.IBESystemBean;

@RestController
public class IBESystemController {

    private final static Logger logger = LoggerFactory.getLogger(IBESystemController.class);

    private IBESystemBean ibeSystem;

    @Autowired
    public IBESystemController(IBESystemBean ibeSystem) {
        this.ibeSystem = requireNonNull(ibeSystem);
    }

    // generate a demo system for testing
    @GetMapping("/demo")
    public Map<Integer, String> setupDemo() {
        if (ibeSystem.totalSystems() == 0L)
            ibeSystem.generateDefaultSystem();
        return ibeSystem.list(0, 1);
    }

    @GetMapping("/system/default")
    public Map<Integer, String> defaultSystem() {
        return ibeSystem.list(0, 1);
    }

    @GetMapping("/system/all")
    public Map<Integer, String> allSystem() {
        return ibeSystem.listAll();
    }

    @GetMapping("/system/allparam")
    public Map<Integer, IBEPublicParameter> allSystemParameters() {
        return ibeSystem.listAllParameters();
    }

    @GetMapping("/system/{owner}/number")
    public SimpleRESTResponse getSystemIDNumber(@PathVariable(value = "owner", required = true) String owner) {
        SimpleRESTResponse resp = new SimpleRESTResponse();
        Integer id = ibeSystem.getIDByName(owner);
        if (id > 0) {
            resp.setPayload(id);
            resp.setResultCode(0);
            resp.setMessage(String.format("System ID found for %s", owner));
        } else {
            resp.setResultCode(1);
            resp.setMessage(String.format("System ID not found for %s", owner));
        }
        return resp;
    }

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        logger.info("Key generation server startup.");
        if (ibeSystem.totalSystems() == 0L) {
            logger.info("Generating default system parameters.");
            ibeSystem.generateDefaultSystem();
        }
    }
}
