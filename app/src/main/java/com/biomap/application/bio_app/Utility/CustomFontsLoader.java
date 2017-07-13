package com.biomap.application.bio_app.Utility;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class CustomFontsLoader {

    public static final int GOTHAM_MEDIUM = 0;
    public static final int GOTHAM_BOLD = 1;
    public static final int GOTHAM_BOOK = 2;
    public static final int GOTHAM_MEDIUM_ITALIC = 3;
    public static final int GOTHAM_BOLD_ITALIC = 4;
    public static final int GOTHAM_BOOK_ITALIC = 5;

    private static final int NUM_OF_CUSTOM_FONTS = 3;

    private static boolean fontsLoaded = false;

    private static Typeface[] fonts = new Typeface[3];

    private static String[] fontPath = {
            "fonts/Gotham-Medium.otf",
            "fonts/Gotham-Bold.otf",
            "fonts/Gotham-Book.otf",
            "fonts/Gotham-MediumItalic.otf",
            "fonts/Gotham-BoldItalic.otf",
            "fonts/Gotham-BookItalic.otf",
    };


    /**
     * Returns a loaded custom font based on it's identifier.
     *
     * @param context        - the current context
     * @param fontIdentifier = the identifier of the requested font
     * @return Typeface object of the requested font.
     */
    public static Typeface getTypeface(Context context, int fontIdentifier) {
        if (!fontsLoaded) {
            loadFonts(context);
        }

        return fonts[fontIdentifier];
    }


    private static void loadFonts(Context context) {
        for (int i = 0; i < NUM_OF_CUSTOM_FONTS; i++) {
            fonts[i] = Typeface.createFromAsset(context.getAssets(), fontPath[i]);
        }

        fontsLoaded = true;
    }
    public static void overrideFonts(final Context context, final View v, int font) {
        try {
            if (v instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) v;
                for (int i = 0; i < vg.getChildCount(); i++) {
                    View child = vg.getChildAt(i);
                    overrideFonts(context, child, font);
                }
            } else if (v instanceof TextView) {
                ((TextView) v).setTypeface(CustomFontsLoader.getTypeface(context, font));
            }
        } catch (Exception e) {
        }
    }
}