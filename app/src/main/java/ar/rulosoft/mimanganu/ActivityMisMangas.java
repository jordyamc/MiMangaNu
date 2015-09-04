package ar.rulosoft.mimanganu;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import ar.rulosoft.mimanganu.componentes.MoreMangasPageTransformer;
import ar.rulosoft.mimanganu.utils.ThemeColors;

public class ActivityMisMangas extends ActionBarActivity implements OnClickListener {

    public static final String SERVER_ID = "server_id";
    public static final String MANGA_ID = "manga_id";
    public static final String MOSTRAR_EN_GALERIA = "mostrarengaleria";
    public int[] colors;
    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    FragmentMisMangas fragmentMisMangas;
    FragmentAddManga fragmentAddManga;
    SharedPreferences pm;
    FloatingActionButton button_add;
    boolean darkTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        pm = PreferenceManager.getDefaultSharedPreferences(ActivityMisMangas.this);
        darkTheme = pm.getBoolean("dark_theme", false);
        if (darkTheme) {
            setTheme(R.style.AppBaseThemeDark);
        }
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.fragment_preferences, false);
        setContentView(R.layout.activity_mis_mangas);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        fragmentAddManga = new FragmentAddManga();
        fragmentMisMangas = new FragmentMisMangas();

        fragmentAddManga.setRetainInstance(true);
        mSectionsPagerAdapter.add(fragmentMisMangas);
        mSectionsPagerAdapter.add(fragmentAddManga);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setPageTransformer(false, new MoreMangasPageTransformer());

        button_add = (FloatingActionButton) findViewById(R.id.button_add);
        button_add.setOnClickListener(this);

        final boolean show_dialog = pm.getBoolean("show_updates", true);
        if (show_dialog) {//TODO ! o no segun la version 1.30 sin !
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
            dlgAlert.setMessage(getString(R.string.update_message));
            dlgAlert.setTitle(R.string.app_name);
            dlgAlert.setCancelable(true);
            dlgAlert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    pm.edit().putBoolean("show_updates", false).apply();
                }
            });
            dlgAlert.setNegativeButton(getString(R.string.see_later), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dlgAlert.create().show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mis_mangas, menu);
        MenuItem menuHideRead = menu.findItem(R.id.action_esconder_leidos);
        boolean checkedRead = pm.getInt(FragmentMisMangas.SELECTOR_MODO,
                FragmentMisMangas.MODO_ULTIMA_LECTURA_Y_NUEVOS) > 0;
        if (checkedRead)
            menuHideRead.setIcon(R.drawable.ic_action_selecionar_todos);
        menuHideRead.setChecked(checkedRead);
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.descargas: {
                Intent intent = new Intent(this, ActivityDownloads.class);
                startActivity(intent);
                break;
            }
            case R.id.licencia: {
                Intent intent = new Intent(this, ActivityLicenseView.class);
                startActivity(intent);
                break;
            }
            case R.id.action_esconder_leidos: {
                if (item.isChecked()) {
                    item.setChecked(false);
                    item.setIcon(R.drawable.ic_action_image_filter_none);
                    pm.edit().putInt(FragmentMisMangas.SELECTOR_MODO,
                            FragmentMisMangas.MODO_ULTIMA_LECTURA_Y_NUEVOS).apply();
                } else {
                    item.setChecked(true);
                    item.setIcon(R.drawable.ic_action_selecionar_todos);
                    pm.edit().putInt(FragmentMisMangas.SELECTOR_MODO,
                            FragmentMisMangas.MODO_SIN_LEER).apply();
                }
                try {
                    fragmentMisMangas.cargarMangas();
                } catch (Exception e) {
                    // TODO Handle cargar error properly
                    e.printStackTrace();
                }
                break;
            }
            case R.id.action_configurar: {
                startActivity(new Intent(ActivityMisMangas.this, OpcionesActivity.class));
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mViewPager.getCurrentItem() == 1) {
            onClick(button_add);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (darkTheme != pm.getBoolean("dark_theme", false)) {
            // re start to apply new theme
            Intent i = getPackageManager()
                    .getLaunchIntentForPackage(getPackageName());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            System.exit(0);
        }

        colors = ThemeColors.getColors(pm, getApplicationContext());
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(colors[0]));
        button_add.setColorNormal(colors[1]);
        button_add.setColorPressed(colors[3]);
        button_add.setColorRipple(colors[0]);
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        if (mViewPager.getCurrentItem() == 0) {
            ObjectAnimator anim =
                    ObjectAnimator.ofFloat(v, "rotation", 360.0f, 315.0f);
            anim.setDuration(200);
            anim.start();
            mViewPager.setCurrentItem(1);
        } else {
            ObjectAnimator anim =
                    ObjectAnimator.ofFloat(v, "rotation", 315.0f, 360.0f);
            anim.setDuration(200);
            anim.start();
            mViewPager.setCurrentItem(0);
        }
    }

//    public interface OnFinishTask {
//        void onFinish();
//    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        List<Fragment> fragments;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<>();
        }

        public void add(Fragment f) {
            fragments.add(f);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }
}
