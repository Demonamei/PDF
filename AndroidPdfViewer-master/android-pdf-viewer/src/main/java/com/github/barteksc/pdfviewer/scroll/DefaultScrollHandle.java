package com.github.barteksc.pdfviewer.scroll;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.R;
import com.github.barteksc.pdfviewer.util.Util;
import com.orhanobut.logger.LogLevel;
import com.orhanobut.logger.Logger;

public class DefaultScrollHandle extends RelativeLayout implements ScrollHandle {

    private final static int HANDLE_LONG = 65;
    private final static int HANDLE_SHORT = 40;//进度条宽度
    private final static int DEFAULT_TEXT_SIZE = 16;

    private float relativeHandlerMiddle = 0f;//范围0到120

    protected TextView textView;
    protected Context context;
    private boolean inverted;
    private PDFView pdfView;
    private float currentPos;
    private Handler handler = new Handler();
    private Runnable hidePageScrollerRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    public LinearLayout getLinearLayout() {
        return linearLayout;
    }

    public void setLinearLayout(LinearLayout linearLayout) {
        this.linearLayout = linearLayout;
    }

    private LinearLayout linearLayout;//左上角按钮容器

    public DefaultScrollHandle(Context context) {
        this(context, false);
        Logger.init("tangbin").hideThreadInfo().logLevel(LogLevel.FULL);
    }

    public DefaultScrollHandle(Context context, boolean inverted) {
        super(context);
        this.context = context;
        this.inverted = inverted;
        textView = new TextView(context);
        setVisibility(INVISIBLE);
        setTextColor(Color.BLACK);
        setTextSize(DEFAULT_TEXT_SIZE);
    }

    @Override
    public void setupLayout(PDFView pdfView) {
        int align, width, height;
        Drawable background;
        // determine handler position, default is right (when scrolling vertically) or bottom (when scrolling horizontally)
        if (pdfView.isSwipeVertical()) {
            width = HANDLE_LONG;
            height = HANDLE_SHORT;
            if (inverted) { // left
                align = ALIGN_PARENT_LEFT;
                background = ContextCompat.getDrawable(context, R.drawable.default_scroll_handle_left);
            } else { // right
                align = ALIGN_PARENT_RIGHT;
                background = ContextCompat.getDrawable(context, R.drawable.default_scroll_handle_right);
            }
        } else {
            width = HANDLE_SHORT;
            height = HANDLE_LONG;
            if (inverted) { // top
                align = ALIGN_PARENT_TOP;
                background = ContextCompat.getDrawable(context, R.drawable.default_scroll_handle_top);
            } else { // bottom
                align = ALIGN_PARENT_BOTTOM;
                background = ContextCompat.getDrawable(context, R.drawable.default_scroll_handle_bottom);
            }
        }

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            setBackgroundDrawable(background);
        } else {
            setBackground(background);
        }

        LayoutParams lp = new LayoutParams(Util.getDP(context, width), Util.getDP(context, height));
        lp.setMargins(0, 0, 0, 0);

        LayoutParams tvlp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvlp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        addView(textView, tvlp);

