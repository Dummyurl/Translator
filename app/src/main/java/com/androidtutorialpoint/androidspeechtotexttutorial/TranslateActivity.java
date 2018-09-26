package com.androidtutorialpoint.androidspeechtotexttutorial;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidtutorialpoint.androidspeechtotexttutorial.api.APICalls;
import com.androidtutorialpoint.androidspeechtotexttutorial.api.data_responses.TranslateResponse;
import com.androidtutorialpoint.androidspeechtotexttutorial.views.CircleView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import at.markushi.ui.CircleButton;
import co.ronash.pushe.Pushe;
import retrofit.Call;
import retrofit.Callback;
import retrofit.JacksonConverterFactory;
import retrofit.Retrofit;

public class TranslateActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private TextView header, toText, toLabel, fromLabel;
    private ImageView micFrom, volFrom, volTo, reverse;
    private EditText fromText;
    private CircleButton convertBTN;
    private CircleButton convertLayout;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private final int SR_CODE = 123;
    private String ENGLISH_CONSTANT = "";//getString(R.string.FIRSTLANG);//"ENGLIGH";
    private String CHINESE_CONSTANT = "";//getString(R.string.FIRSTLANGR);;

    private String ENGLISH_CONVERT = "";//getString(R.string.FIRSTLANGCODE)+"-"+getString(R.string.FIRSTLANGCODER);//"en-zh";
    private String CHINESE_CONVERT = "";//getString(R.string.FIRSTLANGCODER)+"-"+getString(R.string.FIRSTLANGCODE);;//"zh-en";
    private String CHINESE_CODE = "";//getString(R.string.FIRSTLANGCODER);
    private String currentFrom = "";//ENGLISH_CONSTANT;

    TextToSpeech ttsEnglish, ttsChinese;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.translate_layout);
        setTracker();
        setUpAds();
        setUpInitialConstants();
        setUpInteraction();
        setUpSpeakingListeners();
        setUpListeners();
        Pushe.initialize(this,true);
    }
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;

    private void setUpAds(){
        MobileAds.initialize(this, getString(R.string.YOUR_ADMOB_AD_ID));
        if(getString(R.string.do_you_want_banner_ads).equalsIgnoreCase("Yes")) {
            mAdView = (AdView) findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }
        if(getString(R.string.do_you_want_interstitial_ads).equalsIgnoreCase("Yes")) {
            mInterstitialAd = new InterstitialAd(this);
            mInterstitialAd.setAdUnitId(getString(R.string.YOUR_ADMOB_INT_AD_ID));
            mInterstitialAd.loadAd(new AdRequest.Builder().build());
            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    // Load the next interstitial.
                    mInterstitialAd.loadAd(new AdRequest.Builder().build());
                }

            });
        }
    }
    private void showIntAd(){
        if(getString(R.string.do_you_want_interstitial_ads).equalsIgnoreCase("Yes")) {
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            } else {
                Log.d("TAG", "The interstitial wasn't loaded yet.");
            }
        }
    }

    private void setUpInitialConstants() {
        ENGLISH_CONSTANT = getString(R.string.FIRSTLANG);//"ENGLIGH";
        CHINESE_CONSTANT = getString(R.string.FIRSTLANGR);
        ;

        ENGLISH_CONVERT = getString(R.string.FIRSTLANGCODE) + "-" + getString(R.string.FIRSTLANGCODER);//"en-zh";
        CHINESE_CONVERT = getString(R.string.FIRSTLANGCODER) + "-" + getString(R.string.FIRSTLANGCODE);
        ;//"zh-en";
        CHINESE_CODE = getString(R.string.FIRSTLANGCODER);
        currentFrom = ENGLISH_CONSTANT;

    }

    private void setUpSpeakingListeners() {
        ttsEnglish = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                // TODO Auto-generated method stub
                if (status == TextToSpeech.SUCCESS)
                { //Locale loc = new Locale("fa_IR");
                    int result = ttsEnglish.setLanguage(Locale.ENGLISH);
                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                            result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("error", "This Language is not supported");
                    } else {
                        //ConvertTextToSpeech();
                    }
                } else
                    Log.e("error", "Initilization Failed!");
            }
        });

        ttsChinese = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                // TODO Auto-generated method stub
                if (status == TextToSpeech.SUCCESS) {
                    int result = ttsChinese.setLanguage(new Locale("fa"));
                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                            result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("error", "This Language is not supported");
                    } else {
                        //ConvertTextToSpeech();
                    }
                } else
                    Log.e("error", "Initilization Failed!");
            }
        });
    }
    private Tracker mTracker;

    public void setTracker(){
        /*Google Analytics: send screen Name*/

        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getTracker(AnalyticsApplication.TrackerName.APP_TRACKER);

        // Send a screen view.
        mTracker.setScreenName(getString(R.string.app_name));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        mTracker.send(new HitBuilders.ScreenViewBuilder()
                .set(getString(R.string.app_name), getString(R.string.app_name))
                .build());

    }
    private void setUpListeners() {
        /*reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callReverse();
            }
        });*/
        convertLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callReverse();
            }
        });
        micFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askSpeechInput(currentFrom);
            }
        });
        volFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConvertTextToSpeech(currentFrom, fromText.getText().toString());
            }
        });
        volTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentFromR = reverseCurrentFrom(currentFrom);
                ConvertTextToSpeech(currentFromR, toText.getText().toString());
            }
        });
        convertBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentFrom.equals(ENGLISH_CONSTANT))
                    translateText(fromText.getText().toString(), ENGLISH_CONVERT);
                else
                    translateText(fromText.getText().toString(), CHINESE_CONVERT);
            }
        });
        toText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (copyToClipboard(getApplicationContext(), toText.getText().toString())) {
                    if (currentFrom.equals(ENGLISH_CONSTANT)) {
                        Toast.makeText(getApplicationContext(), getString(R.string.Copied), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.CopiedR), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        fromText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                fromText.setFocusableInTouchMode(true);

                return false;
            }
        });
    }

    private String reverseCurrentFrom(String currentFrom) {
        if (currentFrom.equals(ENGLISH_CONSTANT)) {
            return CHINESE_CONSTANT;
        } else return ENGLISH_CONSTANT;
    }

    private void callReverse() {
        if (currentFrom.equals(ENGLISH_CONSTANT)) {
            currentFrom = CHINESE_CONSTANT;
            //header.setText(getString(R.string.convertHeaderR));
            fromLabel.setText(getString(R.string.fromR));
            fromText.setHint(getString(R.string.fromHintR));
            toLabel.setText(getString(R.string.toR));
            toText.setText(getString(R.string.toHintR));
        } else {
            currentFrom = ENGLISH_CONSTANT;
            //header.setText(getString(R.string.convertHeader));
            fromLabel.setText(getString(R.string.from));
            fromText.setHint(getString(R.string.fromHint));
            toLabel.setText(getString(R.string.to));
            toText.setText(getString(R.string.toHint));
        }
        fromText.setText("");
        fromText.setFocusable(false);
    }

    private void setUpInteraction() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        /*private TextView header,toText,toLabel,fromLabel;
        private Button micFrom,volFrom,volTo,convertBTN;
        private EditText fromText;*/
        //header = (TextView) findViewById(R.id.header);
        toText = (TextView) findViewById(R.id.toText);
        toLabel = (TextView) findViewById(R.id.toLabel);
        fromLabel = (TextView) findViewById(R.id.fromLabel);


        micFrom = (ImageView) findViewById(R.id.micFrom);
        volFrom = (ImageView) findViewById(R.id.volFrom);
        volTo = (ImageView) findViewById(R.id.toVol);
        //reverse = (ImageView) findViewById(R.id.reverse);
        convertBTN = (CircleButton) findViewById(R.id.convertBTN);

        fromText = (EditText) findViewById(R.id.fromText);
        convertLayout = (CircleButton) findViewById(R.id.swapLanguage);
        fromText.setFocusable(false);
        currentFrom = CHINESE_CONSTANT;
        callReverse();
    }

    private void ConvertTextToSpeech(String lang_type, String text) {

        //translateText(text,"en-zh");
        if (text == null || "".equals(text)) {
            if (lang_type.equals(ENGLISH_CONSTANT)) {
                text = getString(R.string.emptyEnglish);
            } else {
                text = getString(R.string.emptyChinese);
            }
        }
        if (lang_type.equals(ENGLISH_CONSTANT)) {
            ttsEnglish.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        } else {
            ttsChinese.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            showIntAd();
        }

    }

    private void askSpeechInput(String lang_type) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);


        if (lang_type.equals(ENGLISH_CONSTANT)) {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                    "Hi speak something");
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } else {
            //Specify language
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, CHINESE_CODE);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, CHINESE_CODE);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, CHINESE_CODE);
            intent.putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES, CHINESE_CODE);
            intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, CHINESE_CODE);
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, CHINESE_CODE);
            intent.putExtra(RecognizerIntent.EXTRA_RESULTS, CHINESE_CODE);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                    getString(R.string.audiodisplayChinese));
            startActivityForResult(intent, SR_CODE);

        }
    }

    // Receiving speech input

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    fromText.setText(result.get(0));
                }
                break;
            }
            case SR_CODE: {
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        ArrayList<String> nBestList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        String bestResult = nBestList.get(0);
                        fromText.setText(bestResult);
                    }
                }
                break;
            }

        }

        if (currentFrom.equals(ENGLISH_CONSTANT))
            translateText(fromText.getText().toString(), ENGLISH_CONVERT);
        else
            translateText(fromText.getText().toString(), CHINESE_CONVERT);

    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            //textToSpeech();
        } else {
            Log.e("TTS", "Initialization failed");
        }
    }

    String text_to_return = "";

    private String translateText(final String text, final String lang) {
        class getQData extends AsyncTask<String, String, String> {
            ProgressDialog loading;
            String ROOT_URL = getResources().getString(R.string.ROOT_URL);

            Retrofit adapter = new Retrofit.Builder()
                    .baseUrl(ROOT_URL)
                    .addConverterFactory(JacksonConverterFactory.create())
                    .build();
            APICalls api = adapter.create(APICalls.class);

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showPD();

            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

            }

            @Override
            protected String doInBackground(String... params) {
                text_to_return = "";
                String key = Constants.MY_KEY;
                Call<TranslateResponse> call = api.translate(key, text, lang);
                call.enqueue(new Callback<TranslateResponse>() {
                    @Override
                    public void onResponse(retrofit.Response<TranslateResponse> response, Retrofit retrofit) {
                        //loading.dismiss();
                        hidePD();
                        Log.d("succ", "onResponse:code" + String.valueOf(response.code()));
                        Log.d("error mesg", String.valueOf(response.message()));
                        switch (response.code()) {
                            case 200:
                                TranslateResponse tr = response.body();
                                text_to_return = tr.getText().get(0);
                                toText.setText(text_to_return);
                                String currentFromR = reverseCurrentFrom(currentFrom);
                                ConvertTextToSpeech(currentFromR, toText.getText().toString());
                                break;
                            default:
                                if (currentFrom.equals(ENGLISH_CONSTANT))
                                    Toast.makeText(getApplicationContext(), getString(R.string.APInot200), Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(getApplicationContext(), getString(R.string.APInot200R), Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        pd.dismiss();
                        Log.d("retro error", t.getMessage());
                        if (currentFrom.equals(ENGLISH_CONSTANT))
                            Toast.makeText(getApplicationContext(), getString(R.string.InternetConnectionError), Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getApplicationContext(), getString(R.string.InternetConnectionErrorR), Toast.LENGTH_SHORT).show();


                    }
                });

                return text_to_return;
            }
        }

        getQData ru = new getQData();
        try {
            ru.execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return text_to_return;
    }

    ProgressDialog pd;

    private void showPD() {
        pd = new ProgressDialog(TranslateActivity.this);
        if (currentFrom.equals(ENGLISH_CONSTANT)) {
            pd.setMessage(getString(R.string.convertingPD));
        } else {
            pd.setMessage(getString(R.string.convertingPDR));
        }
        pd.show();
    }

    private void hidePD() {

        pd.dismiss();
    }

    public boolean copyToClipboard(Context context, String text) {
        try {
            int sdk = android.os.Build.VERSION.SDK_INT;
            if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
                android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context
                        .getSystemService(context.CLIPBOARD_SERVICE);
                clipboard.setText(text);
            } else {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context
                        .getSystemService(context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData
                        .newPlainText("copy", text);
                clipboard.setPrimaryClip(clip);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_rate) {
            actionRateUs();
        }


        if (id == R.id.action_share) {
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = "";
            if (currentFrom.equals(ENGLISH_CONSTANT)) {
                shareBody = getString(R.string.share_subject) + " " + "http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName();
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
            } else {
                shareBody = getString(R.string.share_subjectR) + " " + "http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName();
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.share_subjectR));
            }
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, "Share via"));

        }
        return super.onOptionsItemSelected(item);
    }

    private void actionRateUs() {
        Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName())));
        }
    }
}
