package org.bc.fenfencai;

import com.binance.connector.client.impl.spot.Market;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class DataReceiver {
    private final static long MS_OF_MINUTE = 60 * 1000;

    public static void main(String[] args) throws InterruptedException {
        Market market = MarketFactory.newInstance();

        long start = (System.currentTimeMillis() / MS_OF_MINUTE * MS_OF_MINUTE) + MS_OF_MINUTE;

        while (true) {
            TimeUnit.MILLISECONDS.sleep(start + 2000 - System.currentTimeMillis());

            LocalDateTime today = LocalDateTime.now();

            String currentLoop = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + String.valueOf(today.getHour() * 60 + today.getMinute());

            System.out.print("当前期数:" + currentLoop + "------------");

            HashMap<String, Object> parameters = getParameters(start);

            // System.out.println(parameters);

            String response = market.klines(parameters);

            System.out.println(response.split(",")[7]);

            start += (60 * 1000);
        }
    }

    @NotNull
    private static HashMap<String, Object> getParameters(long start) {
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("symbol", BtcUsdtParamers.SYMBOL);
        parameters.put("interval", BtcUsdtParamers.INTERVAL);
        parameters.put("limit", BtcUsdtParamers.LIMIT);

        parameters.put("startTime", start - MS_OF_MINUTE);
        parameters.put("endTime", start);
        return parameters;
    }
}
