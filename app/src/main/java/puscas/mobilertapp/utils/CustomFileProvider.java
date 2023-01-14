package puscas.mobilertapp.utils;

import androidx.core.content.FileProvider;

import puscas.mobilertapp.R;

/**
 * FileProvider is a special subclass of {@link FileProvider} that facilitates secure sharing
 * of files associated with an app by creating a <code>content://</code> {@link android.net.Uri}
 * for a file instead of a <code>file:///</code> {@link android.net.Uri}.
 */
public class CustomFileProvider extends FileProvider {
    public CustomFileProvider() {
        super(R.xml.file_paths);
    }
}
