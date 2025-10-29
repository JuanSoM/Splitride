package com.example.consumocarros;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

public class PremainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_premain);

        ImageView logo = findViewById(R.id.imageView3);
        TextView text = findViewById(R.id.textView2);

        text.setTypeface(ResourcesCompat.getFont(this, R.font.lexend_giga_medium));

        // Cargar animaciones
        Animation logoAnim = AnimationUtils.loadAnimation(this, R.anim.logo_anim);
        Animation textAnim = AnimationUtils.loadAnimation(this, R.anim.text_fade_in);

        logoAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // Opcional: aquí podrías ocultar cosas o preparar algo
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // 🔹 Este código se ejecuta automáticamente cuando la animación termina
                Intent intent = new Intent(PremainActivity.this, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish(); // cerramos la splash para que no vuelva con "atrás"
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // No lo usamos, pero debe estar definido
            }
        });

        // Ejecutar animaciones
        logo.startAnimation(logoAnim);
        text.startAnimation(textAnim);



    }
}
