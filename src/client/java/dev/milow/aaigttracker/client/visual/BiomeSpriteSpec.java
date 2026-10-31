package dev.milow.aaigttracker.client.visual;

import net.minecraft.resources.Identifier;

public record BiomeSpriteSpec(Identifier biomeId) {
    public boolean isEmpty() {
        return this.biomeId == null;
    }
}
