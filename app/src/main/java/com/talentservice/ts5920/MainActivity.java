package com.talentservice.ts5920;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.app.AlertDialog;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    // 1: the target name didn't existing in any place
    // 2: the target existed in other uid
    // 3: the target existed in this uid
    // 4: error
    public static final int NO_SUCH_TARGETNAME = 1;
    public static final int OTHER_UID_HAS_TARGETNAME = 2;
    public static final int THIS_UID_HAS_TARGETNAME =3;
    public static final int UNKNOW_TARGET_STATUS=4;

    DynamodbClient dc = new DynamodbClient();
    //String androidId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

    public String androidId = "";
    EditText targetname;
    EditText desc ;
    EditText fromname ;
    TextView theurl ;
    TextView errormsg;

    private class Reload5920 extends AsyncTask<String, Integer, Boolean> {
        DynamodbClient.TS5920Item tsItem ;

        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try{
                if(dc.existingUID(params[0])){
                    tsItem = dc.getItemByUid(params[0]);
                    return true;
                }
            }catch (Exception e){
                return false;
            }
            return false;
        }

        protected void onPostExecute(Boolean result) {
            if(result){
                targetname.setText(tsItem.getTarget());
                desc.setText(tsItem.getDesc());
                fromname.setText(tsItem.getFrom());
                setTargetUrl(tsItem.getTarget());

            }else{
                errormsg.setText(R.string.no_network);

            }
        }
    }


    private class Update5920 extends AsyncTask<String, Integer, Boolean> {
        DynamodbClient.TS5920Item tsItem ;

        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }

        @Override
        protected Boolean doInBackground(String... params) {

            String targetnameString = targetname.getText().toString().trim();
            String descString = desc.getText().toString();
            String fromnameString = fromname.getText().toString();
            dc.putItem(androidId, targetnameString, descString,fromnameString);

            return true;
        }

        protected void onPostExecute(Boolean result) {

            setTargetUrl(targetname.getText().toString());
        }
    }
    private class Check5920 extends AsyncTask<String, Integer, Integer> {
        // 1: the target name didn't existing in any place
        // 2: the target existed in other uid
        // 3: the target existed in this uid
        // 4: error

        DynamodbClient.TS5920Item tsItem ;

        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }


        @Override
        protected Integer doInBackground(String... params) {
            try{
                String targetNameParam = params[0];

                if(dc.existingTarget(targetNameParam)){
                    DynamodbClient.TS5920Item checkTargetItem = dc.getItemByTarget(targetNameParam);
                    tsItem = dc.getItemByUid(androidId);
                    if (tsItem.getUid().equals(checkTargetItem.getUid())){
                        return THIS_UID_HAS_TARGETNAME;
                    }else{
                        return OTHER_UID_HAS_TARGETNAME;
                    }

                }else{
                    return NO_SUCH_TARGETNAME;
                }
            }catch (Exception e){
                return UNKNOW_TARGET_STATUS;
            }


        }
        // 1: the target name didn't existing in any place
        // 2: the target existed in other uid
        // 3: the target existed in this uid
        // 4: error
        //static int NO_SUCH_TARGETNAME = 1;
        //static int OTHER_UID_HAS_TARGETNAME = 2;
        //static int THIS_UID_HAS_TARGETNAME =3;
        //static int UNKNOW_TARGET_STATUS=4;
        protected void onPostExecute(Integer result) {
            switch(result){
                case NO_SUCH_TARGETNAME :{
                    break;
                }
                case OTHER_UID_HAS_TARGETNAME:{
                    break;
                }
                case THIS_UID_HAS_TARGETNAME: {
                    setTargetUrl(tsItem.getTarget());
                    break;
                }
                case UNKNOW_TARGET_STATUS: {
                    break;
                }

            }


        }
    }


    private class IfOverwriteFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.dialog_overwrite)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // update to new
                            Update5920 update5920 = new Update5920();
                            update5920.execute(androidId, null, null);
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // reload to old
                            Reload5920 asyncUpdateLatest5920 = new Reload5920();
                            asyncUpdateLatest5920.execute(androidId, null, null);
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TelephonyManager tMgr = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        this.androidId = tMgr.getLine1Number();


        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        Button okButton = (Button)findViewById(R.id.ok);
        targetname = ((EditText)findViewById(R.id.edit_target));
        desc = ((EditText)findViewById(R.id.edit_desc));
        fromname = ((EditText)findViewById(R.id.edit_from));
        theurl = (TextView)findViewById(R.id.theurl);
        errormsg = (TextView)findViewById(R.id.errormsg);
        okButton.setOnClickListener(this);

        try {
            Reload5920 asyncUpdateLatest5920 = new Reload5920();
            asyncUpdateLatest5920.execute(this.androidId, null, null);

            if (asyncUpdateLatest5920.get() == null) {
                errormsg.setText(R.string.no_network);

            }
        }catch (Exception e){
            errormsg.setText(R.string.no_network);
        }

        return true;
    }
    public void setTargetUrl(String targetnameString){

        String targeturl = "http://"+targetnameString+".5920.space";
        //  theurl.setText(targeturl);
        theurl.setText(Html.fromHtml("<a href=\"" + targeturl + "\" >" + targeturl + " </a>"));
        theurl.setMovementMethod(LinkMovementMethod.getInstance());
    }

    // 1: the target name didn't existing in any place
    // 2: the target existed in other uid
    // 3: the target existed in this uid
    // 4: error
