package com.gasmtask.support;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Relógio controlável para os testes de integração. Anda junto com o tempo real, deslocado por um offset.
 * Só pode ser adiantado: a validação do JWT usa o relógio real, então um token emitido "no passado"
 * já nasceria expirado.
 */
public class TestClock extends Clock {

    private final AtomicReference<Duration> offset = new AtomicReference<>(Duration.ZERO);

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return Clock.offset(Clock.system(zone), offset.get());
    }

    @Override
    public Instant instant() {
        return Instant.now().plus(offset.get());
    }

    public void setTo(Instant target) {
        Duration delta = Duration.between(Instant.now(), target);
        if (delta.isNegative()) {
            throw new IllegalArgumentException("O relógio de teste só anda para a frente: " + target);
        }
        offset.set(delta);
    }

    public void setTo(LocalDateTime localDateTime, ZoneId zone) {
        setTo(localDateTime.atZone(zone).toInstant());
    }

    public void reset() {
        offset.set(Duration.ZERO);
    }
}
