package app.bootstrap.core.cqrs.command;

import app.bootstrap.core.cqrs.SimpleCommandBus;
import org.junit.jupiter.api.Test;

class TestCommand {

    @Test
    void test() throws Exception {
        final SimpleCommandBus simpleCommandBus = new SimpleCommandBus();
        final SimpleCommandHandler simpleCommandHandler = new SimpleCommandHandler(simpleCommandBus);
        simpleCommandBus.register(simpleCommandHandler, SimpleTrackableCommand.class);
        simpleCommandBus.send(new SimpleTrackableCommand("message"));
    }
}


