package edu.wuwang.ffmpeg.demo;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import edu.wuwang.ffmpeg.R;
import java.lang.reflect.InvocationTargetException;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by aiya on 2017/6/13.
 */

public class DemoActivity extends AppCompatActivity {

    private TextView mTvInfo;
    private Button[] mBtn;
    private GLSurfaceView mGLView;
    private int[] btnIds=new int[]{
        R.id.mBtn1,R.id.mBtn2,R.id.mBtn3,R.id.mBtn4
    };

    private DemoBase demo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_base);
        String demoName=getIntent().getStringExtra("demo");
        try {
            demo=(DemoBase) Class.forName(demoName).getConstructor(DemoActivity.class).newInstance(this);
        } catch (InstantiationException | NoSuchMethodException | IllegalAccessException | ClassNotFoundException | InvocationTargetException e) {
            e.printStackTrace();
        }
        mBtn=new Button[4];
        for (int i=0;i<4;i++){
            mBtn[i]=(Button) findViewById(btnIds[i]);
        }
        mTvInfo=(TextView) findViewById(R.id.mTvInfo);
        mGLView=(GLSurfaceView) findViewById(R.id.mGLView);

        demo.onViewCreated();
        demo.configGLView(mGLView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mGLView!=null){
            mGLView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mGLView!=null){
            mGLView.onPause();
        }
    }

    public void setBtnText(String ... txt){
        for (int i=0;i<txt.length;i++){
            mBtn[i].setVisibility(View.VISIBLE);
            mBtn[i].setText(txt[i]);
        }
    }

    public void outInfo(final String info){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTvInfo.setText(mTvInfo.getText()+"\n"+info);
            }
        });
    }

    public void clearInfo(){
        mTvInfo.setText("");
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.mBtn1:
                demo.onBtn1Click(view);
                break;
            case R.id.mBtn2:
                demo.onBtn2Click(view);
                break;
            case R.id.mBtn3:
                demo.onBtn3Click(view);
                break;
            case R.id.mBtn4:
                demo.onBtn4Click(view);
                break;
        }
    }

}
