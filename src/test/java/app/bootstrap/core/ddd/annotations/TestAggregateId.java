package app.bootstrap.core.ddd.annotations;

import app.bootstrap.core.ddd.Id;
import jakarta.annotation.Nonnull;

import java.util.UUID;

public class TestAggregateId extends Id {

    public TestAggregateId(@Nonnull UUID uuid) {
        super(uuid);
    }
}
