package network;

import java.util.Timer;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuctionTimer {
    private static final Logger logger = LoggerFactory.getLogger(AuctionTimer.class);
    private Timer timer;
    private int secondsRemaining;
    private String itemName;

    public AuctionTimer(String itemName, int durationSeconds) {
        this.itemName = itemName;
        this.secondsRemaining = durationSeconds;
        this.timer = new Timer();
    }

    public void start() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (secondsRemaining > 0) {
                    logger.info("⏳ [{}] Còn lại: {}s", itemName, secondsRemaining);
                    secondsRemaining--;
                    
                    // Gửi cập nhật thời gian cho tất cả Client
                    AuctionServer.broadcast("{\"action\":\"TIME_TICK\", \"item\":\"" + itemName + "\", \"time\":" + secondsRemaining + "}");
                } else {
                    stop();
                    logger.info("🏁 [{}] ĐÃ KẾT THÚC!", itemName);
                    AuctionServer.broadcast("{\"action\":\"END_AUCTION\", \"item\":\"" + itemName + "\", \"msg\":\"Phiên đấu giá kết thúc!\"}");
                }
            }
        }, 0, 1000); // Lặp lại mỗi 1 giây (1000ms)
    }

    public void stop() {
        timer.cancel();
    }
}