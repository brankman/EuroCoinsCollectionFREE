package moedas.euro.free;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class StartScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);

        Thread logoTimer = new Thread(){
            public void run(){
                try{
                    int logoTimer = 0;

                    while(logoTimer < 1000){
                        sleep(100);
                        logoTimer+=100;
                    }
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }catch (Exception e) {
                    e.printStackTrace();
                }finally{
                    finish();
                }
            }
        };
        logoTimer.start();
    }
}