//    static int NO_SUCH_TARGETNAME = 1;
 //   static int OTHER_UID_HAS_TARGETNAME = 2;
 //   static int THIS_UID_HAS_TARGETNAME =3;
 //   static int UNKNOW_TARGET_STATUS=4;

    public void onClick(View v) {
        switch (v.getId()) {
            case  R.id.ok: {
                errormsg.setText("");
                String targetnameString = targetname.getText().toString();
                String descString = desc.getText().toString();
                String fromnameString = fromname.getText().toString();
                Check5920 check5920 = new Check5920();
                try {
                        Integer checkResult = check5920.execute(targetnameString.trim(), null, null).get();
                        switch (checkResult){

                            case NO_SUCH_TARGETNAME: {// target is never existing

                                FragmentTransaction ft = getFragmentManager().beginTransaction();
                                Fragment prev = getFragmentManager().findFragmentByTag("dialog");
                                if (prev != null) {
                                    ft.remove(prev);
                                }
                                ft.addToBackStack(null);

                                // Create and show the dialog.
                                IfOverwriteFragment ifOverwriteFragment;
                                ifOverwriteFragment = new IfOverwriteFragment();
                                ifOverwriteFragment.show(ft, "dialog");

                                break;
                            }
                            case OTHER_UID_HAS_TARGETNAME: {// target is the same as other uid?!
                                errormsg.setText(R.string.somebody_first);
                                Reload5920 asyncUpdateLatest5920 = new Reload5920();
                                asyncUpdateLatest5920.execute(this.androidId, null, null);
                                break;
                            }
                            case THIS_UID_HAS_TARGETNAME: { // target is the same in this uid only
                                Update5920 update5920 = new Update5920();
                                update5920.execute(this.androidId, null, null);

                                break;
                            }
                            case UNKNOW_TARGET_STATUS:{
                                errormsg.setText(R.string.something_wrong+"unknow");
                                break;
                            }
                        }




                }catch(Exception e ){
                    //alert!!
                    e.printStackTrace();
                    errormsg.setText(R.string.something_wrong+e.getMessage());
                }


                break;
            }
            default:
                break;
            //.... etc
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        // if (id == R.id.action_settings) {
        //    return true;
        //}

        return super.onOptionsItemSelected(item);
    }
}
