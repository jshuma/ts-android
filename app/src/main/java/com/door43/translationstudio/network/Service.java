package com.door43.translationstudio.network;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the base class for the client and server classes
 */
public abstract class Service {
    protected final Context mContext;
    protected Map<String, Peer> mPeers = new HashMap<String, Peer>();

    public Service(Context context) {
        mContext = context;
    }

    /**
     * Returns the broadcast ip address
     * @return
     * @throws IOException
     */
    public InetAddress getBroadcastAddress() {
        WifiManager wifi = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        // handle null somehow

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        try {
            return InetAddress.getByAddress(quads);
        } catch (UnknownHostException e) {
            return null;
        }
    }

    /**
     * Returns the ip address of the device
     * @return
     */
    public static String getIpAddress() {
        try {
            for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = (NetworkInterface)en.nextElement();
                for (Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = (InetAddress)enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()&&inetAddress instanceof Inet4Address) {
                        String ipAddress=inetAddress.getHostAddress().toString();
                        return ipAddress;
                    }
                }
            }
        } catch (SocketException ex) {
        }
        return null;
    }

    /**
     * Returns a list of ip addresses on the local network
     * @param subnet the subnet address. This can be any ip address and the subnet will automatically be retreived.
     * @return
     */
    public static List<String> checkHosts(String subnet){

        List<String> hosts = new ArrayList<String>();
        int timeout=1000;

        // trim down to just the subnet
        String[] pieces = subnet.trim().split("\\.");
        if(pieces.length == 4) {
            subnet = pieces[0]+"."+pieces[1]+"."+pieces[2];
        } else if(pieces.length != 3) {
            return hosts;
        }

        for (int i=1;i<254;i++){
            String host=subnet + "." + i;
            try {
                if (InetAddress.getByName(host).isReachable(timeout)){
                    hosts.add(host);
                    System.out.println(host + " is reachable");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return hosts;
    }

    /**
     * Adds a network peer to the list of known peers for this service
     * @param p The peer to be added to the list of peers
     * @return returns true if the peer is new and was added
     */
    protected boolean addPeer(Peer p) {
        if(!mPeers.containsKey(p.getIpAddress())) {
            mPeers.put(p.getIpAddress(), p);
            return true;
        } else {
            mPeers.get(p.getIpAddress()).touch();
            return false;
        }
    }

    /**
     * Returns a list of peers that are connected to this service
     * @return
     */
    public ArrayList<Peer> getPeers() {
        return new ArrayList<Peer>(mPeers.values());
    }

    public abstract void start();
    public abstract void stop();
}