package com.example.btl;

import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/** Giữ bản sao ảnh trong app để GetContent không bị mất quyền đọc khi mở lại. */
public final class ImageStorage {
    private ImageStorage() {}

    public static String copyIntoApp(Context context, Uri source) throws IOException {
        File directory = new File(context.getFilesDir(), "plant_images");
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Không thể tạo thư mục lưu ảnh");
        }

        String mime = context.getContentResolver().getType(source);
        String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime);
        if (extension == null || extension.isEmpty()) extension = "jpg";
        File destination = new File(directory, UUID.randomUUID() + "." + extension);

        try (InputStream input = context.getContentResolver().openInputStream(source)) {
            if (input == null) throw new IOException("Không đọc được ảnh từ thư viện");
            try (OutputStream output = new FileOutputStream(destination)) {
                byte[] bytes = new byte[8192];
                int len;
                while ((len = input.read(bytes)) != -1) output.write(bytes, 0, len);
            }
        } catch (IOException ex) {
            destination.delete();
            throw ex;
        }
        return Uri.fromFile(destination).toString();
    }

    public static void display(ImageView view, String uriString) {
        view.setImageDrawable(null);
        if (uriString == null || uriString.trim().isEmpty()) {
            view.setImageResource(R.drawable.plant_default);
            return;
        }
        try {
            view.setImageURI(Uri.parse(uriString));
            if (view.getDrawable() == null) view.setImageResource(R.drawable.plant_default);
        } catch (Exception ignored) {
            view.setImageResource(R.drawable.plant_default);
        }
    }

    // Chỉ xóa file do chính ứng dụng quản lý; không xóa ảnh gốc của người dùng.
    public static void deleteIfLocal(Context context, String uriString) {
        if (uriString == null || uriString.isEmpty()) return;
        Uri uri = Uri.parse(uriString);
        if (!"file".equals(uri.getScheme())) return;

        File directory = new File(context.getFilesDir(), "plant_images");
        File target = new File(uri.getPath() == null ? "" : uri.getPath());
        try {
            String root = directory.getCanonicalPath() + File.separator;
            if (target.getCanonicalPath().startsWith(root)) target.delete();
        } catch (IOException ignored) {
            // Không được xóa tập tin ngoài thư mục riêng của app.
        }
    }
}
