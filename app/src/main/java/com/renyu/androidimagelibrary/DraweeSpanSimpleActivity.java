package com.renyu.androidimagelibrary;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.interfaces.DraweeHierarchy;
import com.facebook.drawee.span.DraweeSpan;
import com.facebook.drawee.span.DraweeSpanStringBuilder;
import com.facebook.drawee.span.SimpleDraweeSpanTextView;
import com.renyu.commonlibrary.baseact.BaseActivity;

public class DraweeSpanSimpleActivity extends BaseActivity {

    private SimpleDraweeSpanTextView mDraweeSpanTextView;
    private Uri mInlineImageUri;
    private Uri mInlineAnimatedImageUri;

    @Override
    public void initParams() {
        mInlineImageUri =
                Uri.parse("http://7b1g8u.com1.z0.glb.clouddn.com/mappoint.png");
        mInlineAnimatedImageUri =
                Uri.parse("http://7b1g8u.com1.z0.glb.clouddn.com/fengche.gif");
        mDraweeSpanTextView = findViewById(R.id.drawee_text_view);

        String text = "Text with an # inline image using a DraweeSpan. Also works with % animated images!";
        int imagePosition = text.indexOf('#');
        DraweeSpanStringBuilder draweeSpanStringBuilder = new DraweeSpanStringBuilder(text);
        DraweeHierarchy draweeHierarchy = GenericDraweeHierarchyBuilder.newInstance(getResources())
                .setPlaceholderImage(new ColorDrawable(Color.RED))
                .setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP)
                .build();
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(mInlineImageUri)
                .build();
        draweeSpanStringBuilder.setImageSpan(
                this, /* Context */
                draweeHierarchy, /* hierarchy to be used */
                controller, /* controller to be used to update the hierarchy */
                imagePosition, /* image index within the text */
                200, /* image width */
                200, /* image height */
                false, /* auto resize */
                DraweeSpan.ALIGN_CENTER); /* alignment */
        int imagePosition2 = text.indexOf('%');

        DraweeHierarchy draweeAnimatedHierarchy =
                GenericDraweeHierarchyBuilder.newInstance(getResources())
                        .setPlaceholderImage(new ColorDrawable(Color.RED))
                        .setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP)
                        .build();
        DraweeController animatedController =
                Fresco.newDraweeControllerBuilder()
                        .setUri(mInlineAnimatedImageUri)
                        .setAutoPlayAnimations(true)
                        .build();
        draweeSpanStringBuilder.setImageSpan(
                this, /* Context */
                draweeAnimatedHierarchy, /* hierarchy to be used */
                animatedController, /* controller to be used to update the hierarchy */
                imagePosition2, /* image index within the text */
                200, /* image width */
                200, /* image height */
                false, /* auto resize */
                DraweeSpan.ALIGN_CENTER); /* alignment */

        mDraweeSpanTextView.setDraweeSpanStringBuilder(draweeSpanStringBuilder);
    }

    @Override
    public int initViews() {
        return R.layout.activity_draweespan_simple;
    }

    @Override
    public void loadData() {

    }

    @Override
    public int setStatusBarColor() {
        return Color.BLACK;
    }

    @Override
    public int setStatusBarTranslucent() {
        return 0;
    }
}
