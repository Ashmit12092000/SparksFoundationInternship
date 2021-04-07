package com.example.login;

import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.facebook.*;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "24";
    private CallbackManager mcallbackManager;
    private ProfilePictureView profilePictureView;
    private LinearLayout infoLayout;
    private static final int RC_SIGN_IN = 1;
    private TextView email;
    private TextView gender;
    private GoogleApiClient googleApiClient;
    private TextView facebookName;
    GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    Button g_login;
    ProgressBar pbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_main);

        LoginButton btnLogin = (LoginButton) findViewById(R.id.login_button);
        email = (TextView) findViewById(R.id.txtEmail);
        facebookName = (TextView) findViewById(R.id.txtUsername);
        g_login=findViewById(R.id.g_button);
        mAuth=FirebaseAuth.getInstance();
       
        pbar=findViewById(R.id.pbar);
        profilePictureView = (ProfilePictureView) findViewById(R.id.image);
        infoLayout = findViewById(R.id.infolayout);

        btnLogin.setReadPermissions(Arrays.asList("public_profile, email, user_birthday"));
        mcallbackManager = CallbackManager.Factory.create();

  
        btnLogin.registerCallback(mcallbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {

                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.v("Main", response.toString());
                                setProfileToView(object);
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email, birthday");
                request.setParameters(parameters);
                request.executeAsync();

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException exception) {
                Toast.makeText(MainActivity.this, "error to Login Facebook", Toast.LENGTH_SHORT).show();
            }
        });
        AccessTokenTracker tokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if (currentAccessToken == null) {
                    infoLayout.setVisibility(View.GONE);
                    g_login.setVisibility(View.VISIBLE);
                }
            }
        };
        tokenTracker.startTracking();


        GoogleSignInOptions gso =  new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        g_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });


    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            pbar.setVisibility(View.VISIBLE);

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
            }
        }
        else{
            mcallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            pbar.setVisibility(View.GONE);
                            startActivity(new Intent(MainActivity.this,userprofile.class));
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                          
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }

                });
    }



    private void setProfileToView(JSONObject jsonObject) {
        try {
            email.setText(jsonObject.getString("email"));
            facebookName.setText(jsonObject.getString("name"));
            profilePictureView.setPresetSize(ProfilePictureView.NORMAL);
            profilePictureView.setProfileId(jsonObject.getString("id"));
            infoLayout.setVisibility(View.VISIBLE);
            g_login.setVisibility(View.GONE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onStart() {
        super.onStart();
      
        FirebaseUser currentUser = mAuth.getCurrentUser();
       currentUser.reload();
    }
}
