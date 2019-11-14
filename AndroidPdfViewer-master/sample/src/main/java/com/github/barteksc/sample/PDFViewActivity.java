/**
 * Copyright 2016 Bartosz Schiller
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.barteksc.sample;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnDrawListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnLongPressListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.listener.OnPageScrollListener;
import com.github.barteksc.pdfviewer.listener.OnRenderListener;
import com.github.barteksc.pdfviewer.listener.OnTapListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.github.barteksc.pdfviewer.util.Util;
import com.orhanobut.logger.Logger;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.util.SizeF;
import com.yuyh.library.BubblePopupWindow;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.NonConfigurationInstance;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.options)
public class PDFViewActivity extends AppCompatActivity implements OnPageChangeListener, OnLoadCompleteListener,
        OnPageErrorListener {

    private static final String TAG = PDFViewActivity.class.getSimpleName();

    private final static int REQUEST_CODE = 42;
    public static final int PERMISSION_CODE = 42042;

    public static final String SAMPLE_FILE = "sample_wrapper_wrapper.pdf";
    public static final String READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";

    @ViewById
    PDFView pdfView;

    @NonConfigurationInstance
    Uri uri;

    @NonConfigurationInstance
    Integer pageNumber = 0;

    String pdfFileName;
    private Canvas canvas;
    private int count;
    HashMap<Integer, List<PdfLabelPoint>> pointHashMap = new HashMap<>();
    private int clickInsertButtonOffsetWidth = 0;
    private int clickInsertButtonOffsetHeight = 0;
    private int labelPointWidth = 0;
    private BubblePopupWindow leftTopWindow;

    @OptionsItem(R.id.pickFile)
    void pickFile() {
//        int permissionCheck = ContextCompat.checkSelfPermission(this,
//                READ_EXTERNAL_STORAGE);
//
//        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(
//                    this,
//                    new String[]{READ_EXTERNAL_STORAGE},
//                    PERMISSION_CODE
//            );
//
//            return;
//        }
//
//        launchPicker();
//        pdfView.jumpTo(10);
//        pdfView.fitToWidth(0);
    }

    void launchPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        try {
            startActivityForResult(intent, REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            //alert user that file manager not working
            Toast.makeText(this, R.string.toast_pick_file_error, Toast.LENGTH_SHORT).show();
        }
    }

    @AfterViews
    void afterViews() {
        pdfView.setBackgroundColor(Color.LTGRAY);
        if (uri != null) {
            displayFromUri(uri);
        } else {
            displayFromAsset(SAMPLE_FILE);
        }
        setTitle(pdfFileName);
        findViewById(R.id.search_close_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leftTopWindow = new BubblePopupWindow(PDFViewActivity.this);
                View bubbleView = PDFViewActivity.this.getLayoutInflater().inflate(R.layout.layout_popup_view, null);
                TextView tvContent = (TextView) bubbleView.findViewById(R.id.tvContent);
                tvContent.setText("HelloWorlddassssssssssssssssssssssssssssssssssssssssssssssssssewfefewffffffffffwfewfwfewfwefewfewffeeeeeeeeeeeeeeeeeeeeeeeeeeefdswf");
                leftTopWindow.setBubbleView(bubbleView);
                leftTopWindow.show(view, Gravity.BOTTOM);
            }
        });
    }

    private void displayFromAsset(String assetFileName) {
        clickInsertButtonOffsetWidth = Util.getDP(PDFViewActivity.this, 100) / 2;
        clickInsertButtonOffsetHeight = Util.getDP(PDFViewActivity.this, 35);
        labelPointWidth = Util.getDP(PDFViewActivity.this, 20);
        pdfFileName = assetFileName;

        pdfView.fromAsset(SAMPLE_FILE)
                .defaultPage(pageNumber)
                .swipeHorizontal(true)
                .pageFitPolicy(FitPolicy.HEIGHT)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(10) // in dp
                .onPageError(this)
                .pageFitPolicy(FitPolicy.BOTH)
                .onDraw(new OnDrawListener() {
                    @Override
                    public void onLayerDrawn(Canvas canvas, float pageWidth, float pageHeight, int displayedPage) {


                    }
                })
                .onPageScroll(new OnPageScrollListener() {
                    @Override
                    public void onPageScrolled(int page, float positionOffset) {


                    }
                })
                .onRender(new OnRenderListener() {
                    @Override
                    public void onInitiallyRendered(int nbPages) {

                    }
                })
                .onDrawAll(new OnDrawListener() {
                    @Override
                    public void onLayerDrawn(Canvas canvas, float pageWidth, float pageHeight, int displayedPage) {
                        List<PdfLabelPoint> list = pointHashMap.get(displayedPage);
                        if (list != null) {
                            for (int i = 0; i < list.size(); i++) {
                                PdfLabelPoint poin = list.get(i);
                                drawLabelPoint(poin, canvas, pageWidth, pageHeight, displayedPage);
                            }
                        }
                        Logger.t("tangbin24").d("draw all" + displayedPage);
                    }
                })
                .onPageChange(new OnPageChangeListener() {
                    @Override
                    public void onPageChanged(int page, int pageCount) {
                        Logger.t("tangbin4").e("page" + page + "pageCount" + pageCount);
                    }
                }).onLongPress(new OnLongPressListener() {
            @Override
            public void onLongPress(final MotionEvent e) {
                Pair i = checkInLabelResf(e);
                if (i != null) {
                    //长按label事件处理，添加位移控件，删除原有canvas上的点
                    pointHashMap.get((int) i.first).remove((int) i.second);
                    final LabelPoint labelPoint = new LabelPoint(PDFViewActivity.this);
                    labelPoint.setCanDrag(true);
                    pdfView.getDragPinchManager().setEnabled(false);
                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(Util.getDP(PDFViewActivity.this, 20), Util.getDP(PDFViewActivity.this, 20));
                    lp.setMargins(0, 0, 0, 0);
                    pdfView.addView(labelPoint, lp);
                    labelPoint.setX(e.getX() - Util.getDP(PDFViewActivity.this, 17) / 2);
                    labelPoint.setY(e.getY() - Util.getDP(PDFViewActivity.this, 16) / 2);
//                    labelPoint.getParent().requestDisallowInterceptTouchEvent(false);
                    labelPoint.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {

                                insertPointToHashMap(labelPoint, labelPointWidth / 2, labelPointWidth / 2);

                            }
                            return false;
                        }
                    });
                    pdfView.invalidate();

                    pdfView.getDragPinchManager().setEnabled(false);
                    return;
                }

                //长按显示添加批注按钮
                final InsertPdfLabelView labelPoint = new InsertPdfLabelView(PDFViewActivity.this);
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(Util.getDP(PDFViewActivity.this, 100), Util.getDP(PDFViewActivity.this, 35));
                lp.setMargins(0, 0, 0, 0);
                pdfView.addView(labelPoint, lp);
                labelPoint.setX(e.getX() - Util.getDP(PDFViewActivity.this, 100) / 2);
                labelPoint.setY(e.getY() - Util.getDP(PDFViewActivity.this, 50));
                labelPoint.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (labelPoint.isDrag()) {
                            return;
                        }
                        //点击的页码
                        insertPointToHashMap(labelPoint, clickInsertButtonOffsetWidth, clickInsertButtonOffsetHeight);

                    }
                });
                pdfView.getDragPinchManager().setEnabled(false);
            }
        }).onTap(new OnTapListener() {
            @Override
            public boolean onTap(MotionEvent e) {
                Pair i = checkInLabelResf(e);
                final View view = new View(PDFViewActivity.this);
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(1, 1);
                lp.setMargins(0, 0, 0, 0);
                pdfView.addView(view, lp);
                view.setX(e.getX());
                view.setY(e.getY());
                Logger.t("tangbin25").e("tab");
                leftTopWindow = new BubblePopupWindow(PDFViewActivity.this);
                View bubbleView = PDFViewActivity.this.getLayoutInflater().inflate(R.layout.layout_popup_view, null);
                TextView tvContent = (TextView) bubbleView.findViewById(R.id.tvContent);
                tvContent.setText("HelloWorld");
                leftTopWindow.setBubbleView(bubbleView);
                leftTopWindow.show(view, Gravity.TOP);
                return false;
            }
        })
                .load();

    }

    private boolean insertPointToHashMap(View labelPoint, int labelPointWidth, int labelPointHeight) {
        int currentPage = pdfView.findTopFocusPage(pdfView.getCurrentXOffset() - labelPoint.getX() - labelPointWidth, pdfView.getCurrentYOffset() - labelPoint.getY() - labelPointHeight);//当前触摸点加上点击控件的一些偏移量在哪个页码
        float offset = currentPage == 0 ? 0 : -pdfView.pdfFile.getPageOffset(currentPage, pdfView.getZoom());//当前页置顶时偏移量
        SizeF sizeF = pdfView.getPageSize(currentPage);
        PdfLabelPoint pdfLabelPoint = null;
        if (pdfView.isSwipeVertical()) {
            float offsetY = pdfView.getCurrentYOffset() - offset;
            //全部先转化为zoom为1时的大小
            float screenOffsetY = (labelPoint.getY() + labelPointHeight - offsetY) / pdfView.getZoom();//当前页pdf的y轴上的偏移
            float screenOffsetX = (labelPoint.getX() + labelPointWidth - pdfView.getCurrentXOffset()) / pdfView.getZoom();//当前页pdf的x轴上的偏移
            float leftWhiteStrip = (pdfView.getWidth() - sizeF.getWidth()) / 2 - pdfView.getFirstXoffset();
            float screenOffsetPDFX = screenOffsetX - leftWhiteStrip;//当前页pdf的x轴上的偏移
            if (screenOffsetPDFX < 0 || screenOffsetPDFX > sizeF.getWidth() || screenOffsetY < 0 || screenOffsetY > sizeF.getHeight()) {
                Toast.makeText(PDFViewActivity.this, "点击范围外了", 0).show();
                pdfView.removeView(labelPoint);
                pdfView.getDragPinchManager().setEnabled(true);
                return false;
            } else {
                pdfLabelPoint = new PdfLabelPoint(currentPage, screenOffsetPDFX / sizeF.getWidth(), screenOffsetY / sizeF.getHeight(), screenOffsetX, screenOffsetY);
            }
        } else {
            float offsetX = pdfView.getCurrentXOffset() - offset;
            float screenOffsetY = (labelPoint.getY() + labelPointHeight - pdfView.getCurrentYOffset()) / pdfView.getZoom();//当前页pdf的y轴上的偏移
            float screenOffsetX = (labelPoint.getX() + labelPointWidth - offsetX) / pdfView.getZoom();//当前页pdf的x轴上的偏移
            float topWhiteStrip = (pdfView.getHeight() - sizeF.getHeight()) / 2 - pdfView.getFirstYoffsett();
            float screenOffsetPDFY = screenOffsetY - topWhiteStrip;//当前页pdf的x轴上的偏移
            if (screenOffsetPDFY < 0 || screenOffsetPDFY > sizeF.getHeight() || screenOffsetX < 0 || screenOffsetX > sizeF.getWidth()) {
                Toast.makeText(PDFViewActivity.this, "点击范围外了", 0).show();
                pdfView.removeView(labelPoint);
                pdfView.getDragPinchManager().setEnabled(true);
                return false;
            } else {
                pdfLabelPoint = new PdfLabelPoint(currentPage, screenOffsetX / sizeF.getWidth(), screenOffsetPDFY / sizeF.getHeight(), screenOffsetX, screenOffsetY);
            }
        }


        Logger.t("tangbin13").e("pdfLabelPoint" + pdfLabelPoint.toString());
        addLabelPointToHashmap(pdfLabelPoint);
        pdfView.removeView(labelPoint);
        pdfView.invalidate();
        pdfView.getDragPinchManager().setEnabled(true);
        return true;
    }

    private Pair<Integer, Integer> checkInLabelResf(MotionEvent e) {
        int m = 0;
        if ((m = checkPage(e, pdfView.getCurrentPage())) == -1) {
            if ((m = checkPage(e, pdfView.getCurrentPage() - 1)) == -1) {

                if ((m = checkPage(e, pdfView.getCurrentPage() + 1)) == -1) {
                    return null;
                } else {
                    return new Pair<>(pdfView.getCurrentPage() + 1, m);
                }

            } else {
                return new Pair<>(pdfView.getCurrentPage() - 1, m);
            }
        } else {
            return new Pair<>(pdfView.getCurrentPage(), m);
        }

    }

    private int checkPage(MotionEvent e, int currentPage) {
        List<PdfLabelPoint> pdfLabelPoints = pointHashMap.get(currentPage);
        if (pdfLabelPoints == null || pdfLabelPoints.size() == 0) {
            return -1;
        }
        float offset = currentPage == 0 ? 0 : -pdfView.pdfFile.getPageOffset(currentPage, pdfView.getZoom());//当前页置顶时偏移量
        SizeF sizeF = pdfView.getPageSize(currentPage);
        float leftWhiteStrip = (pdfView.getWidth() - sizeF.getWidth()) * pdfView.getZoom() / 2;
        float offsetY = pdfView.getCurrentYOffset() - offset;
        for (int i = 0; i < pdfLabelPoints.size(); i++) {
            float xOffset = pdfLabelPoints.get(i).getWidth() * sizeF.getWidth() * pdfView.getZoom() + leftWhiteStrip;
            float yOffset = pdfLabelPoints.get(i).getHeight() * sizeF.getHeight() * pdfView.getZoom() - offset;
            Logger.t("tangbin21").e("x" + xOffset + "y" + yOffset);
            if (Math.abs(e.getX() - pdfView.getCurrentXOffset() - xOffset) < 60 && Math.abs(e.getY() - pdfView.getCurrentYOffset() - yOffset) < 60) {
                Logger.t("tangbin21").e("范围类");

                return i;
            }
            Logger.t("tangbin21").e("Math.abs(e.getX() + xOffset):" + Math.abs(e.getX() + xOffset) + " Math.abs(e.getY() + yOffset)：" + Math.abs(e.getY() + yOffset));
        }
        return -1;


    }

    private void drawLabelPoint(PdfLabelPoint poin, Canvas canvas, float pageWidth, float pageHeight, int displayedPage) {
        float screenXOffset;
        float screenYOffset;
        float leftWhiteStrip;
        float topWhiteStrip = 0;
        if (pdfView.isSwipeVertical()) {
            leftWhiteStrip = (pdfView.getWidth() * pdfView.getZoom() - pageWidth) / 2 + pdfView.getFirstXoffset();
            screenXOffset = poin.getWidth() * pageWidth + leftWhiteStrip;
            screenYOffset = poin.getHeight() * pageHeight;
        } else {
            topWhiteStrip = (pdfView.getHeight() - pdfView.pdfFile.getPageSize(displayedPage).getHeight()) / 2;
            screenXOffset = poin.getWidth() * pageWidth;
            screenYOffset = poin.getHeight() * pageHeight + topWhiteStrip;
        }

        Logger.t("tangbin30").e("canvans.getheight:" + canvas.getHeight());
        Logger.t("tangbin30").e("pdfView.getHeight() * pdfView.getZoom():" + pdfView.getHeight() * pdfView.getZoom());
        Logger.t("tangbin30").e("pdfView.getFirstYoffsett()" + pdfView.getFirstYoffsett());

        Bitmap mBitmap = BitmapFactory.decodeResource(PDFViewActivity.this.getResources(), R.drawable.pdf_label_point_icon);
        // 指定图片绘制区域(左上角的四分之一)
        Rect src = new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
        // 指定图片在屏幕上显示的区域(原图大小)
//        Rect dst = new Rect((int) screenXOffset - mBitmap.getWidth() / 2, (int) screenYOffset - mBitmap.getWidth() / 2, (int) screenXOffset + mBitmap.getWidth() / 2, (int) screenYOffset + mBitmap.getHeight() / 2);

//        Rect dst = new Rect(150, 0, 170, 20);
//        canvas.drawBitmap(mBitmap, src, dst, new Paint());
        Rect dst1 = new Rect(-(int) (pdfView.getCurrentXOffset() /pdfView.getZoom()), -(int) (pdfView.getCurrentYOffset()/pdfView.getZoom()), -(int) (pdfView.getCurrentXOffset() /pdfView.getZoom())+20, -(int) (pdfView.getCurrentYOffset() /pdfView.getZoom())+ 20);
        canvas.drawBitmap(mBitmap, src, dst1, new Paint());
//        Rect dst2 = new Rect(150, (int) (canvas.getHeight() - pdfView.getFirstYoffsett() - 20), 170, (int) (int) (canvas.getHeight() - pdfView.getFirstYoffsett()));
//        canvas.drawBitmap(mBitmap, src, dst2, new Paint());
//        for (int i = 0; i < 1600; i++) {
//            Rect dst2 = new Rect(0, i, 20, i + 20);
//            canvas.drawBitmap(mBitmap, src, dst2, new Paint());
//            i = i + 20;
//        }
//        for (int i = 0; i < 1600; i++) {
//            Rect dst2 = new Rect(i, 0, i + 20, 20);
//            canvas.drawBitmap(mBitmap, src, dst2, new Paint());
//            i = i + 20;
//        }
    }

    private void addLabelPointToHashmap(PdfLabelPoint pdfLabelPoint) {
        List<PdfLabelPoint> pagePointList = pointHashMap.get(pdfLabelPoint.getPag());
        if (pagePointList == null) {
            pagePointList = new ArrayList<>();
            pointHashMap.put(pdfLabelPoint.getPag(), pagePointList);
        }
        pagePointList.add(pdfLabelPoint);

    }

    private void displayFromUri(Uri uri) {
        pdfFileName = getFileName(uri);

        pdfView.fromUri(uri)
                .defaultPage(pageNumber)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(10) // in dp
                .onPageError(this)
                .load();
    }

    @OnActivityResult(REQUEST_CODE)
    public void onResult(int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            uri = intent.getData();
            displayFromUri(uri);
        }
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        setTitle(String.format("%s %s / %s", pdfFileName, page + 1, pageCount));
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    @Override
    public void loadComplete(int nbPages) {
        PdfDocument.Meta meta = pdfView.getDocumentMeta();
        Log.e(TAG, "title = " + meta.getTitle());
        Log.e(TAG, "author = " + meta.getAuthor());
        Log.e(TAG, "subject = " + meta.getSubject());
        Log.e(TAG, "keywords = " + meta.getKeywords());
        Log.e(TAG, "creator = " + meta.getCreator());
        Log.e(TAG, "producer = " + meta.getProducer());
        Log.e(TAG, "creationDate = " + meta.getCreationDate());
        Log.e(TAG, "modDate = " + meta.getModDate());

        printBookmarksTree(pdfView.getTableOfContents(), "-");

    }

    public void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
        for (PdfDocument.Bookmark b : tree) {

            Log.e(TAG, String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));


            if (b.hasChildren()) {
                printBookmarksTree(b.getChildren(), sep + "-");
            }
        }
    }

    /**
     * Listener for response to user permission request
     *
     * @param requestCode  Check that permission request code matches
     * @param permissions  Permissions that requested
     * @param grantResults Whether permissions granted
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchPicker();
            }
        }
    }

    @Override
    public void onPageError(int page, Throwable t) {
        Log.e(TAG, "Cannot load page " + page);
    }
}
