package com.github.barteksc.sample;

public class PdfLabelPoint {
    private int pag;
    private float height;

    public float getxOffset() {
        return xOffset;
    }

    public void setxOffset(float xOffset) {
        this.xOffset = xOffset;
    }

    public float getyOffset() {
        return yOffset;
    }

    public void setyOffset(float yOffset) {
        this.yOffset = yOffset;
    }

    private float xOffset;//容器层当前页的偏移量

    public PdfLabelPoint(int pag, float width, float height, float xOffset, float yOffset) {
        this.pag = pag;
        this.height = height;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.width = width;
    }

    private float yOffset;

    public PdfLabelPoint(int pag, float width, float height) {
        this.pag = pag;
        this.height = height;
        this.width = width;
    }

    public int getPag() {
        return pag;
    }

    public void setPag(int pag) {
        this.pag = pag;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    private float width;

    @Override
    public String toString() {
        return "PdfLabelPoint{" +
                "pag=" + pag +
                ", height=" + height +
                ", width=" + width +
                '}';
    }
}
