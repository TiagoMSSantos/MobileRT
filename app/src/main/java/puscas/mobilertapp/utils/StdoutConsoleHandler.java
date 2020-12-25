package puscas.mobilertapp.utils;

import java.io.OutputStream;
import java.util.logging.ConsoleHandler;
import javax.annotation.Nonnull;

/**
 * A custom {@link ConsoleHandler} that logs everything to the std output instead of the default
 * std error that {@link ConsoleHandler} uses.
 */
public class StdoutConsoleHandler extends ConsoleHandler {

    /**
     * Method that allows to change the default output stream.
     * {@inheritDoc}
     *
     * @param out The new output stream.
     * @throws SecurityException
     */
    @Override
    protected final synchronized void setOutputStream(@Nonnull final OutputStream out) {
        super.setOutputStream(System.out);
    }
}
