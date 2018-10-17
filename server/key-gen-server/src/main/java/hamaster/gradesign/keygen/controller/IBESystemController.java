package hamaster.gradesign.keygen.controller;

import static java.util.Objects.requireNonNull;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import hamaster.gradesign.keygen.SimpleRESTResponse;
import hamaster.gradesign.keygen.idmgmt.IBESystemBean;

@RestController
public class IBESystemController {

    private IBESystemBean ibeSystem;

    @Autowired
    public IBESystemController(IBESystemBean ibeSystem) {
        this.ibeSystem = requireNonNull(ibeSystem);
    }

    // generate a demo system for testing
    @GetMapping("/demo")
    public Map<Integer, String> setupDemo() {
        if (ibeSystem.totalSystems() == 0L)
            ibeSystem.generateDemoSystem();
        return ibeSystem.list(0, 1);
    }

    @GetMapping("/system/default")
    public Map<Integer, String> defaultSystem() {
        return ibeSystem.list(0, 1);
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
}
