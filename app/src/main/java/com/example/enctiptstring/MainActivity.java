package com.example.enctiptstring;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity {

    private EditText editTextString, editTextPassword;

    private TextView textViewFinallyString, textViewSecretId, encryptedInfo;

    private Button encryptButton, decryptButton, copyString, copyKey, pasteString, pasteKey, deleteString, deleteKey;

    private String string, finallyString, secretId;

    private FirebaseDatabase firebaseDatabase;

    private ShareActionProvider shareActionProvider;

    String AES = "AES";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        firebaseDatabase = FirebaseDatabase.getInstance();

       editTextString= (EditText) findViewById(R.id.String);
        editTextPassword = (EditText) findViewById(R.id.secretIdPassowrd);

        textViewFinallyString = (TextView) findViewById(R.id.finallyString);
        textViewSecretId = (TextView) findViewById(R.id.secretId);
        encryptedInfo = (TextView) findViewById(R.id.encryptInfo);

        encryptButton = (Button) findViewById(R.id.encryptButton);
        decryptButton = (Button) findViewById(R.id.decryptButton);

        copyKey = (Button) findViewById(R.id.copyKey);
        copyString = (Button) findViewById(R.id.copyString);
        pasteString = (Button) findViewById(R.id.pasteString);
        pasteKey = (Button) findViewById(R.id.pasteKey) ;
        deleteString = (Button) findViewById(R.id.deleteString);
        deleteKey = (Button) findViewById(R.id.deleteKey);

        deleteString.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTextString.setText("");
            }
        });
        deleteKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTextPassword.setText("");
            }
        });
        
        pasteString.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PasteString();
            }
        });
        
        pasteKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PasteKey();
            }
        });
        
        copyKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CopyKey();
            }
        });

       copyString.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               CopyString();
           }
       });

        encryptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SaveInFirebase();
            }
        });

        decryptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {

                ReadFromFirebase();

                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Wrong Password", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_share) {

            String fbText1 = String.valueOf(textViewFinallyString.getText());
            String fbText2 = String.valueOf(textViewSecretId.getText());
            String shareBody = "Text: \n " + fbText1 + " \n " + "Key:  \n " + fbText2;

            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            startActivity(sharingIntent);


            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void PasteKey() {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        String clip = clipboardManager.getPrimaryClip().getItemAt(0).getText().toString();
        String lastText = editTextPassword.getText().toString();
        editTextPassword.setText(lastText  + clip);

    }

    private void PasteString() {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        String clip = clipboardManager.getPrimaryClip().getItemAt(0).getText().toString();
        String lastText = editTextString.getText().toString();
        editTextString.setText(lastText  + clip);
    }

    private void CopyKey() {

        try {
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("Converter Result",secretId);
            clipboardManager.setPrimaryClip(clipData);
        }
        catch (Exception e)
        {
            Toast.makeText(MainActivity.this, "Błąd kopiowania.", Toast.LENGTH_SHORT).show();
        }
        finally {
            Toast.makeText(MainActivity.this, "Skopiowano do schowka.", Toast.LENGTH_SHORT).show();
        }


    }

    private void CopyString() {

        try {
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("Converter Result",finallyString);
            clipboardManager.setPrimaryClip(clipData);
        }
        catch (Exception e)
        {
            Toast.makeText(MainActivity.this, "Błąd kopiowania.", Toast.LENGTH_SHORT).show();
        }
        finally {
            Toast.makeText(MainActivity.this, "Skopiowano do schowka.", Toast.LENGTH_SHORT).show();
        }


    }

    private String encrypt(String data, String password) throws Exception {
        SecretKeySpec key = generateKey(password);
        Cipher c = Cipher.getInstance(AES);
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = c.doFinal(data.getBytes());
        String encryptedValue = Base64.encodeToString(encVal, Base64.DEFAULT);
        return  encryptedValue;




    }

    private SecretKeySpec generateKey(String password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = password.getBytes("UTF-8");
        digest.update(bytes, 0, bytes.length);
        byte[] key = digest.digest();
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        return secretKeySpec;
    }


    private void SaveInFirebase()
    {
        try {

            string = editTextString.getText().toString();

            if (string.length()  <1)
            {
                Toast.makeText(MainActivity.this,"Wpisz słowo do zaszyfrowania", Toast.LENGTH_SHORT).show();
            }
            else {
                generateDecryptKey();

                finallyString = encrypt(string, secretId);

                Decrypt  decryptObject= new Decrypt(string, secretId, finallyString);
                firebaseDatabase.getReference().child(secretId).setValue(decryptObject);

                textViewSecretId.setText(secretId);
            }


        }
        catch (Exception e)
        {
            Toast.makeText(MainActivity.this, "Brak połączenia z serverem, spróbuj ponownie", Toast.LENGTH_SHORT).show();
        }
        finally {
            textViewFinallyString.setText(finallyString);
            encryptedInfo.setText(R.string.EncryptedInfo);
        }


    }

    private void ReadFromFirebase()
    {
        string = editTextString.getText().toString();
        secretId = editTextPassword.getText().toString();
        if (string.length() < 1)
        {
            Toast.makeText(MainActivity.this, "Nie wpisano zdania.", Toast.LENGTH_SHORT).show();
        }
        else if (secretId.length() < 1)
        {
            Toast.makeText(MainActivity.this, "Nie wprowadzono klucza", Toast.LENGTH_SHORT).show();
        }
        else
        {
            try {
                firebaseDatabase.getReference().child(secretId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                        if ((int) dataSnapshot.getChildrenCount() != 0) {

                            Decrypt decrypt = dataSnapshot.getValue(Decrypt.class);
                            finallyString = decrypt.stringOriginal;
                        }
                        else
                        {
                            Toast.makeText(MainActivity.this, "Nie wykryto klucza deszyfrującego w bazie.", Toast.LENGTH_LONG).show();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            } catch (Exception e) {

                Toast.makeText(MainActivity.this, "Błąd łączenia z bazą, sprawdź dostęp do internetu", Toast.LENGTH_SHORT).show();
            }
            finally {
                textViewFinallyString.setText(finallyString);
                textViewSecretId.setText("");
                encryptedInfo.setText(R.string.DecryptedInfo);
            }
        }



    }
    private void generateDecryptKey()
    {
        Date dateObj = new Date();
        dateObj.getTime();
        SimpleDateFormat postFormater = new SimpleDateFormat("MMMM dd, yyyy");

        String newDateStr = postFormater.format(dateObj);


        secretId =  newDateStr + "secret_id_";
        String[] SecretKey = new String[32];
        int number;
        Random random = new Random();

        for (int i = 0; i< SecretKey.length; i++ )
        {
            number = random.nextInt(10);
            SecretKey[i] = String.valueOf(number);
            secretId = secretId + SecretKey[i];
        }
    }
    


}