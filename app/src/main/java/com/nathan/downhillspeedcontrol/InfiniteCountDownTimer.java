package com.nathan.downhillspeedcontrol;

import android.os.CountDownTimer;

public abstract class InfiniteCountDownTimer{

    private long timeElapsed;
    private final long countDownTime;
    private CountDownTimer timer;

    public InfiniteCountDownTimer(long millisInFuture, long countDownInterval) {
        timeElapsed = 0;
        countDownTime = millisInFuture;

        timer = new CountDownTimer(millisInFuture, countDownInterval) {
            @Override
            public void onTick(long millisUntilFinished) {
                InfiniteCountDownTimer.this.onTick(millisUntilFinished - timeElapsed);
            }

            @Override
            public void onFinish() {
                InfiniteCountDownTimer.this.onTick(-timeElapsed);
                timeElapsed += countDownTime;
                timer.start();
            }
        };
    }

    public abstract void onTick(long millisUntilFinished);

    public void start()
    {
        timeElapsed = 0;
        timer.start();
    }

    public void cancel()
    {
        timer.cancel();
    }
}
