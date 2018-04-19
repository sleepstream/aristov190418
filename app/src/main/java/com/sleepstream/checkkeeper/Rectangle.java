package com.sleepstream.checkkeeper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class Rectangle extends View {

        private Paint paint = new Paint();
        private Paint paintText = new Paint();
        private Paint paintLine = new Paint();
        Rectangle(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) { // Override the onDraw() Method
            super.onDraw(canvas);


            int x0 = canvas.getWidth()/2;
            int y0 = canvas.getHeight()/2;
            int dx = canvas.getHeight()/3;
            int dy = canvas.getHeight()/3;

            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.GREEN);
            paint.setStrokeWidth(5);

            paintText.setStyle(Paint.Style.STROKE);
            paintText.setColor(Color.GREEN);
            paintText.setStrokeWidth(2);
            paintText.setTextSize(50);


            int lineWidth = canvas.getHeight()/3+dy*2/5;
            paintLine.setStyle(Paint.Style.STROKE);
            paintLine.setColor(R.color.colorBackground);
            paintLine.setStrokeWidth(lineWidth);
            //center

            //draw guide box
            canvas.drawRect(x0-dx+dx/5, y0-dy+dy/5, x0+dx-dx/5, y0+dy-dy/5, paint);
            canvas.drawLine(0, 0, x0*2, 0, paintLine);
            canvas.drawLine(0, canvas.getHeight(), x0*2, canvas.getHeight(), paintLine);
            canvas.drawText(getResources().getString(R.string.Take_picture), 0, 50, paintText);
        }
}
