package com.example.famismartsdq.wifidemo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final boolean D = true;

    private WifiManager wifiManager;
    private WifiInfo currentWifiInfo;// 当前连接的wifi
    // 扫描结果列表
    private List<ScanResult> list_scan;// wifi列表
    private ScanResult scanResult;
    private int current_wifi_index;//当前点击的wifi索引

    private HashSet<String> hashSetWifiItems = new HashSet<String>();
    private SimpleAdapter simpleAdapter;

    private ProgressDialog progressDialog;
    private EditText edtWifiPassWord = null;

    private boolean isConnectWifiThread = false;
    private ConnectWifiThread connectWifiThread = null;

    private final String PREFERENCES_NAME = "userinfo";
    private String wifiPassword = "";
    private AlertDialog.Builder listDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        openWifi();
        currentWifiInfo = wifiManager.getConnectionInfo();
        getAllNetWorkList();
        listDialog=new AlertDialog.Builder(this);



    }

    @Override
    protected void onStart() {
        super.onStart();
        List<String> ssids=new ArrayList<>();

        for (Iterator iterator=hashSetWifiItems.iterator();iterator.hasNext();){
            ssids.add((String) iterator.next());
        }
        final String[] items=new String[ssids.size()];
        for (int i = 0; i < ssids.size(); i++) {
            items[i]=ssids.get(i);
        }
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final AlertDialog.Builder dialo=new AlertDialog.Builder(MainActivity.this);
                dialo.setTitle("连接"+items[which]);
                final EditText inputServer = new EditText(MainActivity.this);
                inputServer.setFocusable(true);
                //dialo.setView(inputServer);
                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(R.layout.access_point_dialog,
                        (ViewGroup) findViewById(R.id.dialog));
                edtWifiPassWord = (EditText) layout
                        .findViewById(R.id.edtTextWifiPassword);
                dialo.setView(layout);
                dialo.setPositiveButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        listDialog.show();
                    }
                });
                dialo.setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i("连接网络","---------->");
                        wifiPassword = edtWifiPassWord.getText()
                                .toString();
                        connectionConfiguration(which,
                                edtWifiPassWord.getText().toString());
                    }
                });
                dialo.show();
            }
        });
        listDialog.setPositiveButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        listDialog.show();
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if(D) Log.v(TAG, "msg.what = " + msg.what);
            SharedPreferences vPreferences = getSharedPreferences(
                    PREFERENCES_NAME, Activity.MODE_PRIVATE);

            SharedPreferences.Editor vEditor = vPreferences.edit();
            switch (msg.what) {
                case 0:
                    new RefreshSsidThread().start();
                    break;
                case 1:
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage("连接失败，请重新连接!")
                            .setPositiveButton("确定",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int whichButton) {
                                        }
                                    }).show();
                    break;
                case 4:
                    vEditor.putString("\"" + list_scan.get(current_wifi_index).SSID + "\"", wifiPassword);
                    vEditor.commit();
                    Toast.makeText(MainActivity.this, "连接成功 ip地址为："+ Formatter.formatIpAddress(currentWifiInfo.getIpAddress()), Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };
    public void openWifi() {
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
    }

    public void getAllNetWorkList() {
        // 每次点击扫描之前清空上一次的扫描结果
        // int vIntSecurity = -1;
        String vStr = "";
        hashSetWifiItems.clear();

        wifiManager.startScan();
        // 开始扫描网络
        list_scan = wifiManager.getScanResults();
        ArrayList<ScanResult> wifiResultlist=new ArrayList<>();
        HashSet<ScanResult> wifilist=new HashSet<>();
        if (list_scan != null) {
            HashSet<String> vSet = new HashSet<String>();
            for (int i = 0; i < list_scan.size(); i++) {
                if(D) Log.d(TAG, "listSize = " + list_scan.size());
                // 得到扫描结果
                scanResult = list_scan.get(i);
                if (Math.abs(scanResult.level) > 100) {
                    vSet.add(scanResult.SSID);
                } else if (Math.abs(scanResult.level) > 70) {
                    vSet.add(scanResult.SSID);

                } else if (Math.abs(scanResult.level) > 50) {
                    vSet.add(scanResult.SSID);

                } else {
                    vSet.add(scanResult.SSID);

                }

                Log.i(TAG,scanResult.toString());
                Log.i(TAG,"wifi数："+wifilist.size());
                int index=0;
                for (Iterator it=wifilist.iterator();it.hasNext();){
                    if (scanResult.SSID.equals(((ScanResult) it.next()).SSID)){
                        index=1;
                    }

                }
                if (index!=1){
                    wifilist.add(scanResult);
                    wifiResultlist.add(scanResult);

                }
            }
            list_scan.clear();
            list_scan=wifiResultlist;
            hashSetWifiItems=vSet;
        }
    }

    public int getSecurity(ScanResult result) {
        if (result.capabilities.contains("WEP")) {
            return 1;
        } else if (result.capabilities.contains("PSK")) {
            return 2;
        } else if (result.capabilities.contains("EAP")) {
            return 3;
        }
        return 0;
    }

    class ConnectWifiThread extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            int index = Integer.parseInt(params[0]);
            if (index > list_scan.size()) {
                return null;
            }
            WifiConfiguration config = CreateWifiInfo(list_scan.get(index).SSID,
                    params[1], getSecurity(list_scan.get(index)));

            int netId = wifiManager.addNetwork(config);
            if (null != config) {
                wifiManager.enableNetwork(netId, true);
                return list_scan.get(index).SSID;
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            if (list_scan.get(current_wifi_index).SSID.equals(result)) {
                handler.sendEmptyMessage(0);
            } else {
                handler.sendEmptyMessage(1);
            }
            super.onPostExecute(result);
        }

        public WifiConfiguration CreateWifiInfo(String SSID, String Password,
                                                int Type) {
            if(D) Log.d(TAG, "SSID = " + SSID + "password " + Password + "type ="
                    + Type);
            WifiConfiguration config = new WifiConfiguration();
            config.allowedAuthAlgorithms.clear();
            config.allowedGroupCiphers.clear();
            config.allowedKeyManagement.clear();
            config.allowedPairwiseCiphers.clear();
            config.allowedProtocols.clear();
            config.SSID = "\"" + SSID + "\"";
            if (Type == 0) {
                config.wepKeys[0] = "\"" + "\"";
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                config.wepTxKeyIndex = 0;
            } else if (Type == 1) {
                config.preSharedKey = "\"" + Password + "\"";
                config.hiddenSSID = true;
                config.allowedAuthAlgorithms
                        .set(WifiConfiguration.AuthAlgorithm.SHARED);
                config.allowedGroupCiphers
                        .set(WifiConfiguration.GroupCipher.CCMP);
                config.allowedGroupCiphers
                        .set(WifiConfiguration.GroupCipher.TKIP);
                config.allowedGroupCiphers
                        .set(WifiConfiguration.GroupCipher.WEP40);
                config.allowedGroupCiphers
                        .set(WifiConfiguration.GroupCipher.WEP104);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                config.wepTxKeyIndex = 0;
            } else if (Type == 2) {
                if(D) Log.d(TAG, "into type wpa");
                config.preSharedKey = "\"" + Password + "\"";
                config.hiddenSSID = true;
                config.allowedAuthAlgorithms
                        .set(WifiConfiguration.AuthAlgorithm.OPEN);
                config.allowedGroupCiphers
                        .set(WifiConfiguration.GroupCipher.TKIP);
                config.allowedKeyManagement
                        .set(WifiConfiguration.KeyMgmt.WPA_PSK);
                config.allowedPairwiseCiphers
                        .set(WifiConfiguration.PairwiseCipher.TKIP);
                // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                config.allowedGroupCiphers
                        .set(WifiConfiguration.GroupCipher.CCMP);
                config.allowedPairwiseCiphers
                        .set(WifiConfiguration.PairwiseCipher.CCMP);
                config.status = WifiConfiguration.Status.ENABLED;
            } else {
                return null;
            }
            return config;
        }

    }

    public void connectionConfiguration(int index, String passwrod) {
        progressDialog = ProgressDialog.show(MainActivity.this, "正在连接...",
                "请稍候...",true);
        connectWifiThread = new ConnectWifiThread();
        connectWifiThread.execute(index + "", passwrod);
    }
    class RefreshSsidThread extends Thread {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            isConnectWifiThread = true;
            int i = 0;
            while (isConnectWifiThread) {
                if (wifiManager == null) {
                    wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                }
                currentWifiInfo = wifiManager.getConnectionInfo();
                if (("\"" + list_scan.get(current_wifi_index).SSID + "\"")
                        .equals(currentWifiInfo.getSSID())
                        && 0 != currentWifiInfo.getIpAddress()) {
                    if (null != progressDialog) {
                        progressDialog.dismiss();
                    }
                    handler.sendEmptyMessage(4);
                    isConnectWifiThread = false;
                } else if (6 == (i++)) {
                    if (null != progressDialog) {
                        progressDialog.dismiss();
                    }
                    isConnectWifiThread = false;
                    handler.sendEmptyMessage(1);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    isConnectWifiThread = false;
                    e.printStackTrace();
                }
            }
            super.run();
        }
    }
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }
}
