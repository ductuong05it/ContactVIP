package com.example.contactvip.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.example.contactvip.R;

public class AvatarUtils {

    public static void loadAvatar(Context context, String uri, String name, long updatedAt, ImageView imageView) {
        if (uri != null && !uri.isEmpty()) {
            Glide.with(context)
                    .load(uri)
                    .apply(RequestOptions.circleCropTransform())
                    // "Signature" giúp Glide biết ảnh đã thay đổi dựa vào timestamp, tránh lấy ảnh cũ từ cache
                    .signature(new ObjectKey(updatedAt))
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.ic_person);
        }
    }
}
