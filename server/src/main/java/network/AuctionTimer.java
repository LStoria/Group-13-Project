package network;

import java.util.Timer;
import java.util.TimerTask;

public class AuctionTimer {
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
                    System.out.println("⏳ [" + itemName + "] Còn lại: " + secondsRemaining + "s");
                    secondsRemaining--;
                    
                    // Gửi cập nhật thời gian cho tất cả Client
                    AuctionServer.broadcast("{\"action\":\"TIME_TICK\", \"item\":\"" + itemName + "\", \"time\":" + secondsRemaining + "}");
                } else {
                    stop();
                    System.out.println("🏁 [" + itemName + "] ĐÃ KẾT THÚC!");
                    AuctionServer.broadcast("{\"action\":\"END_AUCTION\", \"item\":\"" + itemName + "\", \"msg\":\"Phiên đấu giá kết thúc!\"}");
                }
            }
        }, 0, 1000); // Lặp lại mỗi 1 giây (1000ms)
    }

    public void stop() {
        timer.cancel();
    }
}