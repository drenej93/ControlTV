package com.ivan.smarttvremote;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;
import com.ionicframework.capacitor.zeroconf.Zeroconf;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(Zeroconf.class);
        super.onCreate(savedInstanceState);
    }
}
