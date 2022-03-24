package minicpbp.bridge;

import py4j.GatewayServer;


public class EntryPoint {

    public VisualSudoku visualSudoku() {
        return new VisualSudoku();
    }

    public PartialLatinSquare partialLatinSquare(){
        return new PartialLatinSquare();
    }

    public String test() {
        String str = "Bridge is working";
        System.out.println(str);
        return str;
    }

    public static void main(String[] args) {
        GatewayServer gatewayServer = new GatewayServer(new EntryPoint());
        gatewayServer.start();
        System.out.println("Gateway Server Started");
    }
}
