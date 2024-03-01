package org.bc.fenfencai;

import com.binance.connector.client.impl.spot.Market;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.concurrent.*;

public class DataReceiver {
    private final static long MS_OF_MINUTE = 60 * 1000;

    private static final CyclicBarrier BARRIER = new CyclicBarrier(3, () -> {
        System.out.println("两个线程都跑完了，主线程继续...");
    });

    public static void main(String[] args) throws InterruptedException, BrokenBarrierException {

        ExecutorService threadPool = Executors.newFixedThreadPool(2);

        Market market_for_thread1 = MarketFactory.newInstance();
        Market market_for_thread2 = MarketFactory.newInstance();

        long begin_of_next_minute = get_begin_of_next_minute();

        while (true) {
            threadPool.execute(getTask(market_for_thread1, begin_of_next_minute - MS_OF_MINUTE, begin_of_next_minute,
                "准确值", true));

            threadPool.execute(getTask(market_for_thread2, begin_of_next_minute - MS_OF_MINUTE,
                begin_of_next_minute - 1500, "近似值", false ));

            BARRIER.await();
            BARRIER.reset();
            begin_of_next_minute = get_begin_of_next_minute();
        }
    }

    private static long get_begin_of_next_minute() {
        return (System.currentTimeMillis() / MS_OF_MINUTE * MS_OF_MINUTE) + MS_OF_MINUTE;
    }

    private static Runnable getTask(Market market, long start_time, long end_time, String message, boolean isAccurate) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    TimeUnit.MILLISECONDS.sleep(end_time + 1000 - System.currentTimeMillis());

                    LocalDateTime today = LocalDateTime.now();
                    if (!isAccurate) {
                        today = today.plusMinutes(1);
                    }
                    String currentLoop = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                        + String.valueOf(today.getHour() * 60 + today.getMinute());

                    HashMap<String, Object> parameters = getParameters(start_time, end_time, BtcUsdtParamers.INTERVAL);
                    // System.out.println(parameters);

                    String response = market.klines(parameters);
                    System.out.printf("%35s", "第" + currentLoop + "期" + message + " --- ");

                    String result = response.split(",")[7].trim();
                    System.out.println(result.substring(1, result.length() - 1));

                    BARRIER.await();
                } catch (Exception e) {
                    System.out.println("Interrupt exception ... Ignore it ......");
                }
            }
        };
    }

    @NotNull
    private static HashMap<String, Object> getParameters(long start, long end, String interval) {
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("symbol", BtcUsdtParamers.SYMBOL);
        parameters.put("interval", interval);
        parameters.put("limit", BtcUsdtParamers.LIMIT);

        parameters.put("startTime", start);
        parameters.put("endTime", end);
        return parameters;
    }
}
