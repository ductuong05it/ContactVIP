package com.example.contactvip.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.contactvip.R;

import java.util.Random;

public class AvatarUtils {

    public static void loadAvatar(Context context, String uri, String name, ImageView imageView) {
        if (uri != null && !uri.isEmpty()) {
            Glide.with(context)
                    .load(uri)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(imageView);
        } else {
            // Generate letter avatar
            String letter = getInitial(name);
            // In a real app, we might use a library or custom drawable to render the letter on a colored background.
            // For now, we'll just use the default icon.
            imageView.setImageResource(R.drawable.ic_person);
        }
    }

    private static String getInitial(String name) {
        if (name == null || name.isEmpty()) return "?";
        return name.substring(0, 1).toUpperCase();
    }
}
