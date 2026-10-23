package io.github.op7418.guizangtv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

/** Offline month starts and solar-term dates from Hong Kong Observatory, UTC+8. */
final class ChineseAlmanac {
    static final TimeZone ZONE = TimeZone.getTimeZone("Asia/Shanghai");
    private final TreeMap<String,String> months = new TreeMap<>(), terms = new TreeMap<>();
    private final SimpleDateFormat key = format("yyyy-MM-dd");
    private final SimpleDateFormat date = format("yyyy年M月d日  EEEE");
    private String cachedDay = "", lunar = "农历暂不可用", term = "";
    ChineseAlmanac(InputStream source) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(source, "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] row = line.split("\t");
                if (row.length != 3) continue;
                if (row[0].equals("M")) months.put(row[1],row[2]);
                else if (row[0].equals("S")) terms.put(row[1],row[2]);
            }
        }
    }
    static SimpleDateFormat format(String pattern) {
        SimpleDateFormat f = new SimpleDateFormat(pattern,Locale.CHINA);
        f.setTimeZone(ZONE); f.setLenient(false); return f;
    }
    String date(long now) { return date.format(new Date(now)); }
    String lunar(long now) { update(now); return lunar; }
    String term(long now) { update(now); return term; }
    private void update(long now) {
        String today = key.format(new Date(now));
        if (today.equals(cachedDay)) return;
        cachedDay=today; lunar="农历暂不可用"; term="";
        if (today.compareTo("2025-01-01")<0 || today.compareTo("2035-12-31")>0) return;
        Map.Entry<String,String> month=months.floorEntry(today);
        if (month!=null) try {
            int day=(int)((key.parse(today).getTime()-key.parse(month.getKey()).getTime())/86400000L)+1;
            if(day>=1&&day<=30) lunar="农历"+month.getValue()+dayName(day);
        } catch (ParseException invalid) { /* Keep the explicit unavailable label. */ }
        String current=terms.get(today);
        if(current!=null) term="今日 · "+current;
        else {
            Map.Entry<String,String> next=terms.higherEntry(today);
            if(next!=null) term="下个节气 · "+next.getValue()+"  "+next.getKey().substring(5).replace('-','/');
        }
    }
    private static String dayName(int day) {
        if(day==10)return "初十"; if(day==20)return "二十"; if(day==30)return "三十";
        return new String[]{"初","十","廿"}[(day-1)/10]+"一二三四五六七八九".charAt((day-1)%10);
    }
}
