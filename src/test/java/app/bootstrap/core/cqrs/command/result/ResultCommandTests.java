/*
 * App Bootstrap Core
 */
package app.bootstrap.core.cqrs.command.result;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import app.bootstrap.core.cqrs.IResultCommand;
import app.bootstrap.core.cqrs.IResultCommandHandler;
import app.bootstrap.core.cqrs.SimpleICommandBus;
import jakarta.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ResultCommandTests {

    private SimpleICommandBus commandBus;

    @BeforeEach
    void setUp() {
        commandBus = new SimpleICommandBus();
    }

    @Test
    void sendSync_shouldReturnValue_whenHandlerRegistered() throws Exception {
        // Given
        SimpleResultCommandHandler handler = new SimpleResultCommandHandler();
        commandBus.register(handler, SimpleResultCommand.class);
        SimpleResultCommand cmd = new SimpleResultCommand("ok");

        // When
        String result = commandBus.sendSync(cmd);

        // Then
        assertThat(result).isEqualTo("ok handled");
    }

    @Test
    void send_shouldReturnFutureWithValue_whenHandlerRegistered() throws Exception {
        // Given
        SimpleResultCommandHandler handler = new SimpleResultCommandHandler();
        commandBus.register(handler, SimpleResultCommand.class);
        SimpleResultCommand cmd = new SimpleResultCommand("async");

        // When
        CompletableFuture<String> future = commandBus.send(cmd);

        // Then
        assertThat(future).isNotNull();
        assertThat(future.get()).isEqualTo("async handled");
    }

    @Test
    void sendSync_shouldThrow_whenNoHandlerRegistered() {
        // Given
        SimpleResultCommand cmd = new SimpleResultCommand("x");

        // When & Then
        assertThatThrownBy(() -> commandBus.sendSync(cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No handler registered for " + SimpleResultCommand.class.getName());
    }

    @Test
    void send_shouldCompleteExceptionally_whenNoHandlerRegistered() throws Exception {
        // Given
        SimpleResultCommand cmd = new SimpleResultCommand("x");

        // When
        CompletableFuture<String> future = commandBus.send(cmd);

        // Then
        assertThatThrownBy(future::get)
                .hasCauseInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No handler registered for " + SimpleResultCommand.class.getName());
    }

    @Test
    void sendSync_shouldPropagateException_whenHandlerThrows() {
        // Given
        IResultCommand<String> badCmd = new IResultCommand<>() {};
        @SuppressWarnings("unchecked")
        Class<? extends IResultCommand<String>> badCmdClass = (Class<? extends IResultCommand<String>>) badCmd.getClass();
        IResultCommandHandler<IResultCommand<String>, String> throwingHandler =
                new IResultCommandHandler<>() {
                    @Nonnull
                    @Override
                    public String handle(@Nonnull IResultCommand<String> command) throws Exception {
                        throw new RuntimeException("boom");
                    }
                };
        commandBus.register(throwingHandler, badCmdClass);

        // When & Then
        assertThatThrownBy(() -> commandBus.sendSync(badCmd))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("boom");
    }
}
