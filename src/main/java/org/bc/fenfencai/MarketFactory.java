package org.bc.fenfencai;

import com.binance.connector.client.SpotClient;
import com.binance.connector.client.impl.SpotClientImpl;
import com.binance.connector.client.impl.spot.Market;
import com.binance.connector.client.utils.ProxyAuth;
import org.bc.Config;

import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * 因为想要2个线程跑任务，所以取消使用单例
 *
 */
public class MarketFactory {
    //
    // private static Market INSTANCE;
    public static Market newInstance() {
        // if (INSTANCE != null) {
        //     return INSTANCE;
        // }

        SpotClient client = new SpotClientImpl(Config.API_KEY, Config.SECRET_KEY, Config.URL);
//        SpotClient client = new SpotClientImpl(Config.API_KEY, Config.SECRET_KEY, Config.URL);
        Proxy proxyConn = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 7890));
        ProxyAuth proxy = new ProxyAuth(proxyConn, null);
        client.setProxy(proxy);
        // INSTANCE = client.createMarket();
        // return INSTANCE;
        return client.createMarket();
    }
}
