package com.main.android.wi_fi_radar;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.View;

import java.util.ArrayList;

public class RadarView extends View {

    RectF outerBox, innerBox;  // Границы внешнего и внутреннего колец
    private Point center;  // Центр радара
    private int radius;    // Максимальный радиус окружности радара
    private int textSize;    // Размер шрифта
    private int markerSize;  // Размер маркера
    private ArrayList<WIFI> WiFiAPs = null;

    private Paint circlePaint;
    private Paint axisPaint;
    private Paint textPaint;
    // Радар
    private RadialGradient mainGradient;
    private int[] mainGradientColors;
    private float[] mainGradientPositions;
    private Paint mainGradientPaint;

    // Стекло
    private RadialGradient glassShader;
    private int[] glassGradientColors;
    private float[] glassGradientPositions;
    private Paint glassPaint;

    public RadarView(Context context) {
        super(context);
        setupView();
    }

    public RadarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupView();
    }

    public RadarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setupView();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec,
                             int heightMeasureSpec) {
        int measuredWidth = measure(widthMeasureSpec);
        int measuredHeight = measure(heightMeasureSpec);
        int d = Math.min(measuredWidth, measuredHeight);
        setMeasuredDimension(d, d);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
// Определяем шейдеры
        int px = w / 2;
        int py = h / 2;
        center = new Point(px, py);

        radius = Math.min(px, py) - 20; // Отступ от краев канвы
        int ringWidth = radius / 20;    // Ширина кольца

        textSize = radius / 16;
        markerSize = ringWidth / 2;

        outerBox = new RectF(center.x - radius,
                center.y - radius,
                center.x + radius,
                center.y + radius);

        innerBox = new RectF(center.x - radius + ringWidth,
                center.y - radius + ringWidth,
                center.x + radius - ringWidth,
                center.y + radius - ringWidth);

        mainGradient = new RadialGradient(center.x, center.y, radius,
                mainGradientColors, mainGradientPositions, Shader.TileMode.CLAMP);

        glassShader = new RadialGradient(center.x, center.y, radius - ringWidth,
                glassGradientColors, glassGradientPositions, Shader.TileMode.CLAMP);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Радиальный градиентный шейдер радара
        mainGradientPaint.setShader(mainGradient);
        canvas.drawOval(outerBox, mainGradientPaint);

        // Сетка
        canvas.drawCircle(center.x, center.y, radius *0.10f, axisPaint);
        canvas.drawCircle(center.x, center.y, radius *0.40f, axisPaint);
        canvas.drawCircle(center.x, center.y, radius *0.70f, axisPaint);

        canvas.save();
        canvas.rotate(15, center.x, center.y);
        for (int i = 0; i < 12; i++) {
            canvas.drawLine(center.x, center.y, center.x + radius * 0.75f, center.y, axisPaint);
            canvas.rotate(30, center.x, center.y);
        }
        canvas.restore();

        // Данные
        drawData(canvas);

        // Шейдер стекла
        glassPaint.setShader(glassShader);
        canvas.drawOval(innerBox, glassPaint);

        // Внешняя окружность кольца
        circlePaint.setStrokeWidth(1);
        canvas.drawOval(outerBox, circlePaint);

        // Внутренняя окружность кольца
        circlePaint.setStrokeWidth(2);
        canvas.drawOval(innerBox, circlePaint);
    }

    private void drawData(Canvas canvas) {
        if (WiFiAPs == null) return;

        textPaint.setTextSize(textSize);

        float zoom = radius *0.75f / Main.maxSignalLevel;

        for (WIFI AP : WiFiAPs) {

            int Channel = AP.getChannel();
            if (Channel < 1 || Channel > 12) continue;

            int Level = AP.getLevel();
            int Security = AP.getSecurity();
            boolean WPS = AP.getWPS();
            String SSID = AP.getSSID();

            float alpha = 30 * Channel;

            float dx = (Main.maxSignalLevel - Level) * FloatMath.cos(alpha * (float) Math.PI / 180);
            float dy = (Main.maxSignalLevel - Level) * FloatMath.sin(alpha * (float)Math.PI / 180);

            float x = center.x + dx * zoom;
            float y = center.y - dy * zoom;

            int transparentValue = Level * 200 / Main.maxSignalLevel + 55;

            // Открытая сеть или WEP
            if (Security == 0 || Security == 1) {
                textPaint.setColor(Color.argb(transparentValue, 0x00, 0x60, 0x00));
                canvas.drawText(SSID, x + markerSize, y - markerSize, textPaint);
            }
            else
                // WPA или WPA2, но с поддержкой WPS
                if (Security == 2 && WPS) {
                    textPaint.setColor(Color.argb(transparentValue, 0x00, 0x00, 0x80));
                    canvas.drawText(SSID, x + markerSize, y - markerSize, textPaint);
                }
                else
                    // WPA или WPA2 без с поддержки WPS
                    textPaint.setColor(Color.argb(transparentValue, 0xFF, 0xFF, 0xFF));

            canvas.drawCircle(x, y, markerSize, textPaint);
        }
    }

    protected void setupView() {
// Окружности
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(Color.BLACK);
        circlePaint.setStyle(Paint.Style.STROKE);

        // Сетка
        axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisPaint.setColor(Color.argb(0x60, 0xFF, 0xFF, 0xFF));
        axisPaint.setStyle(Paint.Style.STROKE);
        axisPaint.setStrokeWidth(1);

        // Текст и маркеры
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setFakeBoldText(true);
        textPaint.setSubpixelText(true);
        textPaint.setTextAlign(Paint.Align.LEFT);

        // Радар
        mainGradientColors = new int[4];
        mainGradientColors[0] = Color.rgb(0xB8, 0xE0, 0xFF);
        mainGradientColors[1] = Color.rgb(0xA1, 0xCF, 0xFF);
        mainGradientColors[2] = Color.rgb(0x62, 0xAA, 0xFF);
        mainGradientColors[3] = Color.BLACK;

        mainGradientPositions = new float[4];
        mainGradientPositions[0] = 0.0f;
        mainGradientPositions[1] = 0.2f;
        mainGradientPositions[2] = 0.9f;
        mainGradientPositions[3] = 1.0f;

        mainGradientPaint = new Paint();

        // Стекло
        int glassColor = 0xF5;
        glassGradientColors = new int[5];
        glassGradientColors[0] = Color.argb(0, glassColor, glassColor, glassColor);
        glassGradientColors[1] = Color.argb(0, glassColor, glassColor, glassColor);
        glassGradientColors[2] = Color.argb(50, glassColor, glassColor, glassColor);
        glassGradientColors[3] = Color.argb(100, glassColor, glassColor, glassColor);
        glassGradientColors[4] = Color.argb(65, glassColor, glassColor, glassColor);

        glassGradientPositions = new float[5];
        glassGradientPositions[0] = 0.00f;
        glassGradientPositions[1] = 0.80f;
        glassGradientPositions[2] = 0.90f;
        glassGradientPositions[3] = 0.94f;
        glassGradientPositions[4] = 1.00f;

        glassPaint = new Paint();
    }

    private int measure(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.UNSPECIFIED) {
            result = 100;
        } else {
            result = specSize;
        }
        return result;
    }

    public void setData(ArrayList<WIFI> WiFiAPs) {
        this.WiFiAPs = WiFiAPs;
    }


}
