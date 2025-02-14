package ru.alspace.supplier;

import ru.alspace.model.Accessory;
import ru.alspace.storage.Storage;

import java.util.concurrent.atomic.AtomicInteger;

public class AccessorySupplier extends PartSupplier<Accessory> {
    public AccessorySupplier(Storage<Accessory> storage, long delay, AtomicInteger producedCount) {
        super(storage, delay, producedCount);
    }

    @Override
    protected Accessory createPart() {
        return new Accessory();
    }
}
