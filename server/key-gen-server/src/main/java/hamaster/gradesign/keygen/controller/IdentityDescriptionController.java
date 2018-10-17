package hamaster.gradesign.keygen.controller;

import static java.util.Objects.requireNonNull;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import hamaster.gradesign.keygen.IBECSR;
import hamaster.gradesign.keygen.SimpleRESTResponse;
import hamaster.gradesign.keygen.entity.IdentityDescriptionEntity;
import hamaster.gradesign.keygen.idmgmt.IdentityDescriptionBean;

@RestController
public class IdentityDescriptionController {

    private IdentityDescriptionBean identityDescriptionBean;

    @Autowired
    public IdentityDescriptionController(IdentityDescriptionBean identityDescriptionBean) {
        this.identityDescriptionBean = requireNonNull(identityDescriptionBean);
    }

    @PostMapping("/genid")
    public CompletableFuture<Map<String, Integer>> processIDGeneration(List<IBECSR> requests) throws InterruptedException, ExecutionException {
        return CompletableFuture.completedFuture(identityDescriptionBean.generateIdentityDescriptionsSync(requests));
    }

    @PostMapping("/genidsync")
    public Map<String, Integer> processIDGenerationSync(List<IBECSR> requests) {
        return identityDescriptionBean.generateIdentityDescriptionsSync(requests);
    }

    @GetMapping("/chkid/{system}/{id}")
    public SimpleRESTResponse checkIDStatus(@PathVariable(value = "system", required = true) String system,
            @PathVariable(value = "id", required = true) String id) {
        SimpleRESTResponse resp = new SimpleRESTResponse();
        IdentityDescriptionEntity entity = identityDescriptionBean.get(id, system);
        if (entity == null) {
            resp.setResultCode(0);
            resp.setMessage(String.format("No ID records for %s found in system: %s.", id, system));
        } else {
            resp.setResultCode(1);
            resp.setMessage(String.format("ID records for %s found in system: %s.", id, system));
        }
        return resp;
    }

    /**
     * This method is for debug only, will be removed later.
     * @return an example IBECSR object
     */
    @GetMapping("/samplereq")
    public IBECSR sampleCSR() {
        IBECSR csr = new IBECSR();
        csr.setIbeSystemId(1);
        csr.setIdentityString("id string");
        csr.setPassword("some password");
        csr.setPeriod(100000000000L);
        csr.setApplicationDate(new Date());
        return csr;
    }
}
