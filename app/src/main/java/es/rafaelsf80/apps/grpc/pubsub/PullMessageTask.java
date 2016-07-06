package es.rafaelsf80.apps.grpc.pubsub;

/**
 * Copyright 2016 Rafael Sanchez Fuentes
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * Author: Rafael Sanchez Fuentes rafaelsf80 at gmail dot com
 */

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.PullRequest;
import com.google.pubsub.v1.PullResponse;
import com.google.pubsub.v1.ReceivedMessage;
import com.google.pubsub.v1.SubscriberGrpc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.grpc.Channel;
import io.grpc.ClientInterceptors;
import io.grpc.auth.ClientAuthInterceptor;
import io.grpc.internal.ManagedChannelImpl;
import io.grpc.okhttp.OkHttpChannelBuilder;

public class PullMessageTask extends AsyncTask<String, Integer, Integer> {

    ProgressBar pb = null;
    private final String TAG = getClass().getSimpleName();

    Integer numMessages;
    private Context mContext;

    public void setContext(Context ctx) {
        mContext = ctx;
    }

    protected void onPreExecute()
    {
        // Show progressDialog
        pb = (ProgressBar) ((Activity) mContext).findViewById(R.id.progress_bar);
        pb.setVisibility(View.VISIBLE);
        Toast.makeText(mContext, "Pulling from subscription: " + Main.SUBSCRIPTION , Toast.LENGTH_LONG).show();
    }

    @Override
    protected Integer doInBackground(String... params) {

        try {
            Log.d(TAG, "Reading credentials file: " + Main.CREDENTIALS_FILE);
            AssetManager am = mContext.getAssets();
            InputStream isCredentialsFile = am.open( Main.CREDENTIALS_FILE );

            ManagedChannelImpl channelImpl = OkHttpChannelBuilder
                    .forAddress("pubsub.googleapis.com", 443)
                    .negotiationType(io.grpc.okhttp.NegotiationType.TLS)
                    .build();

            Log.d(TAG, "Channel terminated ?: " + channelImpl.isTerminated());

            GoogleCredentials credential = GoogleCredentials.fromStream(isCredentialsFile);
            credential = credential.createScoped(Arrays.asList("https://www.googleapis.com/auth/pubsub"));

            // Intercept the channel to bind the credential
            ExecutorService executor = Executors.newSingleThreadExecutor();
            ClientAuthInterceptor interceptor = new ClientAuthInterceptor(credential, executor);
            Log.d(TAG, "Interceptor: " + interceptor.toString());

            Channel channel = ClientInterceptors.intercept(channelImpl, interceptor);
            Log.d(TAG, "Channel authority: " + channel.authority());

             // Code to create new subscription, if required
//            Subscription subscription = Subscription.newBuilder()
//                    .setTopic("projects/YOUR_PROJECT_ID/topics/test1")
//                    .setName("projects/YOUR_PROJECT_ID/subscriptions/test2")
//                    .setAckDeadlineSeconds(10) // 10-100 seconds
//                    .build();
//            subscriberStub.createSubscription(subscription); // ignore Subscription result.

            // Pull (recover) message
            SubscriberGrpc.SubscriberBlockingStub subscriberStub = SubscriberGrpc.newBlockingStub(channel);

            PullRequest pullRequest = PullRequest.newBuilder()
                    .setSubscription( Main.SUBSCRIPTION )
                    .setReturnImmediately( true )
                    .setMaxMessages(10)
                    .build();

            PullResponse pullResponse = subscriberStub.pull(pullRequest);
            numMessages = pullResponse.getReceivedMessagesCount();
            Log.d(TAG, "Number of messages received: " + String.valueOf(numMessages));
            for (ReceivedMessage message1 : pullResponse.getReceivedMessagesList()) {
                PubsubMessage pubsubMessage1 = message1.getMessage();
                // Payload.
                byte[] elementBytes = pubsubMessage1.getData().toByteArray();
                Log.d(TAG, "Message received (ID="+message1.getAckId()+"): " + new String(elementBytes));
            }

        } catch (IOException e) {
            Log.d(TAG, "Exception: " + e.toString());
        }
        return numMessages;
    }

    @Override
    protected void onProgressUpdate(final Integer... progress) {
        // DO nothing
    }

    @Override
    protected void onPostExecute(Integer num)
    {
        pb.setVisibility(View.GONE);
        Toast.makeText(mContext, "Number of messages received: " + String.valueOf(num) , Toast.LENGTH_LONG).show();
    }
}
