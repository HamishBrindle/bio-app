package com.biomap.application.bio_app.Utility;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.biomap.application.bio_app.R;

public class CustomFontUtils {

    public static final String ANDROID_SCHEMA = "http://schemas.android.com/apk/res/android";

    public static final int BOLD = 0;
    public static final int BOLD_ITALIC = 1;
    public static final int BOOK = 2;
    public static final int BOOK_ITALIC = 3;
    public static final int LIGHT = 4;
    public static final int LIGHT_ITALIC = 5;
    public static final int REGULAR = 6;
    public static final int REGULAR_ITALIC = 7;

    public static void applyCustomFont(TextView customFontTextView, Context context, AttributeSet attrs) {
        TypedArray attributeArray = context.obtainStyledAttributes(
                attrs,
                R.styleable.CustomFontTextView);

        String fontName = attributeArray.getString(R.styleable.CustomFontTextView_font);

        // check if a special textStyle was used (e.g. extra bold)
        int textStyle = attributeArray.getInt(R.styleable.CustomFontTextView_textStyle, 0);

        // if nothing extra was used, fall back to regular android:textStyle parameter
        if (textStyle == 0) {
            textStyle = attrs.getAttributeIntValue(ANDROID_SCHEMA, "textStyle", Typeface.NORMAL);
        }

        Typeface customFont = selectTypeface(context, fontName, textStyle);
        customFontTextView.setTypeface(customFont);

        attributeArray.recycle();
    }

    private static Typeface selectTypeface(Context context, String fontName, int textStyle) {

        if (fontName.contentEquals(context.getString(R.string.font_name_gotham))) {

            switch (textStyle) {
                case BOLD: // bold
                    return FontCache.getTypeface("fonts/Gotham-Bold.otf", context);

                case BOLD_ITALIC: // italic
                    return FontCache.getTypeface("fonts/Gotham-BoldItalic.otf", context);

                case BOOK_ITALIC: // bold italic
                    return FontCache.getTypeface("fonts/Gotham-BookItalic.otf", context);

                case LIGHT: // bold italic
                    return FontCache.getTypeface("fonts/Gotham-Light.otf", context);

                case LIGHT_ITALIC: // bold italic
                    return FontCache.getTypeface("fonts/Gotham-LightItalic.otf", context);

                case REGULAR: // bold italic
                    return FontCache.getTypeface("fonts/Gotham-Medium.otf", context);

                case REGULAR_ITALIC: // bold italic
                    return FontCache.getTypeface("fonts/Gotham-MediumItalic.otf", context);

                default:
                    return FontCache.getTypeface("fonts/Gotham-Book.otf", context);
            }
        } else {
            return null;
        }
    }
}