package mup.nolan.mupplugin.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetUtils
{
    public static InetAddress dnsLookup(String host)
    {
        try
        {
            return InetAddress.getByName(host);
        } catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
