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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import hamaster.gradesgin.util.Hex;
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
    public CompletableFuture<Map<String, Integer>> processIDGeneration(@RequestBody(required = true) List<IBECSR> requests) throws InterruptedException, ExecutionException {
        return CompletableFuture.completedFuture(identityDescriptionBean.generateIdentityDescriptionsSync(requests));
    }

    @PostMapping("/singleid")
    public SimpleRESTResponse singleIDRequest(@RequestBody(required = true) IBECSR request) {
        SimpleRESTResponse resp = new SimpleRESTResponse();
        request = requireNonNull(request);
        IdentityDescriptionEntity exist = identityDescriptionBean.get(request.getIdentityString());
        if (exist != null) {
            if (request.getIbeSystemId() == exist.getSystem().getSystemId()) {
                resp.setResultCode(1);
                resp.setMessage(String.format("Error: conflicting ID: %s for system: %d", request.getIdentityString(), request.getIbeSystemId()));
                return resp;
            }
        }
        IdentityDescriptionEntity newId = identityDescriptionBean.generateSingleIdentityDescriptionEntity(request);
        resp.setResultCode(0);
        resp.setMessage(String.format("Successfully generated ID for %s in system %s", request.getIdentityString(), newId.getSystem().getSystemOwner()));
        resp.setPayload(Hex.hex(newId.getEncryptedIdentityDescription()));
        return resp;
    }

    @PostMapping("/genidsync")
    public Map<String, Integer> processIDGenerationSync(@RequestBody(required = true) List<IBECSR> requests) {
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
        csr.setPassword("some password".getBytes());
        csr.setPeriod(100000000000L);
        csr.setApplicationDate(new Date());
        return csr;
    }
}
