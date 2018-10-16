package hamaster.gradesign.servlet;

import hamaster.gradesign.client.Encoder;
import hamaster.gradesign.daemon.EJBClient;
import hamaster.gradesign.mail.IBEMailParameterGenerator;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ActivationServlet
 */
@WebServlet("/active")
public class ActivationServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private IBEMailParameterGenerator mailParameterGenerator;
    private EJBClient client;

    /**
        * @see HttpServlet#HttpServlet()
        */
    public ActivationServlet() {
    }

    /*
    * (non-Javadoc)
    * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
    */
    public void init(ServletConfig config) throws ServletException {
        client = EJBClient.getInstance();
        mailParameterGenerator = (IBEMailParameterGenerator) client.getBean("mailParameterGenerator");
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String ct = request.getParameter(IBEMailParameterGenerator.CONTENT_KEY);
        String sg = request.getParameter(IBEMailParameterGenerator.SIGNATURE_KEY);
        if (ct == null || sg == null)
            return;
        ct = ct.replace('*', '+');
        ct = ct.replace('-', '/');
        sg = sg.replace('*', '+');
        sg = sg.replace('-', '/');
        Encoder base64 = client.getEncoderByName("Base64");
        byte[] cont = base64.decode(ct);
        byte[] sign = base64.decode(sg);

        int ve = mailParameterGenerator.verify(cont, sign);
        PrintWriter out = response.getWriter();
        switch (ve) {
        case 0:
            out.print("激活成功");
            break;
        case 1:
            out.print("激活错误");
            break;
        case 2:
            out.print("未能在一周内激活");
            break;
        case 3:
            out.print("已经激活");
            break;
        default:
            out.print("未知错误");
        }
        out.flush();
        out.close();
    }
}
