package com.vsv.viewutils;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public final class StopVerticalScrollAnimator {

    private StopVerticalScrollAnimator() {

    }

    public static void setRecycleViewAnimation(@NonNull RecyclerView recyclerView, int minStrengthOfHit, int topAnimationId, int bottomAnimationId) {
        if (minStrengthOfHit < 0) {
            minStrengthOfHit *= -1;
        }
        int finalStrengthOfHit = minStrengthOfHit;
        boolean finalAnimateOnTop = false;
        boolean finalAnimateOnBottom = false;
        Animation topAnimation = null;
        Animation bottomAnimation = null;
        if (topAnimationId > 0) {
            topAnimation = AnimationUtils.loadAnimation(recyclerView.getContext(), topAnimationId);
        }
        if (bottomAnimationId > 0) {
            bottomAnimation = AnimationUtils.loadAnimation(recyclerView.getContext(), bottomAnimationId);
        }
        if (topAnimation == null && bottomAnimation == null) {
            return;
        }
        Animation finalTopAnimation = topAnimation;
        Animation finalBottomAnimation = bottomAnimation;
        recyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {

            final int strengthOfHit = finalStrengthOfHit;

            final Animation topAnimation = finalTopAnimation;

            final Animation bottomAnimation = finalBottomAnimation;

            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (finalTopAnimation != null && oldScrollY > strengthOfHit && !recyclerView.canScrollVertically(-1)) {
                    recyclerView.startAnimation(finalTopAnimation);
                }else if (finalBottomAnimation != null && oldScrollY < -strengthOfHit && !recyclerView.canScrollVertically(1)) {
                    recyclerView.startAnimation(finalBottomAnimation);
                }
            }
        });
    }
}
