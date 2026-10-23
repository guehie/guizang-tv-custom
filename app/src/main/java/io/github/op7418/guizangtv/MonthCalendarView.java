package io.github.op7418.guizangtv;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/** A quiet, Monday-first month grid; updates only when the local date changes. */
final class MonthCalendarView extends View {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final HomeAppearance style;
    private final float density,scaledDensity;
    private int year,month,today,offset,days;
    MonthCalendarView(Context context,HomeAppearance style){
        super(context);this.style=style;
        density=getResources().getDisplayMetrics().density;
        scaledDensity=getResources().getDisplayMetrics().scaledDensity;
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
    }
    void date(long now){
        Calendar date=Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"),Locale.CHINA);
        date.setTimeInMillis(now);
        int y=date.get(Calendar.YEAR),m=date.get(Calendar.MONTH)+1,d=date.get(Calendar.DAY_OF_MONTH);
        if(year==y&&month==m&&today==d)return;
        year=y;month=m;today=d;days=date.getActualMaximum(Calendar.DAY_OF_MONTH);
        date.set(Calendar.DAY_OF_MONTH,1);offset=(date.get(Calendar.DAY_OF_WEEK)+5)%7;
        setContentDescription(year+"年"+month+"月"+today+"日，月历");invalidate();
    }
    private void text(Canvas canvas,String text,float x,float y,float size,int alpha,Paint.Align align){
        paint.setColor(style.ink());paint.setAlpha(alpha);paint.setTextAlign(align);
        paint.setTypeface(style.typeface());paint.setTextSize(size*scaledDensity);
        canvas.drawText(text,x,y*density,paint);
    }
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);if(days==0)return;
        float width=getWidth(),column=width/7;
        // The oversized month stays behind the grid rather than introducing a new card.
        text(canvas,String.format(Locale.ROOT,"%02d",month),width*.55f,150,154,19,Paint.Align.CENTER);
        text(canvas,year+"年 · "+month+"月",width,19,13,215,Paint.Align.RIGHT);
        String[] weekdays={"一","二","三","四","五","六","日"};
        for(int i=0;i<7;i++)text(canvas,weekdays[i],column*(i+.5f),44,12,165,Paint.Align.CENTER);
        for(int day=1;day<=days;day++){
            int slot=offset+day-1;float x=column*(slot%7+.5f),baseline=69+(slot/7)*23;
            text(canvas,Integer.toString(day),x,baseline,day==today?18:14,day<today?85:255,Paint.Align.CENTER);
            if(day==today){
                paint.setColor(0xFFF16B64);paint.setStrokeWidth(2*density);paint.setStrokeCap(Paint.Cap.ROUND);
                canvas.drawLine(x-5*density,(baseline+6)*density,x+5*density,(baseline+6)*density,paint);
            }
        }
    }
}
