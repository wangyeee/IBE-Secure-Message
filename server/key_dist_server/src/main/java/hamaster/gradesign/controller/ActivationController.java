package hamaster.gradesign.controller;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hamaster.gradesign.client.Encoder;
import hamaster.gradesign.mail.IBEMailParameterGenerator;

@RestController
public class ActivationController {

    private IBEMailParameterGenerator mailParameterGenerator;

    private Encoder base64;

    @Autowired
    public ActivationController(IBEMailParameterGenerator mailParameterGenerator, @Qualifier("base64Encoder") Encoder base64) {
        this.mailParameterGenerator = requireNonNull(mailParameterGenerator);
        this.base64 = requireNonNull(base64);
    }

    @GetMapping("/api/active")
    public Map<String, String> doActivate(@RequestParam(value = IBEMailParameterGenerator.CONTENT_KEY, required = true) String ct,
            @RequestParam(value = IBEMailParameterGenerator.SIGNATURE_KEY, required = true) String sg) {
        Map<String, String> result = new HashMap<String, String>();
        ct = ct.replace('*', '+');
        ct = ct.replace('-', '/');
        sg = sg.replace('*', '+');
        sg = sg.replace('-', '/');
        byte[] cont = base64.decode(ct);
        byte[] sign = base64.decode(sg);

        int ve = mailParameterGenerator.verify(cont, sign);
        result.put("RESULT", Integer.toString(ve));
        switch (ve) {
        case 0:
            result.put("MESSAGE", "Successfully activated.");
            //out.print("激活成功");
            break;
        case 1:
            result.put("MESSAGE", "Error occurred when trying to activate.");
            //out.print("激活错误");
            break;
        case 2:
            result.put("MESSAGE", "Activation token expired.");
            //out.print("未能在一周内激活");
            break;
        case 3:
            result.put("MESSAGE", "Already activated.");
            //out.print("已经激活");
            break;
        default:
            result.put("MESSAGE", "Unknown error.");
            //out.print("未知错误");
        }
        return result;
    }
}
