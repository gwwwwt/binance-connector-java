package org.bc.fenfencai;

import com.binance.connector.client.impl.spot.Market;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.concurrent.*;

public class DataReceiver {
    private final static long MS_OF_MINUTE = 60 * 1000;
    private final static long OFFSET_OF_PREV_MINUTE = 5 * 1000;

    private static final Logger logger = LoggerFactory.getLogger(DataReceiver.class);

    // private static final CyclicBarrier BARRIER = new CyclicBarrier(3, () -> {
    //     logger.info("三个线程都跑完了，主线程继续...");
    // });

    public static void main(String[] args) throws InterruptedException, BrokenBarrierException, ExecutionException {

        ExecutorService threadPool = Executors.newFixedThreadPool(2);

        Market market_for_thread1 = MarketFactory.newInstance();
        // Market market_for_thread2 = MarketFactory.newInstance();
        Market market_for_thread3 = MarketFactory.newInstance();

        long begin_of_next_minute = get_begin_of_next_minute();

        long delta = 0L;

        while (true) {
            LocalDateTime today = LocalDateTime.now();
            today = today.plusMinutes(1);

            String currentLoop = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + String.valueOf(today.getHour() * 60 + today.getMinute());

            Future<Result> estimateFuture2 = threadPool.submit(getTask(market_for_thread3, begin_of_next_minute - MS_OF_MINUTE,
                begin_of_next_minute - OFFSET_OF_PREV_MINUTE, "55秒", false));

            Future<Result> accurateFuture = threadPool.submit(getTask(market_for_thread1, begin_of_next_minute - MS_OF_MINUTE,
                begin_of_next_minute, "准确值", true));

            // Future<Result> estimateFuture1 = threadPool.submit(getTask(market_for_thread2, begin_of_next_minute - MS_OF_MINUTE,
            //     begin_of_next_minute - 1000, "近似值", false));
            // BARRIER.await();
            // BARRIER.reset();

            System.out.println("------ 当前第" + currentLoop + "期 ------");
            Result result2 = estimateFuture2.get();
            System.out.println(result2.getDesc() + " - " + result2.getFull_value() + " - " + result2.getValue());
            // System.out.println("预估值: " + result2.getValue()*3/2);
            // Result result1 = estimateFuture1.get();
            // System.out.println(result1.getDesc() + " - " + result1.getFull_value() + " - " + result1.getValue());

            if (delta == 0L) {
                System.out.println("---------- 初始化中 ------");
            } else {
                System.out.println("预估值: " + (result2.getValue() + delta));
            }

            Result result0 = accurateFuture.get();
            System.out.println(result0.getDesc() + " - " + result0.getFull_value() + " - " + result0.getValue());

            delta = result0.getValue() - result2.getValue();
            begin_of_next_minute = get_begin_of_next_minute();
        }
    }

    private static long get_begin_of_next_minute() {
        return (System.currentTimeMillis() / MS_OF_MINUTE * MS_OF_MINUTE) + MS_OF_MINUTE;
    }

    private static Callable<Result> getTask(Market market, long start_time, long end_time, String message, boolean isAccurate) {
        return new Callable<Result>() {
            @Override
            public Result call() throws Exception {
                try {
                    TimeUnit.MILLISECONDS.sleep(end_time + 1000 - System.currentTimeMillis());

                    HashMap<String, Object> parameters = getParameters(start_time, end_time, BtcUsdtParamers.INTERVAL);

                    String dataStr = market.klines(parameters).split(",")[7].trim();
                    dataStr = dataStr.substring(1, dataStr.length() - 1);

                    int index = dataStr.indexOf(".");
                    long value = getValue(dataStr, index);
                    // BARRIER.await();

                    return new Result(message, dataStr, value);
                } catch (Exception e) {
                    logger.error("Interrupt exception ... Ignore it ......");
                    System.exit(-1);
                }
                return null;
            }
        };
    }

    private static long getValue(String dataStr, int index) {
        long value = 0;
        for (int begin = 0; begin < index + 3; begin++) {
            if (begin == index)
                continue;

            value = value * 10 + Integer.parseInt(dataStr.charAt(begin) + "");
        }
        return value;
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

    private static class Result {
        private final String desc;

        public String getFull_value() {
            return full_value;
        }

        private final String full_value;
        private final long value;

        public Result(String desc, String full_value, long value) {
            this.desc = desc;
            this.full_value = full_value;
            this.value = value;
        }

        public String getDesc() {
            return desc;
        }

        public long getValue() {
            return value;
        }
    }
}
