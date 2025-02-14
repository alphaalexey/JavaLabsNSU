package ru.alspace.supplier;

import ru.alspace.model.Motor;
import ru.alspace.storage.Storage;

import java.util.concurrent.atomic.AtomicInteger;

public class MotorSupplier extends PartSupplier<Motor> {
    public MotorSupplier(Storage<Motor> storage, long delay, AtomicInteger producedCount) {
        super(storage, delay, producedCount);
    }

    @Override
    protected Motor createPart() {
        return new Motor();
    }
}
