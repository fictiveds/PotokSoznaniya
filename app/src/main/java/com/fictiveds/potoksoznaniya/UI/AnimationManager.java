package com.fictiveds.potoksoznaniya.UI;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class AnimationManager {

    private final Context context;

    public AnimationManager(Context context) {
        this.context = context;
    }

    public void startFadeInAnimation(View view, int animationResourceId) {
        Animation fadeInAnimation = AnimationUtils.loadAnimation(context, animationResourceId);
        view.startAnimation(fadeInAnimation);
    }

}
