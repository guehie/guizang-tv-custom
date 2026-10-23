package io.github.op7418.guizangtv;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Build;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/** Xiaomi system location -> Open-Meteo. No advertising SDK or background polling. */
final class LocalWeather {
    private static final long FRESH=30*60000L, EXPIRES=2*60*60000L;
    private final Context context;
    private final SharedPreferences cache;
    private final Handler main=new Handler(Looper.getMainLooper());
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private Runnable changed;
    private boolean loading,failed,closed;
    private long attempted=-FRESH, observed;
    private int code=-1;
    private double temperature;
    private Place place;
    private String sampleKey="";
    private SSLSocketFactory legacyTls;
    LocalWeather(Context c,Runnable changed) {
        context=c.getApplicationContext(); this.changed=changed;
        cache=context.getSharedPreferences("local-weather",Context.MODE_PRIVATE);
        place=location();
        observed=cache.getLong("observed",0); code=cache.getInt("code",-1);
        temperature=cache.getFloat("temperature",Float.NaN); sampleKey=cache.getString("place","");
    }
    void refreshIfNeeded() {
        if(closed||loading)return;
        long elapsed=SystemClock.elapsedRealtime();
        if(elapsed-attempted<(failed?5*60000L:FRESH))return;
        Place latest=location();
        if(!latest.valid()){place=latest;attempted=elapsed;notifyChanged();return;}
        boolean moved=!latest.key().equals(place.key());place=latest;
        long age=System.currentTimeMillis()-observed;
        if(!moved && !failed && sampleKey.equals(place.key()) && age>=0 && age<FRESH){attempted=elapsed-age;return;}
        attempted=elapsed;loading=true;
        Place target=place;
        worker.execute(()->{
            try {
                String url=String.format(Locale.ROOT,"https://api.open-meteo.com/v1/forecast?latitude=%.2f&longitude=%.2f&current=temperature_2m,weather_code&timezone=Asia%%2FShanghai&timeformat=unixtime&forecast_days=1",target.lat,target.lon);
                JSONObject current=new JSONObject(request(url)).getJSONObject("current");
                double temp=current.getDouble("temperature_2m"); int weather=current.getInt("weather_code");
                long time=current.getLong("time")*1000L, now=System.currentTimeMillis();
                if(Double.isNaN(temp)||Double.isInfinite(temp)||temp < -100||temp>70||weather<0||weather>99||time<now-EXPIRES||time>now+60000L)
                    throw new IllegalArgumentException("Weather sample invalid or expired");
                cache.edit().putLong("observed",time).putInt("code",weather).putFloat("temperature",(float)temp).putString("place",target.key()).apply();
                main.post(()->{if(closed)return;observed=time;code=weather;temperature=temp;sampleKey=target.key();loading=false;failed=false;notifyChanged();});
            } catch(Exception unavailable) {
                Log.w("GuiZangWeather","Weather update unavailable: "+unavailable.getClass().getSimpleName());
                main.post(()->{if(closed)return;loading=false;failed=true;notifyChanged();});
            }
        });
    }
    String line() {
        if(!place.valid())return "等待电视定位";
        long age=System.currentTimeMillis()-observed;
        if(!sampleKey.equals(place.key()) || code<0 || age<0 || age>EXPIRES || Double.isNaN(temperature))
            return place.label+" · "+(loading?"天气加载中":"天气暂不可用");
        return place.label+"  "+Math.round(temperature)+"°C  "+description(code);
    }
    String note() {
        if(!place.valid())return "未获取有效的电视系统位置，不请求天气";
        if(!sampleKey.equals(place.key())||observed==0)return place.source+" · "+(failed?"联网失败，稍后重试":"正在获取天气");
        String time=ChineseAlmanac.format("HH:mm").format(new Date(observed));
        long age=System.currentTimeMillis()-observed;
        return place.source+" · "+(age>EXPIRES||age<0?"已过期 ":failed?"缓存 ":"")+time+" 更新";
    }
    void show(Activity a) {
        new AlertDialog.Builder(a).setTitle("日期与天气")
                .setMessage(line()+"\n"+note()+"\n\n优先使用电视系统保存的自动定位；位置不可用时不请求天气，不使用固定城市代替。"
                        +"\n\n天气每 30 分钟更新，断网时标明缓存，超过 2 小时不显示旧温度。"
                        +"\n\n天气：Open-Meteo，CC BY 4.0。农历与节气：香港天文台 2025–2035 年历表（北京时间，按日期显示）。")
                .setPositiveButton("刷新天气",(d,w)->{attempted=-FRESH;observed=0;refreshIfNeeded();notifyChanged();})
                .setNegativeButton("返回",null).show();
    }
    void close() {closed=true;changed=null;worker.shutdownNow();main.removeCallbacksAndMessages(null);}
    private void notifyChanged(){if(changed!=null)changed.run();}
    private Place location() {
        for(String key:new String[]{"mitv_location","mitv_user_location"}) try {
            String raw=Settings.System.getString(context.getContentResolver(),key);
            if(raw==null||raw.isEmpty())continue;
            JSONObject value=new JSONObject(raw);double lat=value.getDouble("latitude"),lon=value.getDouble("longitude");
            if(Double.isNaN(lat)||Double.isInfinite(lat)||Double.isNaN(lon)||Double.isInfinite(lon)||Math.abs(lat)>90||Math.abs(lon)>180||(lat==0&&lon==0))continue;
            String city=clean(value.optString("city","本地")),district=clean(value.optString("district",""));
            if(!district.isEmpty()&&!district.endsWith("区")&&!district.endsWith("县")&&!district.endsWith("市"))district+="区";
            return new Place(lat,lon,city+(district.isEmpty()?"":"·"+district),"电视定位");
        }catch(Exception invalid){/* Ignore missing or invalid Xiaomi system location. */}
        return new Place(0,0,"本地","未获取位置");
    }
    private static String clean(String value){String s=value.replace('\n',' ').replace('\r',' ').trim();return s.substring(0,Math.min(s.length(),16));}
    private String request(String address) throws Exception {
        HttpsURLConnection c=(HttpsURLConnection)new URL(address).openConnection();
        if(Build.VERSION.SDK_INT<26)c.setSSLSocketFactory(legacyTls());
        c.setConnectTimeout(10000);c.setReadTimeout(10000);c.setInstanceFollowRedirects(false);
        try {
            if(c.getResponseCode()!=200)throw new java.io.IOException("Weather HTTP status "+c.getResponseCode());
            try(InputStream in=c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){
                byte[] buffer=new byte[2048];int n;
                while((n=in.read(buffer))!=-1){if(out.size()+n>32768)throw new java.io.IOException("Weather response too large");out.write(buffer,0,n);}
                return out.toString("UTF-8");
            }
        }finally{c.disconnect();}
    }
    private SSLSocketFactory legacyTls() throws Exception {
        if(legacyTls!=null)return legacyTls;
        // Android 6 lacks these public CA roots. Keep chain and hostname verification enabled,
        // and apply this store only to the fixed Open-Meteo connection, never system-wide.
        KeyStore roots=KeyStore.getInstance(KeyStore.getDefaultType());roots.load(null,null);
        CertificateFactory certificates=CertificateFactory.getInstance("X.509");
        for(String name:new String[]{"isrg_root_x1.pem","isrg_root_x2.pem"})
            try(InputStream in=context.getAssets().open(name)){roots.setCertificateEntry(name,certificates.generateCertificate(in));}
        TrustManagerFactory trust=TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());trust.init(roots);
        SSLContext tls=SSLContext.getInstance("TLS");tls.init(null,trust.getTrustManagers(),null);
        legacyTls=tls.getSocketFactory();return legacyTls;
    }
    private static String description(int code) {
        if(code==0)return "晴";if(code==1)return "少云";if(code==2)return "多云";if(code==3)return "阴";
        if(code==45||code==48)return "雾";if(code>=51&&code<=57)return "毛毛雨";
        if(code>=61&&code<=67)return "雨";if(code>=71&&code<=77)return "雪";
        if(code>=80&&code<=82)return "阵雨";if(code==85||code==86)return "阵雪";
        if(code>=95)return "雷雨";return "天气状况未知";
    }
    private static final class Place {
        final double lat,lon;final String label,source;
        Place(double lat,double lon,String label,String source){this.lat=lat;this.lon=lon;this.label=label;this.source=source;}
        boolean valid(){return lat!=0 || lon!=0;}
        String key(){return String.format(Locale.ROOT,"%.2f,%.2f",lat,lon);}
    }
}
