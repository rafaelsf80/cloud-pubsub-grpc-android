package es.rafaelsf80.apps.grpc.pubsub;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class Main extends AppCompatActivity {

    private String TAG = getClass().getSimpleName();

    // Constants shared across classes
    public static final String TOPIC ="projects/decent-envoy-503/topics/test1";
    public static final String SUBSCRIPTION ="projects/decent-envoy-503/subscriptions/test1";
    public static final String CLOUD_PROJECT = "decent-envoy-503";
    // assets directory, replace with the json file of your cloud project
    public static final String CREDENTIALS_FILE = "doneval-cloud-d164a2981f94.json";


    TextView tvTitle, tvTopic, tvSubscription;
    Button btPublish, btSubscribe;

   // What happens if I publish to a topic without subscriptions?
   // The publish operation succeeds, but the messages are dropped because there is no subscription interested in them.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        tvTitle = (TextView) findViewById(R.id.tv_title);
        tvTopic = (TextView) findViewById(R.id.tv_topic);
        tvSubscription = (TextView) findViewById(R.id.tv_subscription);
        btPublish = (Button) findViewById(R.id.bt_send);
        btSubscribe = (Button) findViewById(R.id.bt_receive);

        tvTopic.setText( TOPIC );
        tvSubscription.setText( SUBSCRIPTION );

        btPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder alert = new AlertDialog.Builder(Main.this);

                final EditText edittext = new EditText(getApplicationContext());
                alert.setTitle("Message to publish");
                alert.setMessage("To topic: " + TOPIC);
                edittext.setTextColor( Color.BLACK );
                alert.setView(edittext);

                alert.setPositiveButton("Publish", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        PublishMessageTask publishMessageTask = new PublishMessageTask();

                        publishMessageTask.setContext( Main.this );
                        publishMessageTask.execute( edittext.getText().toString() );
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // DO nothing
                    }
                });
                alert.show();
            }
        });

        btSubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PullMessageTask pullMessageTask = new PullMessageTask();

                pullMessageTask.setContext( Main.this );
                pullMessageTask.execute( );
            }
        });
    }
}

