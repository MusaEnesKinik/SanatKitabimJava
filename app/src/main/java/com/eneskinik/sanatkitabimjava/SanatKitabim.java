package com.eneskinik.sanatkitabimjava;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.eneskinik.sanatkitabimjava.databinding.ActivitySanatKitabimBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;

public class SanatKitabim extends AppCompatActivity {

    private ActivitySanatKitabimBinding binding; // binding yaptık

    Bitmap secilenGorsel;

    SQLiteDatabase veriTabani;

    ActivityResultLauncher<Intent> activityResultLauncher; //galeriden fotoğraf almak için ıntent ettiğimiz için bu satırı kullandık
    ActivityResultLauncher<String> permissionLauncher; //izinler string ile uğraştığı için, izin istemek amacı
    /*Activity sunucu başlatıcı demek, yeni bir activity açıp, galeriye gidip bir şey almak istediğimizde, görsel seçince ne olacağını
    yazmak istediğimizde, izin isteyip izin verildiğinde ne olacağını yazmak istediğimizde (ActivityResultLauncher) hepsinde bunu kullanmalıyız */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_sanat_kitabim); // binding yaptığımız için bu satırı silip bir alt satırdaki kodu yazıyoruz
        binding = ActivitySanatKitabimBinding.inflate(getLayoutInflater()); // getLayoutInflater i verdik
        View view = binding.getRoot(); // görünüm oluşturduk
        setContentView(view); // oluşturduğumuz görünümü buraya verdik

        registerLauncher();

        veriTabani = this.openOrCreateDatabase("Sanatlar", MODE_PRIVATE,null);

        Intent intent = new Intent();
        String info = intent.getStringExtra("info");

        if (info.equals("yeni bir şey gönderilecek")) {
            //yeni resim ekle
            binding.isimText.setText(""); //ismin boş olduğundan emin olduk
            binding.sanatciText.setText(""); //sanatçının boş olduğundan emin olduk
            binding.yilText.setText(""); //yılın boş olduğundan emin olduk
            binding.imageView.setImageResource(R.drawable.sec);
            binding.button.setVisibility(View.VISIBLE); // yeni bir şey kaydedilecekse göster
        } else {
            int resimId = intent.getIntExtra("resimId",0);
            binding.button.setVisibility(View.INVISIBLE); // eski bir şey varsa kaydetmeye gerek yok butonu gizle

            try {

                Cursor cursor = veriTabani.rawQuery("SELECT * FROM sanatlar WHERE id = ?",new String[] {String.valueOf(resimId)});
                int sanatismiIx = cursor.getColumnIndex("resim adı");
                int sanatciismiIx = cursor.getColumnIndex("sanatçı adı");
                int yilIx = cursor.getColumnIndex("yapıldığı yıl");
                int resimIx = cursor.getColumnIndex("resim");

                while (cursor.moveToNext()) {
                    binding.isimText.setText(cursor.getString(sanatismiIx));
                    binding.sanatciText.setText(cursor.getString(sanatciismiIx));
                    binding.yilText.setText(cursor.getString(yilIx));

                    byte[] bytes = cursor.getBlob(resimIx);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length); //byte dizisi imageview a koyulmadığı için bitmap e çevrildi
                    binding.imageView.setImageBitmap(bitmap); //bitmap i imageview a verdik
                }

                cursor.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    public void kaydet(View v){

        String isim = binding.isimText.getText().toString();
        String sanatci = binding.sanatciText.getText().toString();
        String yil = binding.yilText.getText().toString();

        Bitmap kucukResim = kucukGorselYap(secilenGorsel,300);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        kucukResim.compress(Bitmap.CompressFormat.PNG,50,outputStream);
        byte[] byteArray = outputStream.toByteArray();

        try {


            veriTabani.execSQL("CREATE TABLE IF NOT EXISTS sanatlar (id INTEGER PRIMARY KEY, sanatismi VARCHAR, sanatciismi VARCHAR, yil VARCHAR, resim BLOB)");

            //veriTabani.execSQL("INSERT INTO sanatlar (sanatismi, sanatciismi, yil, resim) VALUES()");
            String sqlString = "INSERT INTO sanatlar (sanatismi, sanatciismi, yil, resim) VALUES(?, ?, ?, ?)";
            SQLiteStatement sqLiteStatement = veriTabani.compileStatement(sqlString);
            sqLiteStatement.bindString(1,isim);
            sqLiteStatement.bindString(2,sanatci);
            sqLiteStatement.bindString(3,yil);
            sqLiteStatement.bindBlob(4,byteArray);
            sqLiteStatement.execute();


        } catch (Exception e) {
            e.printStackTrace();
        }

        //1. yöntem
        //finish(); // burası kapanır mainactivity ye geri döner

        //2. yöntem
        Intent intent = new Intent(SanatKitabim.this,MainActivity.class); //SanatKitabım Activity den MainActivity ye intent ettik
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //İçinde bulunduğum dahil bundan önceki activity leri kapat şu an gideceğimi aç
        startActivity(intent); //MainActivity yi çalıştırır

    }

    public Bitmap kucukGorselYap(Bitmap gorsel, int maximumSize) {
        int width = gorsel.getWidth();
        int height = gorsel.getHeight();
        
        float bitmapOrani = (float) width / (float) height;
        
        if (bitmapOrani > 1) {
            //yatay görsel
            width = maximumSize;
            height = (int) (width / bitmapOrani);
        } else {
            //dikey görsel
            height = maximumSize;
            width = (int) (height * bitmapOrani);
        }
        return gorsel.createScaledBitmap(gorsel,width,height,true);
    }

    public void fotografSec(View view){  // uygulama telefon galerisinden fotoğraf seçmek için bu medhodu kullanır
        // if in solunda 'READ_EXTERNAL_STORAGE' izni var mı onu konrol ediyor
        // 'PackageManager'ın içinde izin verilmiş olan 'PERMISSION_GRANTED' var, yani izin verilmemiş mi onu kontrol ediyor if içinde

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // izin isteme mantığını kullanıcıya göstereyim mi    //
                                                                                                     // kullanıcı izin ver e basana kadar mesaj görünsün
                Snackbar.make(view,"Bu Uygulama Galerinizdeki Fotoğraflara Erişim İzni İstiyor",Snackbar.LENGTH_INDEFINITE).setAction("İzin Ver", new View.OnClickListener() {
                    @Override                                                                                                  //Burada izin ver butonuna basarak izin gösteriyor
                    public void onClick(View v) {  //request permiision (izin istemek demek) galeri için izin verilmediyse kullanıcıdan izin istenecek
                        //request permiision (izin istemek demek)
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);

                    }
                }).show(); // Snackbar ın görünmesi için


            } else {
                //request permiision (izin istemek demek)
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }


        } else {  // else e giriyorsa izin verilmiş ve galeriye gidecek fotoğraf seçmek için
            // Galeriye gidicem ordan bir gösel alacağım ve geri geleceğim
            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            //eğer izin verilmişse önceki iki ife girmiyor burdaki kod ile galeriye gidebiliyor

            activityResultLauncher.launch(intentToGallery);

        }

    }

    private void registerLauncher() {

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {  // kullanıcı galeriye gittiğinde fotoğraf seçtiğinde ve seçmediğinde yapılacak şeyler yazılacak
                if (result.getResultCode() == RESULT_OK) {  // burası ok ise kullanıcı galeriden bir şey seçmiş demektir
                    Intent intentCvptnGln = result.getData();
                    if (intentCvptnGln != null) {
                       Uri imageData = intentCvptnGln.getData(); // kullanıcının seçtiği görselin nerede olduğunu biliyoruz
                        //binding.imageView.setImageURI(imageData);

                        try { // burada yazılanları dene eğer hata çıkarsa catch içinde yakala, fotoğraf sd kartta olabili kart okunmayabilir bu gibi sorunlarda uygulama hata vermesin diye

                            if (Build.VERSION.SDK_INT >= 28) {

                                ImageDecoder.Source source = ImageDecoder.createSource(SanatKitabim.this.getContentResolver(),imageData);
                                secilenGorsel = ImageDecoder.decodeBitmap(source);
                                binding.imageView.setImageBitmap(secilenGorsel); // kullanıcının seçtiği görseli Bitmap e çevirip kullanıcıya gösterdik

                            } else {
                                secilenGorsel = MediaStore.Images.Media.getBitmap(SanatKitabim.this.getContentResolver(),imageData);
                                binding.imageView.setImageBitmap(secilenGorsel);
                            }



                        } catch (Exception e) {
                            e.printStackTrace(); // hata çıktığında bu mesajı Logcat de gösterir, catch içerisinde kullanıcıya da mesaj verdirilebilir

                        }

                    }
                }
            }
        });

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) { // eğer sonuç doğruysa izin verildi demek

                if (result) {  // izin verildi
                    Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI); // izin aldığında galeriye gider
                    activityResultLauncher.launch(intentToGallery);

                } else {  // izin verilmedi
                    Toast.makeText(SanatKitabim.this, "İzin Vermeniz Gerekli!", Toast.LENGTH_LONG).show();
                    // galeri için eğer izin verilmezse tekrar izin isteyecek
                }

            }
        });

    }


}