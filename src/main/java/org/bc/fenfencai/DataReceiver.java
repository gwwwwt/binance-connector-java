package org.bc.fenfencai;

import com.binance.connector.client.impl.spot.Market;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class DataReceiver {
    public static void main(String[] args) throws InterruptedException {
        Market market = MarketFactory.newInstance();

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("symbol", BtcUsdtParamers.SYMBOL);
        parameters.put("interval", BtcUsdtParamers.INTERVAL);
        parameters.put("limit", BtcUsdtParamers.LIMIT);

        String response;
        long startOfCurrentMinute;
        long startOfLastMinute;

        TimeUnit.MILLISECONDS.sleep((System.currentTimeMillis() / (60 * 1000) * (60 * 1000) + (60 * 1000)) - System.currentTimeMillis());

        while (true) {
            String currentLoop = LocalDateTime.now().minusMinutes(1).format(DateTimeFormatter.ofPattern("yyyyMMddmm"));

            System.out.println("当前期数:" + currentLoop + "------------");

            startOfCurrentMinute = System.currentTimeMillis() / (60 * 1000) * (60 * 1000);
            startOfLastMinute = startOfCurrentMinute - (60 * 1000);

            parameters.put("startTime", startOfLastMinute);
            parameters.put("endTime", startOfCurrentMinute);

            response = market.klines(parameters);
            System.out.println(response.split(",")[7]);

            TimeUnit.MILLISECONDS.sleep((System.currentTimeMillis() / (60 * 1000) * (60 * 1000) + (60 * 1000)) - System.currentTimeMillis());
        }

    }
}
