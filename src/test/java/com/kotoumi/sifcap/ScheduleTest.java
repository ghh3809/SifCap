package com.kotoumi.sifcap;

import com.kotoumi.sifcap.sif.RankUpdater;
import org.junit.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduleTest {

    @Test
    public void testRankUpdate() throws InterruptedException {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleWithFixedDelay(new RankUpdater(), 0, 1, TimeUnit.SECONDS);
        Thread.sleep(1000000);
    }

}
