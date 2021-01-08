package com.app.memoeslink.notemanagement;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.Html;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

public class Methods {
    private static final int[] UNICODE_HEX = {0x1F479, 0x1F47A, 0x1F480, 0x1F47B, 0x1F47D, 0x1F47E, 0x1F916, 0x1F63A, 0x1F921, 0x1F925, 0x1F600, 0x1F429, 0x1F43A, 0x1F98A, 0x1F981, 0x1F984, 0x1F430, 0x1F425, 0x1F427, 0x1F438, 0x1F409};
    private static final String IMAGE_DIRECTORY = "app_images";
    private static final String IMAGE_EXTENSION = ".jpeg";
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String IMAGE_DATE_TIME_FORMAT = "yyyy_MM_dd_HH_mm_ss";
    public static final String USERNAME_REGEX = "^[\\p{L}0-9._-]{4,}$";
    public static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=\\S+$).{6,}$";
    public static Toast toast;

    public static boolean checkIfFileExists(String path) {
        File file = new File(path);
        return file.exists();
    }

    public static boolean checkIfMatchesRegex(String s, String regex) {
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(s).matches();
    }

    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        if (bitmap != null) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            return outputStream.toByteArray();
        } else
            return null;
    }

    public static Bitmap getByteArrayAsBitmap(byte[] b) {
        if (b != null)
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        else
            return null;
    }

    public static String getByteArrayAsString(byte[] b) {
        try {
            return Base64.encodeToString(b, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] getStringAsByteArray(String s) {
        try {
            return Base64.decode(s.getBytes(), Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String saveImage(final Context context, final Bitmap image, final String name) {
        File directory = new File(context.getApplicationInfo().dataDir + File.separator + IMAGE_DIRECTORY);
        String finalName;

        if (name == null || name.trim().isEmpty())
            finalName = Long.toString(System.currentTimeMillis());
        else
            finalName = name;

        try {
            if (!directory.exists())
                directory.mkdir();

            File imagePath = new File(directory, File.separator + finalName + IMAGE_EXTENSION);
            FileOutputStream fos = new FileOutputStream(imagePath);
            image.compress(Bitmap.CompressFormat.JPEG, 70, fos);
            fos.close();
            return imagePath.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static Bitmap openImage(String imagePath) {
        File imageFile = new File(imagePath);
        Bitmap image = null;

        try {
            if (imageFile.exists())
                image = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return image;
    }

    public static boolean deleteImage(Context context, String name) {
        File directory = new File(context.getApplicationInfo().dataDir + File.separator + IMAGE_DIRECTORY);
        String imageName = directory.getAbsolutePath() + File.separator + name + IMAGE_EXTENSION;

        try {
            File imageFile = new File(imageName);

            if (imageFile.exists())
                imageFile.delete();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteDirectory(Context context) {
        File directory = new File(context.getApplicationInfo().dataDir + File.separator + IMAGE_DIRECTORY);

        try {
            if (directory.isDirectory()) {
                String[] children = directory.list();

                for (int i = 0; i < children.length; i++) {
                    new File(directory, children[i]).delete();
                }
                directory.delete();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void showSystemToast(Context context, String message, boolean isCentered, boolean hasLongLength, Status status, String... highlight) {
        switch (status) {
            case SUCCESS:
                message += context.getString(R.string.status_success) + context.getString(R.string.other_space) + message;
            case WARNING:
                message += context.getString(R.string.status_warning) + context.getString(R.string.other_space) + message;
            case ERROR:
                message += context.getString(R.string.status_error) + context.getString(R.string.other_space) + message;
            case UNKNOWN:
                message += context.getString(R.string.status_unknown) + context.getString(R.string.other_space) + message;
            case DEFAULT:
            default:
                break;
        }
        showToast(context, message, isCentered, hasLongLength, highlight);
    }

    public static void showToast(Context context, String message, boolean isCentered, boolean hasLongLength, String... highlight) {
        if (message != null && !message.trim().isEmpty()) {
            if (toast != null) toast.cancel();
            SpannableString span = new SpannableString(message);

            if (highlight != null) {
                for (String highlightedText : highlight) {
                    if (message.contains(highlightedText)) {
                        int color;

                        if (highlightedText.equals(context.getResources().getString(R.string.status_success)))
                            color = Color.GREEN;
                        else if (highlightedText.equals(context.getResources().getString(R.string.status_warning)))
                            color = Color.YELLOW;
                        else if (highlightedText.equals(context.getResources().getString(R.string.status_error)))
                            color = Color.RED;
                        else if (highlightedText.equals(context.getResources().getString(R.string.status_unknown)))
                            color = Color.GRAY;
                        else
                            color = Color.MAGENTA;
                        span.setSpan(new ForegroundColorSpan(color), message.indexOf(highlightedText), message.indexOf(highlightedText) + highlightedText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }
            toast = Toast.makeText(context, span, hasLongLength ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);

            if (isCentered) {
                TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                if (v != null) v.setGravity(Gravity.CENTER);
            }
            toast.show();
        }
    }

    /* To restrict Space Bar in Keyboard */
    public static InputFilter defineSpaceFilter() {
        InputFilter filter = (source, start, end, dest, dstart, dend) -> {
            for (int i = start; i < end; i++) {
                if (Character.isWhitespace(source.charAt(i))) {
                    return "";
                }
            }
            return null;
        };
        return filter;
    }

    public static Spanned fromHtml(String html) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        else
            return Html.fromHtml(html);
    }

    public static int getDominantColor(Bitmap bitmap) {
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, 1, 1, true);
        final int color = newBitmap.getPixel(0, 0);
        newBitmap.recycle();
        return color;
    }

    @ColorInt
    public static int getContrastColor(@ColorInt int color, @ColorInt int... contrastColors) {
        int highLumContrast = Color.BLACK;
        int lowLumContrast = Color.WHITE;

        if (contrastColors != null) {

            try {
                highLumContrast = contrastColors[0];
            } catch (Exception e) {
                e.printStackTrace();
                highLumContrast = Color.BLACK;
            }

            try {
                lowLumContrast = contrastColors[1];
            } catch (Exception e) {
                e.printStackTrace();
                lowLumContrast = Color.WHITE;
            }
        }

        // Counting the perceptive luminance - human eye favors green color...
        double a = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return a < 0.5 ? highLumContrast : lowLumContrast;
    }

    public static String getCurrentDateTimeString() {
        return formatDate(DATE_TIME_FORMAT);
    }

    public static String getImgDateTimeString() {
        return formatDate(IMAGE_DATE_TIME_FORMAT);
    }

    private static String formatDate(String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        Date now = new Date();
        return formatter.format(now);
    }

    public static long stringToSeed(String s) {
        if (s == null) {
            return 0L;
        }
        long hash = 0L;

        for (char c : s.toCharArray()) {
            hash = 31L * hash + c;
        }
        return hash;
    }

    public static int getRandomNumber(int n, long seed) {
        if (n <= 0)
            n = 1;
        Random r = new Random();
        r.setSeed(seed);
        return r.nextInt(n);
    }

    public static String generateIdeogram(String stringSeed) {
        long seed = stringToSeed(stringSeed);
        int unicode = UNICODE_HEX[getRandomNumber(UNICODE_HEX.length, seed)];
        return getIdeogramByUnicode(unicode);
    }

    public static String getIdeogramByUnicode(int unicode) {
        return new String(Character.toChars(unicode));
    }

    public static void storeUserData(Context context, List<User> users) {
        if (users != null && users.size() > 0) {
            SharedPreferences.Editor editor = context.getSharedPreferences(context.getString(R.string.app_preferences), Context.MODE_PRIVATE).edit();
            editor.putLong("profile_id", users.get(0).getId());
            editor.putString("profile_username", users.get(0).getUsername());
            editor.putString("profile_name", users.get(0).getName());
            editor.putString("profile_lastName", users.get(0).getLastName());
            editor.putString("profile_email", users.get(0).getEmail());
            editor.putBoolean("profile_isAdmin", users.get(0).isAdmin());
            editor.putString("profile_image", Methods.getByteArrayAsString(users.get(0).getImage()));
            editor.commit();
        }
    }

    public static void logOff(Context context, boolean userIsInteracting) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.app_preferences), Context.MODE_PRIVATE);

        try {
            if (preferences.contains("profile_id"))
                preferences.edit().remove("profile_id").commit();

            if (preferences.contains("profile_username"))
                preferences.edit().remove("profile_username").commit();

            if (preferences.contains("profile_name"))
                preferences.edit().remove("profile_name").commit();

            if (preferences.contains("profile_lastName"))
                preferences.edit().remove("profile_lastName").commit();

            if (preferences.contains("profile_email"))
                preferences.edit().remove("profile_email").commit();

            if (preferences.contains("profile_isAdmin"))
                preferences.edit().remove("profile_isAdmin").commit();

            if (preferences.contains("profile_image"))
                preferences.edit().remove("profile_image").commit();

            if (preferences.contains("temp_currentScreen"))
                preferences.edit().remove("temp_currentScreen").commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Intent intent = new Intent(context, MainActivity.class);

            if (userIsInteracting) {
                intent.putExtra("isLoggingOff", true);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);

                if (context instanceof Activity) {
                    ((Activity) context).finish();
                }
                Runtime.getRuntime().exit(0);
            } else {
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void restartApplication(Context context) {
        Intent i = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 9999, i, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent);
        System.exit(0);
    }
}
