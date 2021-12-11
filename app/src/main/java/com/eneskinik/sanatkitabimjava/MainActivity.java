package com.eneskinik.sanatkitabimjava;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.eneskinik.sanatkitabimjava.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding; // binding yaptık

    ArrayList<Resim> resimArrayList;

    ResimAdapter resimAdapter; // resimadapter i burada çağırdık

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main); // binding yaptığımız için bu satırı silip bir alt satırdaki kodu yazıyoruz
        binding = ActivityMainBinding.inflate(getLayoutInflater()); // getLayoutInflater i verdik
        View view = binding.getRoot(); // görünüm oluşturduk
        setContentView(view); // oluşturduğumuz görünümü buraya verdik

        resimArrayList = new ArrayList<>();

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        resimAdapter = new ResimAdapter(resimArrayList); // resimArrayList i resimAdapter a verdik
        binding.recyclerView.setAdapter(resimAdapter); // recyclerView ı resimAdapter a bağladık

        verileriAl();
    }

    private void verileriAl(){

        try {

            SQLiteDatabase sqLiteDatabase = this.openOrCreateDatabase("Sanatlar", MODE_PRIVATE, null);
           // sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS sanatlar (id INTEGER PRIMARY KEY, sanatismi VARCHAR, sanatciismi VARCHAR, yil VARCHAR, resim BLOB)");

            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM sanatlar", null);
            int isimIx = cursor.getColumnIndex("sanatismi");
            int idIx = cursor.getColumnIndex("id");

            while (cursor.moveToNext()) {

                String name = cursor.getString(isimIx);
                int id = cursor.getInt(idIx);
                Resim resim = new Resim(name,id);
                resimArrayList.add(resim);

            }

            resimAdapter.notifyDataSetChanged();//veri setinin değiştiğini bildirdik, yazmazsak veriler görünmeyecektir (resimAdapter e verinin geldiğini ve kendisini güncellemesini belirttik)
            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //mainactivity içerisinde menu' yü buraya bağlamak için iki tane method oluşturuyoruz (override ediyoruz)

    @Override  //bunun içinde oluşturduğumuz menu yü koda bağlayacaz
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();  // uygulama içerisinde kullanılan menuInflater döndürüldü
        menuInflater.inflate(R.menu.sanat_menu, menu);  // R.menu.sanat_menu diyerek hangi menüyü bağlamak istediğimizi yazdık, sonra hangi menü ile bağlanacağını yazdık 'onCreateOptionsMenu(Menu menu)' burada menu olarak bize veriliyor

        return super.onCreateOptionsMenu(menu);
    }

    @Override  // opsiyon menüsünden bir şey seçildiğinde ne olacağı bu methoda yazılıyor
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.sanat_ekle) {
            Intent intent = new Intent(this,SanatKitabim.class); //MainActivity den SanatKitabım Activity sınıfına gitmek için bunu yazdık
            intent.putExtra("info","yeni bir şey eklenecek");
            startActivity(intent);
        } //birden fazla id olsaydı 'else if (item.getItemId() == R.id.xx)' gibi yapılacaktı

        return super.onOptionsItemSelected(item);
    }


}