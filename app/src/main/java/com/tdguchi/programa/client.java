package com.tdguchi.programa;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;

public class client {

    private String id;
    private final String HOST = "xapi.xtb.com";
    private int PORT = 5124;
    //private final int PORT = 5112;
    private PrintWriter out;
    private BufferedReader in;
    private SSLSocket sslsocket;
    private SSLSocketFactory sslsocketfactory;
    private String response;
    private ObjectMapper mapper;
    private JsonNode root;
    private JsonNode trades;
    private List<trade> tradesList = new ArrayList<trade>();
    private Double sl;
    private Double tp;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<trade> getTradesList() {
        return this.tradesList;
    }

    public void setTradesList(List<trade> tradesList) {
        this.tradesList = tradesList;
    }

    public Double getPrice(String symbol, int cmd) throws IOException {
        String json = "{\"command\":\"getSymbol\",\"arguments\":{\"symbol\":\"" + symbol + "\"}}";
        out.println(json);
        this.response = in.readLine();
        while (this.response == null || this.response.isEmpty()) {
            this.response = in.readLine();
        }
        this.mapper = new ObjectMapper();
        this.root = this.mapper.readTree(this.response);
        this.root = root.path("returnData");
        if (cmd == 0) {
            return root.path("bid").asDouble();
        } else {
            return root.path("ask").asDouble();
        }
    }

    public void connect() throws IOException {
        this.sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        this.sslsocket = (SSLSocket) sslsocketfactory.createSocket(HOST, PORT);
        this.sslsocket.setKeepAlive(true);
        this.out = new PrintWriter(sslsocket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(sslsocket.getInputStream()));
    }

    public void ping() throws IOException {
        String json = "{\"command\":\"ping\"}";
        this.out.println(json);
        this.response = in.readLine();
        while (this.response == null || this.response.isEmpty()) {
            this.response = in.readLine();
        }
        System.out.println(this.response);
    }

    public String login(String userId, String password) throws IOException, InterruptedException {
        connect();
        Thread.sleep(100);
        String json = "{\"command\":\"login\",\"arguments\":{\"userId\":\"" + userId + "\",\"password\":\"" + password
                + "\"}}";
        out.println(json);
        this.response = in.readLine();
        while (this.response == null || this.response.isEmpty()) {
            this.response = in.readLine();
        }
        this.mapper = new ObjectMapper();
        this.root = this.mapper.readTree(this.response);
        this.id = root.path("streamSessionId").asText();

        return this.response;

    }

    public List<trade> getTrades() throws JsonMappingException, JsonProcessingException {
        String json = "{\"command\": \"getTrades\",\"arguments\": {\"openedOnly\": true}}";
        out.println(json);
        try {
            this.response = in.readLine();
            while (this.response == null || this.response.isEmpty()) {
                this.response = in.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.mapper = new ObjectMapper();
        this.root = this.mapper.readTree(this.response);
        this.trades = root.path("returnData");
        this.tradesList.clear();
        for (JsonNode trade : trades) {
            int comment = trade.get("comment").asInt();
            int cmd = trade.get("cmd").asInt();
            double sl = trade.get("sl").asDouble();
            double tp = trade.get("tp").asDouble();
            int position = trade.get("position").asInt();
            this.tradesList.add(new trade(cmd, comment, sl, tp, position));
        }

        return this.tradesList;
    }

    public void TradeTransaction(int cmd, int position, String symbol, int type, Double volumen, Double sl, Double tp,
                                 int comentario)
            throws NumberFormatException, IOException {
        Double price = getPrice(symbol, cmd);

        this.sl = 0.0;
        this.tp = 0.0;
        if (cmd == 0 && type == 0) {
            this.sl = price - sl;
        } else if (cmd == 1 && type == 0) {
            this.sl = price + sl;
        } else {
            this.sl = sl;
            this.tp = tp;
        }
        String json = "{\"command\":\"tradeTransaction\",\"arguments\":{\"tradeTransInfo\":{\"cmd\":"
                + cmd + ",\"comment\":\""
                + comentario + "\",\"expiration\":3462006335000,\"offset\":0,\"order\":"
                + position + ",\"price\":"
                + price + ",\"sl\":"
                + this.sl + ",\"symbol\":\""
                + symbol + "\",\"tp\":"
                + this.tp + ",\"type\":"
                + type + ",\"volume\":"
                + volumen + "}}}";
        out.println(json);
        try {
            this.response = in.readLine();
            while (this.response == null || this.response.isEmpty()) {
                this.response = in.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}