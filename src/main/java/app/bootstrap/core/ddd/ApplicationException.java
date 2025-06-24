package app.bootstrap.core.ddd;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public abstract class ApplicationException extends Exception {
    @Nonnull
    protected final String errorCode;
    @Nullable
    protected final transient Object context;

    protected ApplicationException(@Nonnull String message, @Nonnull String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.context = null;
    }

    protected ApplicationException(@Nonnull String message,  @Nonnull String errorCode, @Nonnull Object context) {
        super(message);
        this.errorCode = errorCode;
        this.context = context;
    }


    @Nonnull
    public String getErrorCode() {
        return errorCode;
    }

    @Nullable
    public Object getContext() {
        return context;
    }
}
