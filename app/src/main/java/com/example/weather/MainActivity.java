package com.example.weather;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

public class MainActivity extends AppCompatActivity implements Runnable{
    HttpURLConnection httpConn = null;
    InputStream din = null;
    Vector<String> cityname = new Vector<String>();
    Vector<String> low = new Vector<String>();
    Vector<String> high = new Vector<String>();
    Vector<String> icon = new Vector<String>();
    Vector<Bitmap> bitmap = new Vector<Bitmap>();
    Vector<String> summary = new Vector<String>();
    int weatherIndex[] = new int[20];
    String city = "guangzhou";
    //boolean bPress = false;
    //boolean bHasData = false;
    LinearLayout body;
    Button find;
    EditText value;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("天气预报");
        body = findViewById(R.id.my_body);
        find = findViewById(R.id.find);
        value = findViewById(R.id.value);
        find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                body.removeAllViews();
                city = value.getText().toString();
                Toast.makeText(MainActivity.this,"正在查询天气信息……",Toast.LENGTH_LONG).show();
                Thread th = new Thread(MainActivity.this);
                th.start();
            }
        });

    }

    @Override
    public void run() {
        cityname.removeAllElements();
        low.removeAllElements();
        high.removeAllElements();
        icon.removeAllElements();
        bitmap.removeAllElements();
        summary.removeAllElements();
        parseData();
        downImage();
        Message message = new Message();
        message.what = 1;
        handler.sendMessage(message);
    }

    public void parseData(){
        int i=0;
        //String sValue;
        //city变量表示城市名字的拼音
        String weatherUrl="http://flash.weather.com.cn/wmaps/xml/"+city+".xml";
        //表示天气情况图标的基础网址
        String weatherIcon="http://m.weather.com.cn/img/c";
        try {
            URL url = new URL(weatherUrl);
            //建立天气预报查询连接
            httpConn = (HttpURLConnection) url.openConnection();
            //采用GET请求方法
            httpConn.setRequestMethod("GET");
            //打开数据输入流
            din = httpConn.getInputStream();
            //XmlPullParser xmlParser = Xml.newPullParser();
            //xmlParser.setInput(din, "UTF-8");
            InputStreamReader in = new InputStreamReader(httpConn.getInputStream());
            // 为输出创建BufferedReader
            BufferedReader buffer = new BufferedReader(in);

            //String inputLine = "";
            //String resultData = "";
            //使用循环来读取获得的数据
            //while ((inputLine = buffer.readLine()) != null) {
                //在每一行后面加上一个"\n"来换行
            //    resultData += inputLine + "\n";
            //}
            //获得XmlPullParser解析器
            XmlPullParser xmlParser = Xml.newPullParser();
            //ByteArrayInputStream tInputStringStream = null;
            //获得解析到的事件类别,这里有开始文档,结束文档,开始标签,结束标签,文本等等事件
            //tInputStringStream = new ByteArrayInputStream(resultData.getBytes());tInputStringStream
            xmlParser.setInput(din, "UTF-8");

            int evtType = xmlParser.getEventType();
            while (evtType != XmlPullParser.END_DOCUMENT)//一直循环,直到文档结束
            {
                switch (evtType) {
                    case XmlPullParser.START_TAG:
                        String tag = xmlParser.getName();
                        //如果是city标签开始,则说明需要实例化对象了
                        if (tag.equalsIgnoreCase("city")) {
                            //城市天气预报
                            cityname.addElement(xmlParser.getAttributeValue(null, "cityname") + "天气：");
                            //天气情况概述
                            summary.addElement(xmlParser.getAttributeValue(null, "stateDetailed"));
                            //最低温度
                            low.addElement("最低：" + xmlParser.getAttributeValue(null, "tem2"));
                            //最高温度
                            high.addElement("最高：" + xmlParser.getAttributeValue(null, "tem1"));
                            //天气情况图标网址
                            icon.addElement(weatherIcon + xmlParser.getAttributeValue(null, "state1") + ".gif");

                        }
                        break;

                    case XmlPullParser.END_TAG:
                        //标签结束
                    default:
                        break;
                }
                //如果xml没有结束,则导航到下一个节点
                evtType = xmlParser.next();
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }finally {
            {
                try{
                    din.close();
                    httpConn.disconnect();
                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        }
    }

    private void downImage()
    {
        //天气情况图标获取
        int i=0;
        for(i=0;i<icon.size();i++){
            try
            {
                URL url=new URL(icon.elementAt(i));
                System.out.println(icon.elementAt(i));
                httpConn =(HttpURLConnection)url.openConnection();
                httpConn.setRequestMethod("GET");
                din =httpConn.getInputStream();
                //Vector<Bitmap> bitmap = new Vector<Bitmap>();
                //图片数据Bitmap
                bitmap.addElement(BitmapFactory.decodeStream(httpConn.getInputStream()));
            }catch (Exception ex){
                ex.printStackTrace();
            }finally{
                //释放连接
                try{
                    din.close();
                    httpConn.disconnect();
                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private final Handler handler=new Handler(){
        public void handleMessage(Message msg){
            switch(msg.what){
                case 1:
                    //调用第(1)步中的代码,来更新主界面的天气信息
                    showData();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public void showData(){
        body.removeAllViews();//清除存储旧的查询结果的组件
        body.setOrientation(LinearLayout.VERTICAL);
        LayoutParams params = new LayoutParams(
                LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        params.weight=80;
        params.height=50;
        for(int i=0;i<cityname.size();i++){
            LinearLayout linerlayout=new LinearLayout(this);
            linerlayout.setOrientation(LinearLayout.HORIZONTAL);
            //城市
            TextView dayView=new TextView(this);
            dayView.setLayoutParams(params);
            dayView.setText(cityname.elementAt(i));
            linerlayout.addView(dayView);
            //描述
            TextView  summaryView=new TextView(this);
            summaryView.setLayoutParams(params);
            summaryView.setText(summary.elementAt(i));
            linerlayout.addView(summaryView);
            //图标
            ImageView icon=new ImageView(this);
            icon.setLayoutParams(params);
            icon.setImageBitmap(bitmap.elementAt(i));
            linerlayout.addView(icon);
            //最低气温
            TextView lowView=new TextView(this);
            lowView.setLayoutParams(params);
            lowView.setText(low.elementAt(i));
            linerlayout.addView(lowView);
            //最高气温
            TextView  highView=new TextView(this);
            highView.setLayoutParams(params);
            highView.setText(high.elementAt(i));
            linerlayout.addView(highView);
            body.addView(linerlayout);
        }
    }
}