        lp.addRule(align);
        pdfView.addView(this, lp);
//添加线性布局于左上角
        linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(0, Util.getDP(context, 40), 0, 0);
        LayoutParams lp1 = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pdfView.addView(linearLayout, lp1);
        //linearLayout添加目录按钮
        TextView textViewCatalogue = new TextView(context);
        textViewCatalogue.setBackgroundResource(R.drawable.pdf_left_button);
        textViewCatalogue.setText("目录");
        textViewCatalogue.setTextColor(Color.WHITE);
        textViewCatalogue.setGravity(Gravity.CENTER_VERTICAL);
        textViewCatalogue.setCompoundDrawablePadding(Util.getDP(context, 10));
        Drawable drawable = getResources().getDrawable(R.drawable.pdf_catalogue);
        drawable.setBounds(Util.getDP(context, 7), Util.getDP(context, 0), Util.getDP(context, 22), Util.getDP(context, 12));
        drawable.setBounds(Util.getDP(context, 7), Util.getDP(context, 0), Util.getDP(context, 22), Util.getDP(context, 12));
        textViewCatalogue.setCompoundDrawables(drawable, null, null, null);
        textViewCatalogue.setHeight(Util.getDP(context, 30));
        textViewCatalogue.setWidth(Util.getDP(context, 65));
        LinearLayout.LayoutParams llparam1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        llparam1.setMargins(0, Util.getDP(context, 10), 0, 0);
        linearLayout.addView(textViewCatalogue, llparam1);
        //添加批注按钮
        TextView textViewAnnotate = new TextView(context);
        textViewAnnotate.setBackgroundResource(R.drawable.pdf_left_button);
        textViewAnnotate.setText("批注");
        textViewAnnotate.setTextColor(Color.WHITE);
        textViewAnnotate.setGravity(Gravity.CENTER_VERTICAL);
        textViewAnnotate.setCompoundDrawablePadding(Util.getDP(context, 10));
        Drawable drawable1 = getResources().getDrawable(R.drawable.pdf_annotate);
        drawable1.setBounds(Util.getDP(context, 7), Util.getDP(context, 0), Util.getDP(context, 22), Util.getDP(context, 12));
        drawable1.setBounds(Util.getDP(context, 7), Util.getDP(context, 0), Util.getDP(context, 22), Util.getDP(context, 12));
        textViewAnnotate.setCompoundDrawables(drawable1, null, null, null);
        textViewAnnotate.setHeight(Util.getDP(context, 30));
        textViewAnnotate.setWidth(Util.getDP(context, 65));
        LinearLayout.LayoutParams llparam2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        llparam2.setMargins(0, Util.getDP(context, 10), 0, 0);
        linearLayout.addView(textViewAnnotate, llparam2);
        this.pdfView = pdfView;
        //添加bottom
        View view = inflate(context, R.layout.pdf_bottom_setting, null);
        LayoutParams tvlp1 = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvlp1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        pdfView.addView(view, tvlp1);
    }

    @Override
    public void destroyLayout() {
        pdfView.removeView(this);
    }

    @Override
    public void setScroll(float position) {
        if (!shown()) {
            show();
        } else {
            handler.removeCallbacks(hidePageScrollerRunnable);
        }
        if (pdfView != null) {
            setPosition((pdfView.isSwipeVertical() ? pdfView.getHeight() : pdfView.getWidth()) * position);
        }
    }

    private void setPosition(float pos) {
        if (Float.isInfinite(pos) || Float.isNaN(pos)) {
            return;
        }
        float pdfViewSize;
        if (pdfView.isSwipeVertical()) {
            pdfViewSize = pdfView.getHeight();
        } else {
            pdfViewSize = pdfView.getWidth();
        }
        pos -= relativeHandlerMiddle;

        if (pos < 0) {
            pos = 0;
        } else if (pos > pdfViewSize - Util.getDP(context, HANDLE_SHORT)) {
            int s = Util.getDP(context, HANDLE_SHORT);
            pos = pdfViewSize - s;
        }

        if (pdfView.isSwipeVertical()) {
            setY(pos);
        } else {
            setX(pos);
        }

        calculateMiddle();
        invalidate();
    }

    private void calculateMiddle() {
        float pos, viewSize, pdfViewSize;
        if (pdfView.isSwipeVertical()) {
            pos = getY();
            viewSize = getHeight();
            pdfViewSize = pdfView.getHeight();
        } else {
            pos = getX();
            viewSize = getWidth();
            pdfViewSize = pdfView.getWidth();
        }

        relativeHandlerMiddle = ((pos + relativeHandlerMiddle) / pdfViewSize) * viewSize;//范围0到120
        Logger.e("relativeHandlerMiddle" + relativeHandlerMiddle);
    }

    @Override
    public void hideDelayed() {
        handler.postDelayed(hidePageScrollerRunnable, 1000);
    }

    @Override
    public void setPageNum(int pageNum) {
        String text = String.valueOf(pageNum);
        if (!textView.getText().equals(text)) {
            textView.setText(text);
        }
    }

    @Override
    public boolean shown() {
        return getVisibility() == VISIBLE;
    }

    @Override
    public void show() {
        setVisibility(VISIBLE);

    }

    @Override
    public void hide() {
        setVisibility(INVISIBLE);
    }

    public void setTextColor(int color) {
        textView.setTextColor(color);
    }

    /**
     * @param size text size in dp
     */
    public void setTextSize(int size) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
    }

    private boolean isPDFViewReady() {
        return pdfView != null && pdfView.getPageCount() > 0 && !pdfView.documentFitsView();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (!isPDFViewReady()) {
            return super.onTouchEvent(event);
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                pdfView.stopFling();
                handler.removeCallbacks(hidePageScrollerRunnable);
                if (pdfView.isSwipeVertical()) {
                    currentPos = event.getRawY() - getY();

                } else {
                    currentPos = event.getRawX() - getX();
                }
                Logger.t("tangbin").e("event.getRawY()" + event.getRawY());
                Logger.t("tangbin").e("getY()" + getY());
                Logger.t("tangbin").e("currentPos" + currentPos);
            case MotionEvent.ACTION_MOVE:
                if (pdfView.isSwipeVertical()) {
                    setPosition(event.getRawY() - currentPos + relativeHandlerMiddle);//设置进度条坐标
                    pdfView.setPositionOffset(relativeHandlerMiddle / (float) getHeight(), false);
                    Logger.t("tangbin1").e("setPosition" + (event.getRawY() - currentPos + relativeHandlerMiddle));
                    Logger.t("tangbin2").e("setPositionOffset" + (relativeHandlerMiddle / (float) getHeight()));
                } else {
                    setPosition(event.getRawX() - currentPos + relativeHandlerMiddle);
                    pdfView.setPositionOffset(relativeHandlerMiddle / (float) getWidth(), false);
                    Logger.t("tangbin").e("setPosition" + (event.getRawX() - currentPos + relativeHandlerMiddle));
                }
                Logger.t("tangbin").e("event.getRawY()1" + event.getRawY());
                return true;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                hideDelayed();
                pdfView.performPageSnap();
                return true;
        }

        return super.onTouchEvent(event);
    }
}
