package tsukuba.emp.mirror;

import javax.jmdns.*;
import javax.jmdns.impl.JmDNSImpl;

public class Projection {
    public static void main(String[] args) {
        JmDNS jmdns = new JmDNSImpl();
        jmdns.registerService(new ServiceInfo("_http._tcp", "idmirror.1.local.", 17353, 0, 0, "idMirror projection server"));
    }
}
