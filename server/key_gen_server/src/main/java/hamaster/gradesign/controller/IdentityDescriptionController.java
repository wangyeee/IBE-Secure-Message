package hamaster.gradesign.controller;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import hamaster.gradesign.IBECSR;
import hamaster.gradesign.idmgmt.IdentityDescriptionBean;

@RestController
public class IdentityDescriptionController {

    private IdentityDescriptionBean identityDescriptionBean;

    @Autowired
    public IdentityDescriptionController(IdentityDescriptionBean identityDescriptionBean) {
        this.identityDescriptionBean = identityDescriptionBean;
    }

    @PostMapping("genid")
    public CompletableFuture<Map<String, Integer>> processIDGeneration(List<IBECSR> requests) throws InterruptedException, ExecutionException {
        return CompletableFuture.completedFuture(identityDescriptionBean.generateIdentityDescriptionsSync(requests));
    }

    /**
     * This method is for debug only, will be removed later.
     * @return an example IBECSR object
     */
    @GetMapping("samplereq")
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
