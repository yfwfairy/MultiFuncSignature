package com.njupt.multifuncsignature;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.njupt.multifuncsignature.signature.KeyManager;

import java.security.KeyPair;

public class KeymanagerActivity extends AppCompatActivity {
    private EditText keyNameEdt;
    private EditText keyPasswardEdt;
    private Button keyCreateBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keymanager);
        initUI();
    }

    private void initUI() {
        keyNameEdt = findViewById(R.id.key_name_edt);
        keyPasswardEdt = findViewById(R.id.key_passward_edt);
        keyCreateBtn = findViewById(R.id.key_create_btn);
        keyCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(keyNameEdt.getText())) {
                    Toast.makeText(KeymanagerActivity.this,"keyName can't be empty!",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(keyPasswardEdt.getText())) {
                    Toast.makeText(KeymanagerActivity.this,"keyPwd can't be empty!",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (keyPasswardEdt.getText().length() != 8) {
                    Toast.makeText(KeymanagerActivity.this,"keyPwd length can't be less than 8!",Toast.LENGTH_SHORT).show();
                    return;
                }
                String privKeyName = keyNameEdt.getText() + KeyManager.PRIV_POSTFIX;
                String pubKeyName = keyNameEdt.getText() + KeyManager.PUB_POSTFIX;
                String passward = keyPasswardEdt.getText().toString();
                KeyPair keyPair = KeyManager.getInstance(KeymanagerActivity.this).generateRSAKeyPair();
                try {
                    KeyManager.getInstance(KeymanagerActivity.this).exportKeyToFile(keyPair.getPrivate(), passward, privKeyName);
                    KeyManager.getInstance(KeymanagerActivity.this).exportKeyToFile(keyPair.getPublic(), passward, pubKeyName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Toast.makeText(KeymanagerActivity.this,"create success! privKeyName: " + privKeyName + " pubKeyName: " + pubKeyName ,Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
