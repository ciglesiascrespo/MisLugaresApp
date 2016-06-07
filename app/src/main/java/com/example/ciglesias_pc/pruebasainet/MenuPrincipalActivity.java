package com.example.ciglesias_pc.pruebasainet;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.ciglesias_pc.pruebasainet.fragments.FragmentCrearLugar;
import com.example.ciglesias_pc.pruebasainet.fragments.FragmentMisLugares;
import com.example.ciglesias_pc.pruebasainet.provider.LugaresConfiguracion;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;

/**
 * Created by Ciglesias-pc on 05/06/2016.
 */
public class MenuPrincipalActivity extends AppCompatActivity {

    TextView txtCorreo, txtTipoLogin;
    /**
     * Instancia del drawer
     */
    private DrawerLayout drawerLayout;
    Toolbar toolbar;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_menu_principal);
        setToolbar();
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
//        TextView textView = (TextView)navigationView.findViewById(R.id.id_txt_username);
        TextView textView = (TextView)navigationView.getHeaderView(0).findViewById(R.id.id_txt_username);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        textView.setText(LugaresConfiguracion.getCorreoUsuario(getApplicationContext()));
        selectItem(R.id.id_item_crear_lugar);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {

                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // Marcar item presionado
                        menuItem.setChecked(true);
                        // Crear nuevo fragmento
                        selectItem(menuItem.getItemId());
                        return true;
                    }
                }
        );
    }

    private void selectItem(int id) {
        // Enviar título como arguemento del fragmento
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch (id) {
            case R.id.id_item_crear_lugar:
                toolbar.setTitle("Crear lugar");
                Bundle args = new Bundle();


                FragmentCrearLugar fragmentCrearLugar = new FragmentCrearLugar();

                fragmentManager
                        .beginTransaction()
                        .replace(R.id.main_content, fragmentCrearLugar)
                        .commit();
                break;

            case R.id.id_item_mis_lugares:
                toolbar.setTitle("Mis lugares");
                FragmentMisLugares fragmentMisLugares = new FragmentMisLugares();
                fragmentManager
                        .beginTransaction()
                        .replace(R.id.main_content, fragmentMisLugares)
                        .commit();
                break;

            case R.id.id_item_salir:
                LugaresConfiguracion.updateLugaresConfiguracion(context, LugaresConfiguracion.LOGIN_INACTIVO, LugaresConfiguracion.LUGARES_CONFIGURACION_ID_LOGIN_ACTIVO);
                if(!FacebookSdk.isInitialized())FacebookSdk.sdkInitialize(getApplicationContext());
                LoginManager.getInstance().logOut();
                finish();
                startActivity(new Intent(MenuPrincipalActivity.this,MainActivity.class));
                break;


        }


        drawerLayout.closeDrawers(); // Cerrar drawer


    }

    private void setToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            // Poner ícono del drawer toggle
            ab.setHomeAsUpIndicator(R.drawable.ic_menu);
            ab.setDisplayHomeAsUpEnabled(true);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
            //   getMenuInflater().inflate(R.menu.main, menu);
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        context = this;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
