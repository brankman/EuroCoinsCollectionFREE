package moedas.euro.free;

import android.content.Intent;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdView;
//import com.google.android.gms.ads.MobileAds;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        /** Start ads**/
        //MobileAds.initialize(getApplicationContext(), getString(R.string.banner_ad_unit_id));
        //AdView mAdView = (AdView) findViewById(R.id.adView);
        //AdRequest adRequest = new AdRequest.Builder().build();
        //mAdView.loadAd(adRequest);

        Button bt_rate_it = (Button)findViewById(R.id.bt_rate_it);
        bt_rate_it.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=moedas.euro.free")));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=moedas.euro.free&feature=search_result#?t=W251bGwsMSwxLDEsIm1vZWRhcy5ldXJvLmZyZWUiXQ..")));
                }
            }
        });


        Button bt_goPro = (Button)findViewById(R.id.bt_goPro);
        bt_goPro.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=moedas.euro")));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=moedas.euro&feature=search_result#?t=W251bGwsMSwxLDEsIm1vZWRhcy5ldXJvIl0.")));
                }
            }
        });

    }
}
