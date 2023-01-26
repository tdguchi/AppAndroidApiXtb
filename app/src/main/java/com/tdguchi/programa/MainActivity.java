package com.tdguchi.programa;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    client pablo;
    client marta;
    client papa;
    private static List<client> clients = new ArrayList<>();
    private Handler pingHandler = new Handler();
    private static Double dttp;
    private static Double dssl;
    private List<trade> tradesList;
    private double sl = 30.0;
    private double tp = 0.0;
    private static final String indice = "US100";
    private static final double volumen = 0.02;
    private Button btnlargo;
    private Button btncorto;
    private Button btnclose;
    private Button btnmod;
    private Button btnref;
    private EditText etsl;
    private EditText ettp;
    private int comentario = 0;

    private Runnable pingRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                pablo.ping();
                marta.ping();
                papa.ping();
            } catch (IOException e) {
                e.printStackTrace();
            }
            pingHandler.postDelayed(this, 5 * 60 * 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnlargo = findViewById(R.id.largo);
        btncorto = findViewById(R.id.corto);
        btnclose = findViewById(R.id.cerrar);
        btnmod = findViewById(R.id.modificar);
        btnref = findViewById(R.id.refrescar);
        etsl = findViewById(R.id.etsl);
        ettp = findViewById(R.id.ettp);
        pablo = new client();
        marta = new client();
        papa = new client();

        new login().execute();

        clients.add(pablo);
        clients.add(marta);
        clients.add(papa);

        //pingHandler.post(pingRunnable);


        btnlargo.setOnLongClickListener(view -> {
            operacion(0);
            return false;
        });

        btncorto.setOnLongClickListener(view -> {
           operacion(1);
            return false;
        });

        btnclose.setOnClickListener(view -> {
            try {
                for (client client : clients) {
                    Thread hilo = new Thread(() -> {
                        try {
                            List<trade> trades = client.getTradesList();
                            for (trade trade : trades) {
                                //if (trade.getChecked()) {
                                    client.TradeTransaction(trade.getCmd(), trade.getPosition(), indice, 2, volumen,
                                            trade.getSl(), trade.getTp(), trade.getComment());
                                    Thread.sleep(100);
                                //}
                            }
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
                    hilo.start();
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            try {
                Thread.sleep(3000);
                mostrarTrades();
            } catch (JsonProcessingException | InterruptedException e) {
                e.printStackTrace();
            }
        });

        btnmod.setOnClickListener(view -> {
            try {
                String ssl = etsl.getText().toString();
                String ttp = ettp.getText().toString();
                System.out.println(ssl);
                if (ssl.equals("0.00") || ssl.isEmpty()) {
                    dssl = 0.00;
                } else {
                    dssl = Double.parseDouble(ssl);
                }
                if (ttp.equals("0.00") || ttp.isEmpty()) {
                    dttp = 0.00;
                } else {
                    dttp = Double.parseDouble(ttp);
                }
                for (client client : clients) {
                    Thread hilo = new Thread(() -> {
                        try {
                            List<trade> trades = client.getTradesList();
                            for (trade trade : trades) {
                                if (dssl != 0.00) {
                                    sl = dssl;
                                } else {
                                    sl = trade.getSl();
                                }
                                if (dttp != 0.00) {
                                    tp = dttp;
                                } else {
                                    tp = trade.getTp();
                                }
                                System.out.println(sl);
                                //if (trade.getChecked()) {
                                    client.TradeTransaction(trade.getCmd(), trade.getPosition(), indice, 3, volumen,
                                            sl, tp, trade.getComment());
                                    Thread.sleep(100);
                                //}
                            }
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
                    hilo.start();
                    hilo.join();
                }

            } catch (Exception e1) {
                e1.printStackTrace();
            }
            try {
                Thread.sleep(3000);
                mostrarTrades();
            } catch (JsonProcessingException | InterruptedException e) {
                e.printStackTrace();
            }
        });

        btnref.setOnClickListener(view -> {
            for (client client : clients) {
                Thread hilo = new Thread(() -> {
                    try {
                        client.getTrades();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                hilo.start();
            }
        });





    }
    private class login extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                pablo.login();
                marta.login();
                papa.login();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    private void operacion(int cmd) {
        try {
            for (client client : clients) {
                Thread hilo = new Thread() {
                    public void run() {
                        try {
                            client.TradeTransaction(cmd, 0, indice, 0, volumen, sl, tp, comentario);
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };
                hilo.start();
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }


    private void mostrarTrades() throws JsonProcessingException {
        for (client client : clients) {
            tradesList = client.getTradesList();
            for (trade trade : tradesList) {
                System.out.println(trade.toString());
            }
        }
    }
}