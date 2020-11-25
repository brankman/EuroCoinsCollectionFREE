package moedas.euro.free;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.core.app.NavUtils;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdView;
//import com.google.android.gms.ads.MobileAds;

public class CountriesActivity extends AppCompatActivity {

    static Database mDataBase;
    Functions classFunctions = new Functions(this);
    static long selected_index = 0;
    ListView lv;
    static int listView_index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countries);
        getSupportActionBar().setTitle(R.string.title_countries);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /** Start ads**/
        classFunctions.startAds();

        lv = findViewById(R.id.lv_countries);
        mDataBase = new Database(this);
        try {
            mDataBase.open();
            Cursor c = mDataBase.selectCountryCoins();
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                    selected_index = arg3;
                    Intent i = new Intent(getApplicationContext(),CountriesCoinsActivity.class);
                    i.putExtra("SelectedCountryIndex", selected_index);
                    startActivity(i);
                    finish();
                }
            });

            CustomCursorAdapter newAdapter = new CustomCursorAdapter(this, c);
            lv.setAdapter(newAdapter);

            /*este IF serve posicionar a lista no mesmo ponto quando voltamos novamente a este ecrã*/
            boolean isBack = getIntent().getExtras().getBoolean("isBack");
            if(listView_index != 0 && isBack) {
                lv.setSelection(listView_index);
            }
            else {
                lv.setSelection(0);
            }

        }catch(Exception ex){
            ex.printStackTrace();
        }
        finally {
            mDataBase.close();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpTo(this, new Intent(getApplicationContext(), MainActivity.class));
            listView_index = 0;
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*este método permite guardar a posição da listView quando se sai deste ecrã*/
    @Override
    protected void onPause() {
        super.onPause();
        listView_index = lv.getFirstVisiblePosition();
    }

    private class CustomCursorAdapter extends CursorAdapter {

        public CustomCursorAdapter(Context context, Cursor c) {
            super(context, c);
        }

        // The newView method is used to inflate a new view and return it,
        // you don't bind any data to the view at this point.
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.list_custom_simple, parent, false);
        }

        // The bindView method is used to bind all data to a given view
        // such as setting the text on a TextView.
        @Override
        public void bindView(View view, Context context, Cursor cursor) {

            byte[] blobFlag = cursor.getBlob(cursor.getColumnIndex("country_flag"));
            if (blobFlag != null) {
                ImageView imgFlag = view.findViewById(R.id.iv_icon);
                imgFlag.setImageBitmap(BitmapFactory.decodeByteArray(blobFlag, 0, blobFlag.length));
            }

            TextView title = view.findViewById(R.id.tv_title_name);
            String country_name = cursor.getString(cursor.getColumnIndex("country_name"));

            if(country_name != null)
            {
                int resId = context.getResources().getIdentifier(country_name, "string", "moedas.euro.free");
                title.setText(context.getString(resId));
            }

            try {
                mDataBase.open();
                Cursor crs = mDataBase.selectCountryCoinsCount(country_name);
                if (crs.moveToFirst()) {
                    int totalCoinsHave = crs.getInt(crs.getColumnIndex("totalCoinsCountry"));

                    TextView stat = view.findViewById(R.id.tv_subtitle_stats);
                    stat.setText(getString(R.string.subtitleList) + " " + totalCoinsHave + "/8");
                }
            }catch(Exception ex){
                ex.printStackTrace();
            }finally {
                mDataBase.close();
            }
        }
    }
}
