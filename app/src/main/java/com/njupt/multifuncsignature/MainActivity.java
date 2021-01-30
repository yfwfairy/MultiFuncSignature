package com.njupt.multifuncsignature;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.njupt.multifuncsignature.signature.KeyManager;
import com.njupt.multifuncsignature.signature.RsaEncrypt;
import com.njupt.multifuncsignature.util.ContentUriUtil;
import com.njupt.multifuncsignature.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MAIN_TAG";
    String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int PERMISSION_REQUEST = 1;

    List<String> mPermissionList = new ArrayList<>();
    private Spinner privKeySpinner;
    private Button keyManagerBtn;
    private ArrayAdapter<String> privKeyAdapter;
    private TextView sourceFileTxv;
    private Button selectSrcFileBtn;
    private EditText signatureFileNameEdt;
    private Button genSigBtn;
    private EditText signatureNewFileNameEdt;
    private Button genSigAppendBtn;
    private RadioGroup abstractRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkPermission();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initPrivSpinnerAdapter();
    }

    private void initView() {
        keyManagerBtn = findViewById(R.id.key_manager_btn);
        privKeySpinner = findViewById(R.id.prikey_spi);
        sourceFileTxv = findViewById(R.id.srcfile_txv);
        selectSrcFileBtn = findViewById(R.id.srcfile_select_btn);
        signatureFileNameEdt = findViewById(R.id.sign_name_edt);
        abstractRadioGroup = findViewById(R.id.abstract_type_radiogroup);
        genSigBtn = findViewById(R.id.signfile_generate_btn);
        signatureNewFileNameEdt = findViewById(R.id.sign_newfilename_edt);
        genSigAppendBtn = findViewById(R.id.sign_newfile_generate_btn);
        initPrivSpinnerAdapter();
        keyManagerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, KeymanagerActivity.class);
                startActivity(intent);
            }
        });
        selectSrcFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSystemFile();
            }
        });
        genSigBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertPasswardDialog(0);
            }
        });
        genSigAppendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertPasswardDialog(1);
            }
        });
    }


    public void alertPasswardDialog(final int type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请输入私钥密码");
        final EditText edit = new EditText(this);
        edit.setHeight(150);
        edit.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        edit.setTransformationMethod(PasswordTransformationMethod.getInstance());
        edit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
        edit.setHint("8位密码");
        builder.setView(edit);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String password = edit.getText().toString();
                if (password.length() != 8) {
                    Toast.makeText(MainActivity.this, "invalid passward", Toast.LENGTH_SHORT).show();
                    alertPasswardDialog(type);
                } else {
                    if (type == 0) {
                        //生成新签名文件
                        genNewDigitalSignature(password);
                    } else if (type == 1) {
                        //追加在源文件后面
                        appendDigitalSignatureToFile(password);
                    }
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

            }
        });
        Dialog dialog = builder.create();
        dialog.show();
        Button btnPos = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
        Button btnNeg = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
        btnPos.setTextColor(Color.BLUE);
        btnNeg.setTextColor(Color.BLUE);
    }

    private byte[] genDigitalSignatureBytes(String passward) {
        String privKeyName = privKeySpinner.getSelectedItem().toString();
        String srcFileName = sourceFileTxv.getText().toString();
        RsaEncrypt.DigestType digestType;
        switch (abstractRadioGroup.getCheckedRadioButtonId()) {
            case R.id.md5_rb:
                digestType = RsaEncrypt.DigestType.MD5;
                break;
            case R.id.sha1_rb:
                digestType = RsaEncrypt.DigestType.SHA1;
                break;
            default:
                digestType = RsaEncrypt.DigestType.MD5;
        }
        Key privKey = null;
        try {
            privKey = KeyManager.getInstance(this).importKeyFromFile(privKeyName, passward);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "password invalid, generate failed", Toast.LENGTH_SHORT).show();
            return null;
        }
        if (privKey == null) {
            Toast.makeText(this, "privKey null", Toast.LENGTH_SHORT).show();
            return null;
        }
        byte[] data = null;
        try {
            data = IOUtils.readFileBytes(new File(srcFileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (data == null) {
            Toast.makeText(this, "src file null", Toast.LENGTH_SHORT).show();
            return null;
        }
        return RsaEncrypt.rsaSign(data, (RSAPrivateKey) privKey, digestType);
    }

    private void genNewDigitalSignature(String passward) {
        String signatureName = (!TextUtils.isEmpty(signatureFileNameEdt.getText())) ? signatureFileNameEdt.getText().toString() : "ds_" + SystemClock.uptimeMillis();
        byte[] dsBytes = genDigitalSignatureBytes(passward);
        if (dsBytes == null) {
            return;
        }
        String sigFileName = KeyManager.getInstance(this).KEY_STORE_PATH + signatureName + ".dsg";
        File signFile = new File(sigFileName);
        try {
            IOUtils.writeFileBytes(dsBytes, signFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "generate digital signature success! " + sigFileName, Toast.LENGTH_SHORT).show();
    }

    private void appendDigitalSignatureToFile(String passward) {
        if (sourceFileTxv.getText() == null) {
            Toast.makeText(this, "source file can't be null! ", Toast.LENGTH_SHORT).show();
            return;
        }
        String srcFileName = sourceFileTxv.getText().toString();
        File file = new File(srcFileName);
        if (file == null) {
            Toast.makeText(this, "source file can't be null! ", Toast.LENGTH_SHORT).show();
            return;
        }
        String fileName = file.getName();
        String newFileName = KeyManager.getInstance(this).KEY_STORE_PATH + (!TextUtils.isEmpty(signatureNewFileNameEdt.getText()) ? signatureNewFileNameEdt.getText().toString() + ".apd" : "appendDS_" + fileName + ".apd");
        byte[] dsBytes = genDigitalSignatureBytes(passward);
        if (dsBytes == null) {
            return;
        }
        try {
            byte[] totalBytes = IOUtils.readFileBytesAndAppend(file, dsBytes);
            File signFile = new File(newFileName);
            IOUtils.writeFileBytes(totalBytes, signFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "append digital signature success! " + newFileName, Toast.LENGTH_SHORT).show();
    }

    private void openSystemFile(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,false);//打开多个文件
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try{
            startActivityForResult(Intent.createChooser(intent,"请选择文件"),1);
        }catch (ActivityNotFoundException e){
            e.printStackTrace();
            Toast.makeText(this,"请安装文件管理器",Toast.LENGTH_SHORT);
        }
    }

    private void initPrivSpinnerAdapter() {
        String[] m = KeyManager.getInstance(this).getPrivkeysNameArray();
        if (m == null) {
            return;
        }
        privKeyAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, m);
        privKeySpinner.setAdapter(privKeyAdapter);
    }



    // 检查权限
    private void checkPermission() {
        mPermissionList.clear();
        //判断哪些权限未授予
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);
            }
        }
        /**
         * 判断是否为空
         */
        if (mPermissionList.isEmpty()) {//未授予的权限为空，表示都授予了

        } else {//请求权限方法
            String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);//将List转为数组
            ActivityCompat.requestPermissions(MainActivity.this, permissions, PERMISSION_REQUEST);
        }
    }

    /**
     * 响应授权
     * 这里不管用户是否拒绝，都进入首页，不再重复申请权限
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST:
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            if(data.getData() != null) {
                String path = ContentUriUtil.getPath(this,data.getData());
                Log.i(TAG,"Single image path ---- "+ path);
                sourceFileTxv.setText(path);
            }

        }
    }


}
