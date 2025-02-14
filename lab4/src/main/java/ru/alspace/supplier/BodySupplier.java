package ru.alspace.supplier;

import ru.alspace.model.Body;
import ru.alspace.storage.Storage;

import java.util.concurrent.atomic.AtomicInteger;

public class BodySupplier extends PartSupplier<Body> {
    public BodySupplier(Storage<Body> storage, long delay, AtomicInteger producedCount) {
        super(storage, delay, producedCount);
    }

    @Override
    protected Body createPart() {
        return new Body();
    }
}
