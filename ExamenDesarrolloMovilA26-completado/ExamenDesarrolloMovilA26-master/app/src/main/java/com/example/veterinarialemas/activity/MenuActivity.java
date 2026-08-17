package com.example.veterinarialemas.activity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.veterinarialemas.R;
import com.example.veterinarialemas.fragments.BarberFragment;
import com.example.veterinarialemas.fragments.FoodFragment;
import com.example.veterinarialemas.fragments.MedicalAttentionFragment;
import com.google.android.material.navigation.NavigationView;

public class MenuActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Toolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView navigationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        //Configurar toolbar
        configurarToolBar();
        //Configurar drawerlayout
        configurarDrawerLayout();
        //Configurar navigationView
        configurarNavigationView();
        //Cargar fragmento inicial
        loadFragment(new MedicalAttentionFragment());
        navigationView.setCheckedItem(R.id.nav_medic_assistant);
    }

    private void configurarNavigationView() {
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void configurarDrawerLayout() {
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toogle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toogle);
        toogle.syncState();
    }

    private void configurarToolBar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Fragment fragment = obtenerFragmentPorItemId(menuItem.getItemId());
        if (fragment != null) {
            loadFragment(fragment);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    public Fragment obtenerFragmentPorItemId(int itemId) {
        if (itemId == R.id.nav_medic_assistant) {
            return new MedicalAttentionFragment();
        } else if (itemId == R.id.nav_barber) {
            return new BarberFragment();
        } else if (itemId == R.id.nav_food) {
            return new FoodFragment();
        }
        return null;
    }
}
