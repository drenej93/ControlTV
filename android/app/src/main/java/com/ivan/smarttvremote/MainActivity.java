package com.ivan.smarttvremote;

import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import com.getcapacitor.BridgeActivity;

import java.io.OutputStream;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class MainActivity extends BridgeActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Exponemos el puente nativo hacia JavaScript bajo el nombre "AndroidTVBridge"
        WebView webView = getBridge().getWebView();
        webView.addJavascriptInterface(new TVBridge(), "AndroidTVBridge");
    }

    public class TVBridge {
        @JavascriptInterface
        public void requestPairingPin(final String ipAddress) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Creamos un contexto SSL que confíe en el certificado autofirmado del televisor
                        TrustManager[] trustAllCerts = new TrustManager[]{
                            new X509TrustManager() {
                                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                                public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                            }
                        };

                        SSLContext sc = SSLContext.getInstance("TLS");
                        sc.init(null, trustAllCerts, new SecureRandom());
                        SSLSocketFactory factory = sc.getSocketFactory();

                        // Conectamos al puerto oficial de emparejamiento de Google TV 11 (6467)
                        SSLSocket socket = (SSLSocket) factory.createSocket(ipAddress, 6467);
                        socket.startHandshake();

                        // Enviamos el paquete inicial de solicitud de emparejamiento (Android TV Remote v2)
                        OutputStream out = socket.getOutputStream();
                        byte[] pairingRequest = new byte[]{
                            0x08, 0x02, 0x10, (byte) 0xc8, 0x01, 0x52, 0x0f, 0x0a, 0x0d,
                            'S', 'm', 'a', 'r', 't', 'T', 'V', 'R', 'e', 'm', 'o', 't', 'e'
                        };
                        out.write(pairingRequest);
                        out.flush();

                        // Mantenemos el socket abierto brevemente para que el TV despliegue el PIN en pantalla
                        Thread.sleep(15000);
                        socket.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
}
