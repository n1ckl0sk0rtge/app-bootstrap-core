/*
 * App Bootstrap Core
 */
package app.bootstrap.core.cqrs.command.result;

import app.bootstrap.core.cqrs.IResultCommandHandler;
import jakarta.annotation.Nonnull;

public final class SimpleResultCommandHandler
        implements IResultCommandHandler<SimpleResultCommand, String> {

    @Nonnull
    @Override
    public String handle(@Nonnull SimpleResultCommand command) throws Exception {
        return command.payload() + " handled";
    }
}
