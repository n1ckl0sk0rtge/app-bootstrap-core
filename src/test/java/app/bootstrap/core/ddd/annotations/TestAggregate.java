package app.bootstrap.core.ddd.annotations;

import app.bootstrap.core.ddd.AggregateRoot;
import app.bootstrap.core.ddd.IDomainEvent;
import jakarta.annotation.Nonnull;

import java.util.List;

@app.bootstrap.core.ddd.annotation.AggregateRoot(description = """
        A test aggregate class used to validate and demonstrate the behavior of AggregateRoot functionality
        in a testing environment. It serves as a concrete implementation for unit testing purposes.""")
public class TestAggregate extends AggregateRoot<TestAggregateId> {

    public TestAggregate(@Nonnull TestAggregateId id,
                         @Nonnull List<IDomainEvent> domainEvents) {
        super(id, domainEvents);
    }
}
