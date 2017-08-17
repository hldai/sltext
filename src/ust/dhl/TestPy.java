package ust.dhl;

import py4j.GatewayServer;

/**
 * Created by p_hliangdai on 2017/8/11.
 */
public class TestPy {
    public String addition(String a, String b) {
        return a + b;
    }

    public static void main(String[] args) {
        TestPy app = new TestPy();
//        System.out.println(app.addition("a", "b"));
        GatewayServer server = new GatewayServer(app);
        server.start();
    }
}
